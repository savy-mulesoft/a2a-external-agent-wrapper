# A2A External Wrapper — Template & Deployment Guide

This is a **MuleSoft A2A External Wrapper template** that provides **two supported ways** to keep the wrapper in sync with an external agent: a **Manual** approach and a **Cursor‑driven** approach. Both achieve the same end state: **A2A in → external HTTP → A2A out**, with **no dependency on external schemas** (shapes are inferred from YAML samples).

## 🤝 Contribution

A. Via Git

[https://github.com/savy-mulesoft/a2a-external-agent-wrapper](https://github.com/savy-mulesoft/a2a-external-agent-wrapper)

You can contribute to this repo by creating another branch in this repo for the new external agent you are onboarding.

Eg branch name -    Azure_Fins_Savy

And add details in readme so the team can benefit from it.

Note - Please don't push any api key in the repo

B. Via Exchange

When publishing your agent template to exchange please tag it with **"a2a-external-wrapper"** , to logically group the external agents for reuse

## 🚀 Quick Start

1. Search for "**A2A External Agent Wrapper Template**" in Anypoint Exchange
2. Download the template
3. Follow the configuration steps below

### 📋 Prerequisites

- MuleSoft Anypoint Platform account
- MuleSoft Runtime 4.9.9+
- Java 17+
- Maven 3.6+

## 🔧 Customization

The wrapper is designed to be easily customizable for different external agents using 2 different approaches

## What stays true in both approaches

- **A2A boundaries are deterministic**
    - **Build Payload** input is A2A (role + first text part, plus temperature/max_tokens from properties).
    - **Build Response** output is A2A (minimal success or failure envelope).
- **No external schema usage** — outbound/inbound mapping is **inferred** from samples provided in `external-agent-mapping.yaml` (e.g., `sampleCurl`, `sampleRequest`, `sampleResponse`). If no usable sample is present, fall back to a minimal OpenAI‑style JSON.
- **Skills parity** — the count of `<a2a:agent-skill>` nodes in `a2a-external-wrapper.xml` **must exactly match** the number of `agent.skill.N.*` groups in `config.properties` (no gaps, no extras).
- **Headers are one JSON string** — all external headers are carried in a single property key `external.headers.json` and parsed at runtime once:#[read(p("external.headers.json") default "{}", "application/json") as Object]

------

## Approach 1 — Manual Update

### Files you will edit

- `src/main/resources/config.properties`
- `src/main/mule/a2a-external-wrapper.xml`
- DataWeave transforms inside your flows for:
    - **Build Payload** (A2A → external request)
    - **Build Response** (external response → A2A)

### Step‑by‑step

1. **Review YAML samples**

    - Open `src/main/resources/external-agent-mapping.yaml` and note:
        - External **URL/method/timeout/headers**
        - Any `sampleCurl`**/**`sampleRequest`**/**`sampleResponse` blocks to infer shapes
        - Agent **metadata** and **skills**

2. **Update **`config.properties`

    - External config (create/overwrite if missing):external.url=external.method=external.timeout.ms=external.headers.json=external.temperature=external.max_tokens=
    - Agent card config:agent.host=agent.path=agent.name=agent.version=agent.description=
    - **Agent skills (strict 1..N)**agent.skill.1.id=...agent.skill.1.name=...agent.skill.1.description=...agent.skill.2.id=......
        - If YAML has **fewer** skills than your properties, **delete** extras.
        - If YAML has **more**, **add** the missing ones.
        - **No gaps** in numbering: must be contiguous `1..N`.

3. **Update **`<a2a:agent-skill>`** nodes in XML**

    - In `src/main/mule/a2a-external-wrapper.xml`, **replace** the skills list with **exactly one** static `<a2a:agent-skill>` node per `agent.skill.N.*` set from properties (no expressions in attributes). Example:  <![CDATA[Customer support for transaction issues]]>

4. **Edit the two transformers**

    - **Build Payload** (A2A → external JSON):
        - role ← `payload.message.role` (default `"user"`)
        - text ← first `payload.message.parts` where `'type' == "text"`
        - temperature ← `${external.temperature}`
        - max_tokens ← `${external.max_tokens}`
        - **Infer** outbound JSON shape from YAML **samples**; if none, use minimal OpenAI‑style:{"messages": [{ "role": "", "content": "" }],"temperature": ,"max_tokens": }
        - **Tip:** Quote `'type'` when building parts in DW objects.
    - **Build Response** (external → A2A):
        - If `treatsBodyAsJson` is true (default), parse as JSON; else treat as plain text.
        - **Infer** the reply path using YAML **sampleResponse**:
            - If OpenAI‑like → `choices[0].message.content`
            - Else try: `message.content`, `content`, `output`, `answer`, `data.result`, etc.
            - If not found → return **A2A FAILED** with a short reason.
        - Emit minimal A2A success envelope with `status.state="completed"` and one text part.

5. **Validate**

    - Build passes, DW compiles (including quoted `'type'`).
    - Skills count in XML **equals** properties’ skills count.
    - HTTP uses `${external.*}` and parses headers from `external.headers.json`.
    - Manual smoke test with a sample A2A payload.

------

## Approach 2 — Cursor‑Driven Update

**Edit only one file:** `src/main/resources/external-agent-mapping.yaml`

Cursor reads your update instructions from `cursor-update-from-yaml.txt` and applies changes to both code and properties automatically.

### What you edit

- `external-agent-mapping.yaml` — provide/adjust:
    - `external.url`, `external.method`, `external.timeoutMs`, `external.headers`
    - `request.modelParams.temperature`, `request.modelParams.max_tokens`
    - **Samples**: `sampleCurl` / `sampleRequest` / `sampleResponse` (the more precise, the better)
    - `agent.host`, `agent.path`, `agent.name`, `agent.version`, `agent.description`
    - `agent.skills` list (authoritative for skills parity)

### What Cursor updates for you

- `config.properties`:
    - External keys (`external.*`) and `external.headers.json` (minified single‑line JSON)
    - Agent keys (`agent.*`) and **skills** (`agent.skill.1.* … agent.skill.N.*`) with strict 1..N
- `a2a-external-wrapper.xml`:
    - Rewrites the `<a2a:agent-skill>` nodes to **exactly match** the skills count
    - Keeps `<a2a:server-config>` using `${agent.*}` placeholders
- **Transformers**:
    - Updates **Build Payload** and **Build Response** by **inferring** JSON shapes from YAML samples (no external schema)

### Example Cursor prompt

Copy‑paste this into Cursor’s chat for the repo/workspace:

```
Ask Cursor to execute instruction in the playbook.txt in `src/main/resources`.
When done, show a brief diff summary and any assumptions you made.
```

### Validation (Cursor approach)

- After Cursor runs, confirm:
    - Skills parity: XML count === properties count === YAML skills length
    - Properties include `external.headers.json` as **single‑line JSON**
    - Build & DW compile cleanly; run a quick test

------

## Choosing an approach

| Criterion | Manual | Cursor‑Driven |
|---|---|---|
| Time to update | Slower | Faster (one file + one prompt) |
| Control/granularity | Highest | High, guided by playbook |
| Risk of drift | Medium (human error) | Low (automated parity rules) |
| Prereqs | Mule/DW familiarity | A good YAML with samples |


------

## Unit Testing

### External Agent Processor Tests

The project includes comprehensive unit tests for the DataWeave transformations that convert between A2A and external agent formats.

#### Test Files

1. `src/test/resources/request.json` - Sample A2A request payload
2. `src/test/resources/response.json` - Sample external agent response payload
3. `src/test/java/com/mycompany/ExternalAgentProcessorTest.java` - JUnit test class

#### What the Tests Verify

1. **A2A Request Structure Validation**

    - Verifies A2A request has required fields (id, sessionId, message)
    - Validates message structure with role and parts
    - Confirms text extraction from message parts

2. **Expected External Request Format**

    - Validates the structure that should be sent to external agent
    - Verifies OpenAI-style format with messages, temperature, max_tokens
    - Confirms proper role mapping ("user") and content extraction

3. **External Response Structure Validation**

    - Verifies external agent response format (Azure OpenAI structure)
    - Validates choices array with message content
    - Confirms assistant role and content extraction

4. **Expected A2A Response Format**

    - Validates the A2A response structure after transformation
    - Verifies status object with state "completed" and timestamp
    - Confirms proper message parts structure with agent role

#### Running the Tests

```bash
# Run all tests
mvn test

# Run only the external agent processor tests
mvn test -Dtest=ExternalAgentProcessorTest

# Run tests with verbose output
mvn test -Dtest=ExternalAgentProcessorTest -X
```

#### Customizing Test Data

To test with your specific external agent:

1. **Update **`src/test/resources/request.json` with a sample A2A request:{"id": "test-task-123","sessionId": "test-session-456","message": { "role": "user", "parts": [   { "type": "text",
 "text": "Your test question here"   } ]}}
2. **Update **`src/test/resources/response.json` with your external agent's actual response format:{"choices": [ {   "message": { "content": "Your agent's response format here"   } }]}
3. **Run the tests** to verify your transformations work correctly

#### Test Coverage

The tests ensure that:

- ✅ A2A requests are properly transformed to external agent format
- ✅ External agent responses are properly transformed to A2A format
- ✅ Required fields are present in both transformations
- ✅ Data types and structures match expected formats
- ✅ Error handling for missing or malformed data

## 🚀 Deployment

### Deploy to CloudHub

```bash
mvn clean package
# Deploy via Anypoint Platform or use Maven plugin
```

### Deploy to Runtime Fabric

```bash
mvn clean package
# Deploy the generated JAR to your Runtime Fabric
```

### Local Development

```bash
mvn clean package
java -jar target/a2a-external-wrapper-1.0.0-mule-application.jar
```

## 📖 Usage

### A2A Endpoint

- **URL**: `http://localhost:8082/external-azure-fins` (configurable)
- **Method**: POST
- **Content-Type**: application/json

### Sample A2A Request

```json
{
  "id": "task-123",
  "sessionId": "session-456",
  "message": {
    "role": "user",
    "parts": [
      {
        "type": "text",
        "text": "How can I improve my credit score?"
      }
    ]
  }
}
```

### Sample A2A Response

```json
{
  "id": "task-123",
  "sessionId": "session-456",
  "status": {
    "state": "completed",
    "timestamp": "2025-09-18T00:30:00Z",
    "message": {
      "role": "agent",
      "parts": [
        {
          "type": "text",
          "text": "Here are some ways to improve your credit score..."
        }
      ]
    }
  }
}
```

------

## Common pitfalls & fixes

- **Mismatch in skills count** → Ensure YAML skills length == number of `agent.skill.N.*` groups == number of `<a2a:agent-skill>` nodes.
- **Unquoted **`type`** key in DW** → Use `'type'` in object literals.
- **Headers not applied** → Verify `external.headers.json` is valid single‑line JSON and is parsed via `read(...)`.
- **No usable sample in YAML** → Transformers should fall back to the minimal OpenAI‑style request and a generic path search for the reply (then fail gracefully if not found).

------

**That’s it.** Pick your path, update the files, validate, and you’re good.
