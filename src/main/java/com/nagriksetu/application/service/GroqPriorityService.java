package com.nagriksetu.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nagriksetu.application.model.Department;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GroqPriorityService {
    private static final Logger log = LoggerFactory.getLogger(GroqPriorityService.class);
    @Value("${groq.api.key:}") private String groqApiKey;
    @Value("${groq.text.model:openai/gpt-oss-20b}") private String textModel;
    @Value("${groq.vision.model:qwen/qwen3.8-27b}") private String visionModel;
    private final ObjectMapper objectMapper;

    public GroqPriorityService(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public String suggestPriority(String title, String description, String category, String department,
                                  String location, Double latitude, Double longitude, MultipartFile image) {
        if (groqApiKey.isBlank()) return "3";
        String prompt = "You are the priority assessor for a municipal civic-report queue. Analyse every provided report field and, when present, the attached image. Do not infer a hazard that is not supported by the report or image. Return JSON only in exactly this format: {\"priority\":\"N\"}. Priority rubric: 1=immediate risk of death, injury, fire, flooding, collapse, or major public-health hazard; 2=serious safety/health risk or essential-service outage; 3=standard service issue requiring normal response; 4=minor inconvenience; 5=cosmetic or very low impact. Report title: " + safe(title) + "; description: " + safe(description) + "; category: " + safe(category) + "; selected department: " + safe(department) + "; location: " + safe(location) + "; latitude: " + safe(latitude) + "; longitude: " + safe(longitude) + ".";
        boolean hasImage = image != null && !image.isEmpty();
        if (hasImage) {
            try {
                // Primary assessment: uses the photo plus all written report context.
                return requestPriority(visionModel, multimodalContent(prompt, image));
            } catch (Exception exception) {
                log.warn("Groq vision priority assessment failed; retrying with the report text", exception);
            }
        }
        try {
            // A text retry avoids silently assigning priority 3 merely because a photo payload
            // was rejected, too large, or temporarily unavailable to the vision model.
            return requestPriority(textModel, prompt);
        } catch (Exception exception) {
            log.warn("Groq text priority assessment failed; using neutral priority 3", exception);
            return "3";
        }
    }

    private String requestPriority(String model, Object content) {
        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", content)),
                "temperature", 0,
                "max_completion_tokens", 240);
        String response = RestClient.create().post().uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey).contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(String.class);
        String priority = priorityFrom(response);
        if (priority == null) throw new IllegalStateException("Groq returned an invalid priority response.");
        return priority;
    }

    /** Select a supported department from the report title before the report is submitted. */
    public DepartmentSuggestion suggestDepartment(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Enter an issue title before using automatic department selection.");
        if (groqApiKey.isBlank()) throw new IllegalStateException("Groq is not configured. Set GROQ_API_KEY before using automatic department selection.");

        String choices = java.util.Arrays.stream(Department.values())
                .map(Department::name)
                .collect(java.util.stream.Collectors.joining("; "));
        String prompt = "You route civic issues to a Maharashtra government department. Based only on the issue title, choose exactly one value from ALLOWED_DEPARTMENT_KEYS. Return only one JSON object in this exact format: {\"department\":\"ONE_ALLOWED_KEY\"}. Do not return a label, explanation, or any key outside that list. Issue title: " + safe(title) + ". ALLOWED_DEPARTMENT_KEYS: " + choices;

        try {
            Map<String, Object> payload = Map.of("model", textModel, "messages", List.of(Map.of("role", "user", "content", prompt)), "temperature", 0, "max_completion_tokens", 160);
            String response = RestClient.create().post().uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey).contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(String.class);
            Department department = departmentFrom(response);
            // The browser and database receive only labels from the Department enum. If a
            // model response is imprecise, retain a valid application result by using the
            // title-routing fallback instead of exposing an unsupported free-form value.
            if (department == null) department = fallbackDepartment(title);
            return new DepartmentSuggestion(department.label(), "AI suggestion based on the report title");
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Groq could not select a department right now. Please choose one manually.", exception);
        }
    }

    /** Validates that an uploaded report image actually relates to the citizen's title. */
    public ImageVerification verifyReportImage(String title, MultipartFile image) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Enter an issue title before uploading a photo for AI verification.");
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("Choose an image to verify.");
        if (groqApiKey.isBlank()) throw new IllegalStateException("Groq is not configured. Image verification is unavailable.");
        String prompt = "You validate a civic issue photo against its report title. Inspect the image and decide whether it visibly and plausibly depicts the same civic issue. Return only JSON in exactly this format: {\"matches\":true} or {\"matches\":false}. Be strict: return false when the image is unrelated, unclear, or cannot support the title. Report title: " + safe(title);
        try {
            Map<String, Object> payload = Map.of(
                    "model", visionModel,
                    "messages", List.of(Map.of("role", "user", "content", multimodalContent(prompt, image))),
                    "temperature", 0,
                    "max_completion_tokens", 160);
            String response = RestClient.create().post().uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey).contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().body(String.class);
            boolean matches = imageMatchFrom(response);
            if (matches) return new ImageVerification(true, "Image verified as \"" + title.trim() + "\" issue.");
            return new ImageVerification(false, "The uploaded image does not appear to match the \"" + title.trim() + "\" issue. Please choose a relevant photo.");
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Groq could not verify this image right now. Please try again.", exception);
        }
    }

    private List<Map<String, Object>> multimodalContent(String prompt, MultipartFile image) throws IOException {
        String mimeType = image.getContentType() == null ? "image/jpeg" : image.getContentType();
        String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(image.getBytes());
        return List.of(Map.of("type", "text", "text", prompt), Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)));
    }

    private String priorityFrom(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            String priority = objectMapper.readTree(content).path("priority").asText();
            if (priority.matches("[1-5]")) return priority;
        } catch (Exception ignored) { }
        Matcher match = Pattern.compile("\\\"priority\\\"\\s*:\\s*\\\"?([1-5])").matcher(response == null ? "" : response);
        return match.find() ? match.group(1) : null;
    }

    private boolean imageMatchFrom(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode result = objectMapper.readTree(content);
            if (result.has("matches") && result.get("matches").isBoolean()) return result.get("matches").asBoolean();
        } catch (Exception ignored) { }
        // Failed or malformed validation responses must not be accepted as verified images.
        return false;
    }

    private Department departmentFrom(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            String candidate = objectMapper.readTree(content).path("department").asText("");
            Department exact = departmentByKeyOrLabel(candidate);
            if (exact != null) return exact;
            for (Department department : Department.values()) if (content.contains(department.name()) || content.toLowerCase().contains(department.label().toLowerCase())) return department;
        } catch (Exception ignored) { }
        return null;
    }

    private Department departmentByKeyOrLabel(String candidate) {
        if (candidate == null || candidate.isBlank()) return null;
        String normalised = candidate.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        for (Department department : Department.values()) {
            if (department.name().equals(normalised) || department.label().equalsIgnoreCase(candidate.trim())) return department;
        }
        return null;
    }

    private Department fallbackDepartment(String title) {
        String text = title == null ? "" : title.toLowerCase();
        if (matches(text, "garbage", "waste", "dump", "litter", "sanitation", "sewage")) return Department.URBAN_DEVELOPMENT;
        if (matches(text, "pothole", "road", "footpath", "bridge", "sidewalk")) return Department.PUBLIC_WORKS;
        if (matches(text, "streetlight", "electric", "power", "wire", "transformer")) return Department.ENERGY;
        if (matches(text, "water", "drain", "drainage", "flood", "pipe", "leak")) return Department.WATER_SUPPLY_AND_SANITATION;
        if (matches(text, "fire", "disaster", "landslide", "emergency")) return Department.DISASTER_MANAGEMENT;
        if (matches(text, "hospital", "health", "mosquito", "disease", "medical")) return Department.PUBLIC_HEALTH;
        if (matches(text, "bus", "traffic", "transport", "port")) return Department.TRANSPORT_AND_PORTS;
        return Department.URBAN_DEVELOPMENT;
    }

    private boolean matches(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }

    private String safe(Object value) { return value == null || value.toString().isBlank() ? "not provided" : value.toString(); }
    public record DepartmentSuggestion(String department, String rationale) { }
    public record ImageVerification(boolean verified, String message) { }
}
