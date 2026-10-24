function formatDeviceLocation(coordinates) {
  if (!Number.isFinite(coordinates?.latitude) || !Number.isFinite(coordinates?.longitude)) return '';
  return `Device location: ${coordinates.latitude.toFixed(6)}, ${coordinates.longitude.toFixed(6)}`;
}

if (typeof module !== 'undefined') module.exports = { formatDeviceLocation };
