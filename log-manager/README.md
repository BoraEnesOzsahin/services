# log-manager — ELK + Filebeat Stack

Central log aggregation service for the **reckon** platform.  
Runs **Elasticsearch 8**, **Logstash**, **Kibana**, and **Filebeat** inside the shared `reckon-ext-net` Docker network, collecting logs from every container on the host.

---

## Architecture

```
Other services (reckon-ext-net)
        │  Docker stdout/stderr
        ▼
   ┌─────────┐   container logs    ┌──────────┐   Beats protocol   ┌──────────┐
   │ Docker  │──────────────────▶  │ Filebeat │──────────────────▶ │ Logstash │
   │  host   │                     └──────────┘                     └────┬─────┘
   └─────────┘                                                            │ HTTP
                                                                          ▼
                                                                ┌───────────────┐
                                                                │ Elasticsearch │
                                                                └───────┬───────┘
                                                                        │ HTTP
                                                                        ▼
                                                                   ┌────────┐
                                                                   │ Kibana │
                                                                   └────────┘
```

| Component       | Container name    | Port(s)          |
|-----------------|-------------------|------------------|
| Elasticsearch   | `elasticsearch`   | `9200`           |
| Kibana          | `kibana`          | `5601`           |
| Logstash        | `logstash`        | `5044`, `9600`   |
| Filebeat        | `filebeat`        | *(no port)*      |
| log-manager app | `log-manager`     | `8080`           |

---

## Prerequisites

1. Docker Engine ≥ 24 & Docker Compose v2
2. **This stack creates `reckon-ext-net`** — start it first.  
   All other services join the network as `external: true` in their own compose files:
   ```yaml
   networks:
     reckon-ext-net:
       external: true
   ```
3. Increase the host's virtual memory limit (required by Elasticsearch):
   ```bash
   # Linux / WSL2
   sudo sysctl -w vm.max_map_count=262144
   # Make it permanent
   echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
   ```

---

## Quick Start

```bash
# 1. (Linux/WSL2 only) — required by Elasticsearch, do once per boot
sudo sysctl -w vm.max_map_count=262144

# 2. Start the full stack
#    The multi-stage Dockerfile builds the JAR inside Docker — no local Maven needed
docker compose up -d --build

# 3. Follow logs
docker compose logs -f

# 4. Open Kibana
#    http://localhost:5601
#    Stack Management → Data Views → Create data view
#    Index pattern:  reckon-logs-*
#    Timestamp field: @timestamp

# 5. Verify log-manager is up
curl http://localhost:8080/api/status
curl http://localhost:8080/actuator/health
```

---

## Configuration files

| File | Purpose |
|------|---------|
| [`docker-compose.yml`](docker-compose.yml) | Core ELK + Filebeat services |
| [`docker-compose.override.yml`](docker-compose.override.yml) | log-manager Spring Boot service |
| [`.env`](.env) | Elastic version & JVM heap sizes |
| [`filebeat/filebeat.yml`](filebeat/filebeat.yml) | Filebeat Docker autodiscover + multiline |
| [`logstash/config/logstash.yml`](logstash/config/logstash.yml) | Logstash main config |
| [`logstash/pipeline/logstash.conf`](logstash/pipeline/logstash.conf) | Logstash pipeline (input → filter → output) |
| [`src/main/resources/application-docker.yaml`](src/main/resources/application-docker.yaml) | Spring Boot ECS JSON logging (Docker profile) |

---

## Integrating other services

Any service on `reckon-ext-net` is automatically discovered by Filebeat.  
To enable multiline (e.g. for Java stack traces) add these Docker labels to the other service:

```yaml
labels:
  co.elastic.logs/enabled: "true"
  co.elastic.logs/multiline.pattern: "^[0-9]{4}-[0-9]{2}-[0-9]{2}"
  co.elastic.logs/multiline.negate:  "true"
  co.elastic.logs/multiline.match:   "after"
```

Logs are indexed as `reckon-logs-<container-name>-YYYY.MM.DD`.

---

## Useful commands

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health?pretty

# List indices
curl http://localhost:9200/_cat/indices?v

# Logstash monitoring
curl http://localhost:9600/_node/stats?pretty

# Restart only Filebeat (e.g. after config change)
docker compose restart filebeat

# Tear down (keep volumes)
docker compose down

# Tear down + delete all data
docker compose down -v
```
