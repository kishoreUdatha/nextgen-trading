# k6 Load Tests
Run after the stack is up:

```bash
docker run --rm -i --net=host grafana/k6 run - < ops/k6/place_order.js
```

This drives order placement against `OMS` and records `place_latency` and `http_req_duration` thresholds.
If running outside market hours (IST), expect HTTP 422 (market closed).
