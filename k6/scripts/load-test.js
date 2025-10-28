import { check, sleep } from 'k6';
import { getAllDevices, getDeviceById, getDevicesByBrand, createDevice, updateDevice, deleteDevice } from './common.js';

export const options = {
  stages: [
    { duration: '1m', target: 50 },   // Ramp up to 50 VUs
    { duration: '4m', target: 50 },   // Stay at 50 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.01'],  // Less than 1% failures
    'http_req_duration{name:CreateDevice}': ['p(95)<300'],
    'http_req_duration{name:GetAllDevices}': ['p(95)<200'],
    'http_req_duration{name:GetDeviceById}': ['p(95)<100'],
  },
};

let deviceIds = []; // Shared array to store created device IDs

export default function () {
  const probability = Math.random();
  
  // 40%: Get all devices
  if (probability < 0.40) {
    getAllDevices();
  }
  // 20%: Get device by ID
  else if (probability < 0.60) {
    if (deviceIds.length > 0) {
      const randomId = deviceIds[Math.floor(Math.random() * deviceIds.length)];
      getDeviceById(randomId);
    } else {
      getAllDevices(); // Fallback if no devices exist
    }
  }
  // 15%: Get devices by brand
  else if (probability < 0.75) {
    const brands = ['Philips', 'Samsung', 'Ring', 'Nest'];
    const brand = brands[Math.floor(Math.random() * brands.length)];
    getDevicesByBrand(brand);
  }
  // 10%: Create device
  else if (probability < 0.85) {
    const deviceId = createDevice();
    if (deviceId) {
      deviceIds.push(deviceId);
      // Keep only last 100 device IDs to avoid memory issues
      if (deviceIds.length > 100) {
        deviceIds.shift();
      }
    }
  }
  // 10%: Update device
  else if (probability < 0.95) {
    if (deviceIds.length > 0) {
      const randomId = deviceIds[Math.floor(Math.random() * deviceIds.length)];
      updateDevice(randomId);
    } else {
      getAllDevices(); // Fallback
    }
  }
  // 5%: Delete device
  else {
    if (deviceIds.length > 0) {
      const randomId = deviceIds.splice(Math.floor(Math.random() * deviceIds.length), 1)[0];
      deleteDevice(randomId);
    } else {
      getAllDevices(); // Fallback
    }
  }
  
  sleep(1);
}

