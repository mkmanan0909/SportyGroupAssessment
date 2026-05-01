# Live events → score poll → Kafka

Small Spring Boot app: you `POST` when an event goes live or not, it polls an HTTP score URL every 10s for live ids, pushes JSON to Kafka. In-memory only, fine for a take-home.

**Requirements:** JDK 11+ and Maven. For Kafka on the default address `localhost:9092`, Docker (Docker Desktop / Engine) plus this repo’s **`docker compose`** broker is recommended.

Copy **`.env.example`** to **`.env`** when you tune Kafka advertised hostnames (remote clients); see § Remote VM.

---

## Run locally with Kafka (JVM on host)

1. **Start Kafka** (wait until the container is healthy / logs show the broker ready — first start can take ~30s):

   ```bash
   docker compose up -d
   ```

2. **Run the app** (Kafka must be reachable on **`KAFKA_BOOTSTRAP_SERVERS`**, default `localhost:9092`):

   ```bash
   mvn spring-boot:run
   ```

   Or build once and run the JAR:

   ```bash
   mvn -DskipTests package
   java -jar target/SportyGroupAssessment-1.0-SNAPSHOT.jar
   ```

   The app uses **`KAFKA_BOOTSTRAP_SERVERS`** (default `localhost:9092` mapped from the compose broker).

3. **`GET /scores/{eventId}`** is served in-process (mock). Default **`sporty.external-score.url-template`** matches **`server.port`** (`application.properties`; e.g. loopback **`GET /scores/{eventId}`**). Override if you change the HTTP port or use an external score service.

**Diagnostic:** Logs like **`Connection to node -1 … could not be established`** mean Kafka is not reachable on the bootstrap servers—start **`docker compose`**, reopen port **9092**, or fix advertise/listeners on the broker.

**`mvn clean` fails** (“Failed to delete …`SportyGroupAssessment-1.0-SNAPSHOT.jar`”): a **running JVM** still has that file open—for example **`java -jar target\…`** or an IDE run/debug. Stop those processes (**Task Manager** → `java.exe`, or close the debugger), then rerun Maven. To locate the lock holder in PowerShell:  
`Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" | Where-Object { $_.CommandLine -match 'SportyGroupAssessment' } | Select-Object ProcessId, CommandLine`

---

## Run full stack in Docker (`sporty` + Kafka)

Kafka gets two advertised listeners so **clients on the host** use **`localhost:9092`**, while the **app container** uses **`broker:29092`** on the Compose network:

```bash
docker compose --profile app up -d --build
```

Expose **8080** for HTTP. App env includes **`KAFKA_BOOTSTRAP_SERVERS=broker:29092`**.

---

## Deploy on a remote VM (end-to-end)

**Typical layout:** one Linux VM runs **broker** (Compose or managed Kafka). The **sporty JAR or Docker image** reaches the broker via **`KAFKA_BOOTSTRAP_SERVERS`**.

1. Clone the repo or copy the **`target/*.jar`** / image.
2. **Java + broker on same VM:** compose broker only (`docker compose up -d`), then:

   ```bash
   export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   java -jar SportyGroupAssessment-1.0-SNAPSHOT.jar
   ```

3. **Kafka on another host:** set **`KAFKA_BOOTSTRAP_SERVERS=host:9092`** (comma-separated list as needed). Open firewall rules for that port from the app host.
4. **Clients on other machines producing/consuming against your Docker broker** must get correct **advertised** addresses. Copy **`.env.example`** to **`.env`** and set **`KAFKA_ADVERTISED_PUBLIC_HOST`** to the VM’s **reachable DNS name or IP**, then restart **`docker compose up -d`**. Open inbound **9092** on the VM for those clients.
5. **HTTP API for operators/curl:** open inbound **8080** (or your **`server.port`**) and call `http://<vm>:8080/events/status`.

**Container image on remote:** build and push from CI or on the server:

```bash
docker build -t sporty-live-events:latest .
docker run -d -p 8080:8080 \
  -e KAFKA_BOOTSTRAP_SERVERS=your-kafka:9092 \
  sporty-live-events:latest
```

(Override **`SPORTY_EXTERNAL_SCORE_URL_TEMPLATE`** if the score API is not loopback on that container.)

---

## Configuration (env / properties)

