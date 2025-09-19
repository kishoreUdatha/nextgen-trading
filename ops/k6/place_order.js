// k6 script: order placement throughput & lag SLOs
import http from 'k6/http';
import { check, sleep, Trend } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
  },
};

const placeLatency = new Trend('place_latency');

export default function () {
  const url = 'http://localhost:8081/api/v1/orders';
  const payload = JSON.stringify({
    symbol: 'NIFTY', segment: 'CASH', side: 'BUY',
    qty: 1, orderType: 'MARKET', tif: 'DAY'
  });
  const headers = { 'Content-Type': 'application/json' };

  const t0 = Date.now();
  let res = http.post(url, payload, { headers });
  const t1 = Date.now();
  placeLatency.add(t1 - t0);

  check(res, {
    'status 201 or 422': (r) => r.status === 201 || r.status === 422,
  });

  sleep(0.2);
}
