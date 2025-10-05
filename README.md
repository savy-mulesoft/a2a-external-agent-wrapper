# A2A External Agent Wrapper

**Make ANY external agent A2A-compliant in minutes!**

This MuleSoft application provides a universal wrapper that transforms any HTTP-based external agent into an A2A (Application-to-Application) compliant service. Simply configure your external agent details in a YAML file, run the automated playbook, and your agent is ready for A2A conversations.

## 🚀 Quick Start (3 Steps)

### 1. Configure Your External Agent
Edit `src/main/resources/external-agent-mapping.yaml` with your external agent details:

```yaml
# === Agent identity for A2A ===
agent:
  host: "http://localhost:8081"
  path: "/your-agent-path"
  name: "Your Agent Name"
  version: "1.0.0"
  description: "What your agent does"
  skills:
    - id: "1"
      name: "Your Skill 1"
      description: "Description of what this skill does"
    - id: "2"
      name: "Your Skill 2"
      description: "Description of what this skill does"

# === Your external agent cURL ===
curl:
  request: |
    curl --location 'https://your-api-endpoint.com/chat' \
        --header 'Content-Type: application/json' \
        --header 'Authorization: Bearer your-token' \
        --data '{
        "prompt": "user input goes here",
        "temperature": 0.7,
        "max_tokens": 500
        }'

# === Sample response from your agent ===
response:
  json: |
    {
        "response": {
            "text": "This is the response from your agent",
            "confidence": 0.95
        },
        "status": "success"
    }

# === Mapping (tells the wrapper where to find things) ===
mapping:
  requestUserTextPath: "prompt"           # Where to put user input in your request
  responseTextPath: "response.text"       # Where to find response text in your response
```

### 2. Run the Automated Playbook
The playbook automatically updates all the code for you:

```bash
# Simply paste the playbook content into Cursor/Claude and it will:
# - Update config.properties with your agent settings
# - Update the Mule flows with your request/response mapping
# - Update the A2A agent card with your skills
# - Ensure everything is properly configured
```

### 3. Test Your A2A Agent
```bash
mvn clean package
mvn mule:run

# Test with A2A protocol
curl --location 'http://localhost:8081/your-agent-path' \
--header 'Content-Type: application/json' \
--data '{
  "jsonrpc":"2.0",
  "id":"1",
  "method":"message/send",
  "params":{
    "message":{
      "role":"user",
      "parts":[{"kind":"text","text":"Hello, how can you help me?"}]
    }
  }
}'
```

## 🎯 What This Does

This wrapper acts as a **universal translator** between:
- **A2A Protocol** ← → **Your HTTP API**

### The Magic Happens Here:
1. **Receives A2A task** with user message
2. **Extracts user text** from A2A message structure  
3. **Transforms to your API format** using your YAML configuration
4. **Calls your external agent** via HTTP
5. **Transforms response back** to A2A format
6. **Returns A2A-compliant response**

## 🔧 What the Playbook Updates

When you run the playbook, it automatically updates these files based on your YAML configuration:

### `config.properties`
- `external.url` - Your API endpoint
- `external.method` - HTTP method (POST/GET/etc.)
- `external.timeout.ms` - Request timeout
- `external.headers.json` - HTTP headers as JSON
- `external.temperature` - AI model temperature
- `external.max_tokens` - AI model max tokens
- `agent.*` - All your agent identity settings
- `agent.skill.N.*` - All your agent skills (dynamically generated)

### `a2a-external-wrapper.xml`
- **A2A Agent Card**: Updates skills section with your exact skills from YAML
- **Request Transform**: Updates the payload builder to match your API structure
- **Response Transform**: Updates response parsing to extract text from your API response
- **HTTP Configuration**: Applies your headers and settings

### What Stays the Same
- Core A2A server configuration
- HTTP connectors and flow structure
- Logging and error handling
- A2A protocol compliance

## 📁 Project Structure

```
├── src/main/mule/
│   └── a2a-external-wrapper.xml        # Main flows (auto-updated by playbook)
├── src/main/resources/
│   ├── config.properties               # Settings (auto-updated by playbook)
│   ├── external-agent-mapping.yaml    # YOUR CONFIGURATION (edit this!)
│   ├── playbook.txt                    # Automation instructions
│   └── log4j2.xml                     # Logging config
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

## 🌟 Use Cases

- **AI Agent Integration**: Connect OpenAI, Claude, Gemini, or custom AI services
- **Legacy API Modernization**: Make old HTTP APIs A2A-compliant
- **Multi-Agent Orchestration**: Enable external services in agent conversations
- **Rapid Prototyping**: Quickly test external agents in A2A networks

## 🔍 Example Configurations

### OpenAI-style API
```yaml
curl:
  request: |
    curl --location 'https://api.openai.com/v1/chat/completions' \
        --header 'Authorization: Bearer sk-...' \
        --header 'Content-Type: application/json' \
        --data '{
        "model": "gpt-3.5-turbo",
        "messages": [{"role": "user", "content": "user input"}],
        "temperature": 0.7
        }'

mapping:
  requestUserTextPath: "messages[0].content"
  responseTextPath: "choices[0].message.content"
```

### Custom REST API
```yaml
curl:
  request: |
    curl --location 'https://your-api.com/process' \
        --header 'X-API-Key: your-key' \
        --data '{"query": "user input", "format": "text"}'

mapping:
  requestUserTextPath: "query"
  responseTextPath: "result.answer"
```

## 🚀 Advanced Features

- **Automatic Skills Generation**: Define skills in YAML, playbook creates A2A skills automatically
- **Flexible Request Mapping**: Support for any JSON structure via `requestUserTextPath`
- **Smart Response Parsing**: Extract responses from any JSON structure via `responseTextPath`
- **Header Management**: Automatic HTTP header configuration from YAML
- **Timeout Control**: Configurable request timeouts
- **Error Handling**: Built-in A2A error response formatting

## 🛠️ Development

### Prerequisites
- Java 8+
- Maven 3.6+
- MuleSoft Runtime 4.9.8+

### Making Changes
1. Edit `external-agent-mapping.yaml` with your agent configuration
2. Run the playbook (paste into Cursor/Claude)
3. Test your changes: `mvn clean package && mvn mule:run`

### Dependencies
- **A2A Connector**: 0.3.0-BETA (Agent-to-Agent protocol)
- **HTTP Connector**: 1.10.4 (External API calls)
- **Inference Connector**: 0.5.7 (AI model support)

## 📖 API Reference

### A2A Endpoint
- **URL**: `http://localhost:8081{agent.path}`
- **Method**: POST
- **Protocol**: JSON-RPC 2.0 with A2A message structure

### Agent Card Endpoint  
- **URL**: `http://localhost:8081{agent.path}/.well-known/agent.json`
- **Method**: GET
- **Returns**: Agent capabilities and skills

## 🤝 Contributing

1. Fork the repository
2. Update `external-agent-mapping.yaml` for your use case
3. Run playbook to generate code
4. Test thoroughly
5. Submit pull request with your configuration example

## 📄 License

This project is licensed under the MuleSoft license agreement.

---

**Ready to make your agent A2A-compliant?** Just edit the YAML and run the playbook! 🚀
