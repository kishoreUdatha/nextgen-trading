# Trading Platform (Monorepo Skeleton)

This is a runnable **skeleton** for a microservices-based trading platform (OMS, Risk, Portfolio, MarketData, ExecAdapter)
generated from your prompt. It boots with Docker Compose (Kafka, Postgres, Redis, ClickHouse) and minimal Spring Boot services.

> This is a minimal starter: APIs, config, topics, and wiring stubs are present so your team can extend quickly.

## Quick start

```bash
# 1) Build Maven modules
./mvnw -T 1C -q -DskipTests clean package

# 2) Bring up infra + services
docker compose up -d
```

Services (default):
- OMS: http://localhost:8081
- Risk: http://localhost:8082
- Portfolio: http://localhost:8083
- MarketData: http://localhost:8084
- ExecAdapter: http://localhost:8085

## Modules
- `/services/*` - microservices (Spring Boot 3, WebFlux, Java 17)
- `/libs/*` - shared libraries (domain, messaging, security)
- `/ops/*` - docker, k8s, clickhouse DDL, grafana/prometheus

## Next steps
- Implement real risk checks, order state machine, Kafka outbox, and ClickHouse sinks.
- Expand OpenAPI specs in `/docs` and generate client SDKs if needed.