| Purpose | Env / property |
|--------|----------------|
| Kafka bootstrap | **`KAFKA_BOOTSTRAP_SERVERS`** → `spring.kafka.bootstrap-servers` |
| Topic | **`SPORTY_KAFKA_TOPIC`** → `sporty.kafka.topic` (default `live-event-scores`) |
| Score URL template | **`SPORTY_EXTERNAL_SCORE_URL_TEMPLATE`** or `sporty.external-score.url-template` |
| Broker advertise (Compose) | **`KAFKA_ADVERTISED_PUBLIC_HOST`** in **`.env`** (see `.env.example`) |

---

## API

`POST /events/status` with `eventId` (string or number) and `status` (`"live"`, `"not live"`, or boolean).

```bash
curl -X POST localhost:8090/events/status -H "Content-Type: application/json" -d "{\"eventId\":\"42\",\"status\":\"live\"}"
```

(Default HTTP port comes from **`server.port`** in **`application.properties`**, typically **8090** in this repo.)

---

## Tests

```bash
mvn test
```

Kafka is mocked in tests. WireMock is used for the HTTP client unit test.

---

## Design decisions (summary)

This section matches the homework brief: clarify trade-offs reviewers should know when reading the implementation.

**Single scheduler vs “one job per live event”.**  
The specification suggests scheduling **a** poll every ~10 s **per** live event. The prototype uses **one** `@Scheduled` task at a configurable fixed rate (**`sporty.poll-interval-ms`**, default **10000 ms**) that snapshots live event ids and invokes the score client for **each**. That keeps behavior simple under low load for a take‑home artifact: each live event is still polled roughly every interval. Trade‑off: all events execute **sequentially** in one tick, so one slow HTTP call postpones others in that tick, and coupling differs from isolated per‑event timers.

**In‑memory truth.**  
Which events are live is held in **`ConcurrentHashMap`** (`EventStateServiceImpl`). Persistence and multi‑instance correctness are explicitly out of scope for the prototype.

**External score API.**  
The score feed URL is **`sporty.external-score.url-template`** (default matches **`server.port`** and loopback **`GET /scores/{eventId}`** from **`MockScoreFeedController`**). Override the property for a separate service URL.

**Kafka publishing & retries.**  
Messages are **`ScoreMessage`** (JSON via **`JsonSerializer`**) to a configurable topic with **event id** as partition key. **Spring Retry** (**`@EnableRetry(proxyTargetClass = true)`**) plus **`@Retryable`** on **`ScoreMessagePublisher`** retries **`ExecutionException`** from **`send().get()`**; CGLIB proxies avoid JDK-proxy issues when injecting the publisher as a concrete class. Exhausted retries are logged in **`@Recover`**.

**Operational logging.**  
State transitions (“live”, “off air”) and **successful Kafka publishes** are logged at **`INFO`** (`ScoreMessagePublisher`: includes **partition/offset** when the client returns record metadata, otherwise **eventId/topic** only).

---

## AI-assisted development (disclosure)

The assignment encourages using AI tooling **provided** contributors **review**, **validate**, and **document** that output—this section fulfills the documentation part.

**What was generated vs hand-written.**  
Parts of scaffolding, refactoring, Docker/Compose snippets, **`README`** run guides, **`LiveEventPoller`** comments, **`application.properties`** / URL defaults, **`ApiExceptionHandler`**, **`MockScoreFeedController`**, and iterative fixes (Kafka listener binding, URL defaults versus **`server.port`**, **`JsonSerializer`** config WARN) originated from AI-assisted drafts. Architectural choices (single poller vs per‑event schedule, Retry on **`ExecutionException`**, acceptance tests suite) were  revised manually after reading code and **`mvn test` / runtime checks**.

**How it was verified.**  
- **`mvn test`** / **`mvn verify`** after substantive changes (controller, poller, publisher, Kafka config, REST client WireMock tests).  
- Manual runs: **`docker compose`** for Kafka, **`spring-boot:run`**, **`java -jar`**, **`curl`** for **`POST /events/status`** and inspecting logs for publishes and retries.  
- Code review passes for thread safety (**`ConcurrentHashMap`**, snapshot copy), double **`isLive`** check before Kafka publish after HTTP fetch, and alignment with **`eventId`** / **`currentScore`** JSON.

**Responsible use.**  
AI output was **edited** where it mismatched homework constraints (exact endpoint **`POST /events/status`**, payloads, **`~10 s`** poll, Kafka retries, observable logging). Errors (e.g. placeholder resolution timing, misplaced producer properties) were **fixed in code**, not pasted blindly.

---

## Notes

One `@Scheduled` tick walks all live ids. Kafka publish retries target **`ExecutionException`** from **`send().get()`**; **`@Recover`** logs after all attempts fail (see § Design decisions).
