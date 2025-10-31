import http from 'k6/http';
import { check, sleep } from 'k6';

// Base URL for API
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api/v1/devices`;

// Device name and brand pools for realistic testing
const NAMES = [
  'Sensor', 'Camera', 'Thermostat', 'Lock', 'Light', 'Doorbell',
  'Motion', 'Smoke', 'Water', 'Window', 'Garage', 'Blinds'
];

const BRANDS = [
  'Philips', 'Samsung', 'Honeywell', 'Ring', 'Nest', 'August',
  'Wyze', 'Eufy', 'TP-Link', 'Arlo', 'Abode', 'SimpliSafe'
];

// Helper function to create a random device name
export function randomDeviceName() {
  return `${NAMES[Math.floor(Math.random() * NAMES.length)]}-${Math.floor(Math.random() * 1000)}`;
}

// Helper function to create a random brand
export function randomBrand() {
  return BRANDS[Math.floor(Math.random() * BRANDS.length)];
}

// Helper function to create a new device
export function createDevice() {
  const payload = JSON.stringify({
    name: randomDeviceName(),
    brand: randomBrand()
  });
  
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'CreateDevice' }
  };
  
  const response = http.post(`${API_BASE}`, payload, params);
  
  const success = check(response, {
    'create device status is 201': (r) => r.status === 201,
    'create device has ID': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.id !== undefined && body.id !== null;
      } catch (e) {
        return false;
      }
    }
  });
  
  if (success && response.status === 201) {
    try {
      const device = JSON.parse(response.body);
      return device.id;
    } catch (e) {
      return null;
    }
  }
  
  return null;
}

// Helper function to get all devices
export function getAllDevices() {
  const params = { tags: { name: 'GetAllDevices' } };
  const response = http.get(`${API_BASE}`, params);
  
  check(response, {
    'get all devices status is 200': (r) => r.status === 200,
    'get all devices returns array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch (e) {
        return false;
      }
    }
  });
  
  return response;
}

// Helper function to get device by ID
export function getDeviceById(id) {
  const params = { tags: { name: 'GetDeviceById' } };
  const response = http.get(`${API_BASE}/${id}`, params);
  
  check(response, {
    'get by ID status is 200': (r) => r.status === 200,
    'get by ID returns device': (r) => r.status === 200 || r.status === 404
  });
  
  return response;
}

// Helper function to get devices by brand
export function getDevicesByBrand(brand) {
  const params = { 
    tags: { name: 'GetDevicesByBrand' },
    params: { brand: brand }
  };
  const response = http.get(`${API_BASE}`, params);
  
  check(response, {
    'get by brand status is 200': (r) => r.status === 200,
    'get by brand returns array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch (e) {
        return false;
      }
    }
  });
  
  return response;
}

// Helper function to get devices by state
export function getDevicesByState(state) {
  const params = { 
    tags: { name: 'GetDevicesByState' },
    params: { state: state }
  };
  const response = http.get(`${API_BASE}`, params);
  
  check(response, {
    'get by state status is 200': (r) => r.status === 200,
    'get by state returns array': (r) => {
      try {
        const body = JSON.parse(r.body);
        return Array.isArray(body);
      } catch (e) {
        return false;
      }
    }
  });
  
  return response;
}

// Helper function to update device
export function updateDevice(id) {
  const payload = JSON.stringify({
    name: randomDeviceName(),
    brand: randomBrand()
  });
  
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'UpdateDevice' }
  };
  
  const response = http.patch(`${API_BASE}/${id}`, payload, params);
  
  check(response, {
    'update device status is valid': (r) => r.status === 200 || r.status === 400 || r.status === 404,
    'update device returns device or error': (r) => r.status >= 200 && r.status < 500
  });
  
  return response;
}

// Helper function to delete device
export function deleteDevice(id) {
  const params = { tags: { name: 'DeleteDevice' } };
  const response = http.del(`${API_BASE}/${id}`, null, params);
  
  check(response, {
    'delete device status is valid': (r) => r.status === 204 || r.status === 400 || r.status === 404
  });
  
  return response;
}

// Helper function to get a device and return its state
export function getDeviceState(id) {
  const response = getDeviceById(id);
  if (response.status === 200) {
    try {
      const device = JSON.parse(response.body);
      return device.state;
    } catch (e) {
      return null;
    }
  }
  return null;
}

// Device states
const STATES = ['AVAILABLE', 'IN_USE', 'INACTIVE'];

// Helper function to get a random device state
export function randomDeviceState() {
  return STATES[Math.floor(Math.random() * STATES.length)];
}

// Helper function to change device state
export function changeDeviceState(id, state) {
  const payload = JSON.stringify({
    state: state
  });
  
  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'ChangeDeviceState' }
  };
  
  const response = http.patch(`${API_BASE}/${id}`, payload, params);
  
  return response;
}

