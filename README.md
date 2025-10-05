# A2A External Agent Wrapper

A MuleSoft application that provides an A2A (Application-to-Application) compliant wrapper for integrating external agents via HTTP. This wrapper enables external AI agents or services to participate in A2A conversations by translating between A2A protocol and standard HTTP/JSON APIs.

## What This Project Does

This application acts as a bridge between:
- **A2A Protocol**: MuleSoft's Agent-to-Agent communication standard
- **External HTTP APIs**: Any external agent or AI service that accepts HTTP requests

### Key Features

- **A2A Server**: Exposes an A2A-compliant endpoint that can receive tasks from other A2A agents
- **HTTP Translation**: Converts A2A messages to HTTP requests for external services
- **Response Mapping**: Transforms external service responses back to A2A format
- **Agent Card**: Publishes agent capabilities via `.well-known/agent.json` endpoint
- **Configurable Mapping**: Easy configuration of request/response transformations

## How It Works

1. **Receives A2A Task**: The application listens for A2A tasks containing user messages
2. **Extracts User Input**: Parses the user's text from the A2A message structure
3. **Calls External Agent**: Makes HTTP request to configured external service
4. **Returns A2A Response**: Transforms the external response into A2A-compliant format

## Project Structure

```
├── src/main/mule/
│   └── a2a-external-wrapper.xml     # Main Mule flow configuration
├── src/main/resources/
│   ├── config.properties            # Application configuration
│   ├── external-agent-mapping.yaml # External agent configuration
│   └── log4j2.xml                  # Logging configuration
├── pom.xml                          # Maven project configuration
└── README.md                        # This file
```

## Configuration

### Agent Configuration (`external-agent-mapping.yaml`)

Configure your external agent details:

```yaml
# Agent identity for A2A
agent:
  host: "http://localhost:8081"
  path: "/external-azure-fins"
  name: "ACME FINS Agent"
  version: "1.0.0"
  description: "ACME Financial Services Agent"

# External service cURL example
curl:
  request: |
    curl --location 'https://your-external-service.com/chat' \
        --header 'Content-Type: application/json' \
        --data '{"prompt": "user input here"}'

# Sample response from external service
response:
  json: |
    {
      "agent_response": {
        "content": "Response text from external agent"
      }
    }

# Mapping configuration
mapping:
  requestUserTextPath: "prompt"
  responseTextPath: "agent_response.content"
```

### Application Properties (`config.properties`)

```properties
# HTTP listener configuration
http.listener.port=8081

# External service configuration
external.url=https://your-external-service.com/chat
external.timeout.ms=30000

# Agent configuration
agent.host=http://localhost:8081
agent.path=/external-azure-fins
agent.name=ACME FINS Agent
agent.version=1.0.0
agent.description=ACME Financial Services Agent
```

## Running the Application

### Prerequisites
- Java 8+
- Maven 3.6+
- MuleSoft Runtime 4.9.8+

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd a2a-external-wrapper
   ```

2. **Configure your external agent**
   - Update `src/main/resources/external-agent-mapping.yaml`
   - Update `src/main/resources/config.properties`

3. **Run the application**
   ```bash
   mvn clean package
   mvn mule:run
   ```

4. **Test the integration**
   ```bash
   curl --location 'http://localhost:8081/external-azure-fins' \
   --header 'Content-Type: application/json' \
   --data '{
     "jsonrpc":"2.0",
     "id":"1",
     "method":"message/send",
     "params":{
       "message":{
         "role":"user",
         "parts":[{"kind":"text","text":"How do I improve my credit score?"}]
       }
     }
   }'
   ```

## API Endpoints

### A2A Task Endpoint
- **URL**: `http://localhost:8081/external-azure-fins`
- **Method**: POST
- **Content-Type**: application/json
- **Protocol**: JSON-RPC 2.0 with A2A message structure

### Agent Card Endpoint
- **URL**: `http://localhost:8081/external-azure-fins/.well-known/agent.json`
- **Method**: GET
- **Returns**: Agent capabilities and metadata

## Example Response

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "id": "task-123",
    "sessionId": "session-456",
    "status": {
      "state": "completed",
      "timestamp": "2025-01-05T18:19:33Z",
      "message": {
        "role": "agent",
        "parts": [
          {
            "kind": "text",
            "text": "Response from your external agent"
          }
        ]
      }
    }
  }
}
```

## Use Cases

- **AI Agent Integration**: Connect external AI services to A2A networks
- **Legacy System Bridging**: Expose existing HTTP APIs as A2A agents
- **Multi-Agent Orchestration**: Enable external services to participate in agent conversations
- **Protocol Translation**: Bridge different communication protocols

## Dependencies

- **MuleSoft Runtime**: 4.9.8
- **A2A Connector**: 0.3.0-BETA
- **HTTP Connector**: 1.10.4
- **Inference Connector**: 0.5.7

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MuleSoft license agreement.
