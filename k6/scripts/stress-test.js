import { check, sleep } from 'k6';
import { getAllDevices, getDeviceById, getDevicesByBrand, createDevice, updateDevice, deleteDevice } from './common.js';

export const options = {
  stages: [
    { duration: '5m', target: 10 },   // Ramp up to 10 VUs
    { duration: '5m', target: 100 },  // Gradually increase to 100 VUs
    { duration: '5m', target: 200 },  // Push to 200 VUs
    { duration: '5m', target: 300 },  // Peak at 300 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
    http_req_failed: ['rate<0.05'],  // Allow up to 5% failures under stress
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
  
  sleep(0.5); // Shorter sleep to increase pressure
}

