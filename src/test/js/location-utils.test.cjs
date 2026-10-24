const test = require('node:test');
const assert = require('node:assert/strict');

const { formatDeviceLocation } = require('../../main/resources/static/js/location-utils.js');

test('formats captured device coordinates for the report location field', () => {
  assert.equal(
    formatDeviceLocation({ latitude: 23.34412345, longitude: 85.30987654 }),
    'Device location: 23.344123, 85.309877'
  );
});

test('does not format an incomplete device position', () => {
  assert.equal(formatDeviceLocation({ latitude: 23.34412345 }), '');
});
