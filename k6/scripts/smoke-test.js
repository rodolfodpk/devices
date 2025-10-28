import { check } from 'k6';
import http from 'k6/http';
import { getAllDevices, getDeviceById, getDevicesByBrand, getDevicesByState } from './common.js';

export const options = {
  stages: [
    { duration: '30s', target: 5 },  // Ramp up to 5 VUs
    { duration: '30s', target: 5 },  // Stay at 5 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<200', 'p(99)<500'],
    http_req_failed: ['rate<0.01'],  // Less than 1% failures
  },
};

export default function () {
  // Read-only operations only
  
  // 50%: Get all devices
  if (Math.random() < 0.5) {
    getAllDevices();
  } 
  // 25%: Get devices by brand
  else if (Math.random() < 0.75) {
    const brands = ['Philips', 'Samsung', 'Ring', 'Nest'];
    const brand = brands[Math.floor(Math.random() * brands.length)];
    getDevicesByBrand(brand);
  }
  // 25%: Get devices by state
  else {
    const states = ['AVAILABLE', 'IN_USE', 'INACTIVE'];
    const state = states[Math.floor(Math.random() * states.length)];
    getDevicesByState(state);
  }
}

