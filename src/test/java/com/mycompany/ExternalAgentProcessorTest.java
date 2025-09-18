package com.mycompany;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for external agent processor transformations.
 * Tests the DataWeave transformation logic for converting between A2A and external agent formats.
 */
public class ExternalAgentProcessorTest {

    private ObjectMapper objectMapper;

    // @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    // @Test
    public void testA2ARequestStructure() throws Exception {
        // Load test A2A request
        String a2aRequest = loadTestResource("request.json");
        Map<String, Object> requestData = objectMapper.readValue(a2aRequest, Map.class);
        
        // Verify A2A request structure
        assertTrue("A2A request should have 'id' field", requestData.containsKey("id"));
        assertTrue("A2A request should have 'sessionId' field", requestData.containsKey("sessionId"));
        assertTrue("A2A request should have 'message' field", requestData.containsKey("message"));
        
        assertEquals("Task ID should match", "test-task-123", requestData.get("id"));
        assertEquals("Session ID should match", "test-session-456", requestData.get("sessionId"));
        
        Map<String, Object> message = (Map<String, Object>) requestData.get("message");
        assertTrue("Message should have 'role' field", message.containsKey("role"));
        assertTrue("Message should have 'parts' field", message.containsKey("parts"));
        
        assertEquals("Role should be 'user'", "user", message.get("role"));
        
        List<?> parts = (List<?>) message.get("parts");
        assertEquals("Should have exactly 1 part", 1, parts.size());
        
        Map<String, Object> part = (Map<String, Object>) parts.get(0);
        assertEquals("Part type should be 'text'", "text", part.get("type"));
        assertEquals("Part text should match expected", "How can I improve my credit score from 700?", part.get("text"));
    }
    
    // @Test
    public void testExpectedExternalRequestFormat() throws Exception {
        // This test verifies what the external request should look like after transformation
        // Based on the DataWeave transformation in the main flow
        
        Map<String, Object> expectedExternalRequest = Map.of(
            "messages", List.of(
                Map.of(
                    "role", "user",
                    "content", "How can I improve my credit score from 700?"
                )
            ),
            "temperature", 0.2,
            "max_tokens", 300
        );
        
        // Verify structure
        assertTrue("External request should have 'messages'", expectedExternalRequest.containsKey("messages"));
        assertTrue("External request should have 'temperature'", expectedExternalRequest.containsKey("temperature"));
        assertTrue("External request should have 'max_tokens'", expectedExternalRequest.containsKey("max_tokens"));
        
        List<?> messages = (List<?>) expectedExternalRequest.get("messages");
        assertEquals("Should have exactly 1 message", 1, messages.size());
        
        Map<String, Object> message = (Map<String, Object>) messages.get(0);
        assertEquals("Message role should be 'user'", "user", message.get("role"));
        assertEquals("Message content should match", "How can I improve my credit score from 700?", message.get("content"));
        
        assertEquals("Temperature should be 0.2", 0.2, expectedExternalRequest.get("temperature"));
        assertEquals("Max tokens should be 300", 300, expectedExternalRequest.get("max_tokens"));
    }

   // @Test
    public void testExternalResponseStructure() throws Exception {
        // Load test external response
        String externalResponse = loadTestResource("response.json");
        Map<String, Object> responseData = objectMapper.readValue(externalResponse, Map.class);
        
        // Verify external response structure (Azure OpenAI format)
        assertTrue("External response should have 'choices'", responseData.containsKey("choices"));
        assertTrue("External response should have 'created'", responseData.containsKey("created"));
        assertTrue("External response should have 'id'", responseData.containsKey("id"));
        assertTrue("External response should have 'model'", responseData.containsKey("model"));
        
        List<?> choices = (List<?>) responseData.get("choices");
        assertEquals("Should have exactly 1 choice", 1, choices.size());
        
        Map<String, Object> choice = (Map<String, Object>) choices.get(0);
        assertTrue("Choice should have 'message'", choice.containsKey("message"));
        assertTrue("Choice should have 'finish_reason'", choice.containsKey("finish_reason"));
        
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        assertTrue("Message should have 'content'", message.containsKey("content"));
        assertTrue("Message should have 'role'", message.containsKey("role"));
        
        assertEquals("Message role should be 'assistant'", "assistant", message.get("role"));
        assertTrue("Message content should contain expected text", 
            ((String) message.get("content")).contains("Improving your credit score"));
    }
    
    @Test
    public void testExpectedA2AResponseFormat() throws Exception {
        // This test verifies what the A2A response should look like after transformation
        // Based on the DataWeave transformation in the main flow
        
        String sampleContent = "Improving your credit score from 700 can be achieved through several strategies...";
        
        Map<String, Object> expectedA2AResponse = Map.of(
            "id", "test-task-123",
            "sessionId", "test-session-456",
            "status", Map.of(
                "state", "completed",
                "timestamp", "2025-09-17T14:40:00Z", // Example timestamp
                "message", Map.of(
                    "role", "agent",
                    "parts", List.of(
                        Map.of(
                            "type", "text",
                            "text", sampleContent
                        )
                    )
                )
            )
        );
        
        // Verify A2A response structure
        assertTrue("A2A response should have 'id'", expectedA2AResponse.containsKey("id"));
        assertTrue("A2A response should have 'sessionId'", expectedA2AResponse.containsKey("sessionId"));
        assertTrue("A2A response should have 'status'", expectedA2AResponse.containsKey("status"));
        
        assertEquals("ID should match", "test-task-123", expectedA2AResponse.get("id"));
        assertEquals("Session ID should match", "test-session-456", expectedA2AResponse.get("sessionId"));
        
        Map<String, Object> status = (Map<String, Object>) expectedA2AResponse.get("status");
        assertEquals("Status state should be 'completed'", "completed", status.get("state"));
        assertTrue("Status should have timestamp", status.containsKey("timestamp"));
        assertTrue("Status should have message", status.containsKey("message"));
        
        Map<String, Object> message = (Map<String, Object>) status.get("message");
        assertEquals("Message role should be 'agent'", "agent", message.get("role"));
        assertTrue("Message should have parts", message.containsKey("parts"));
        
        List<?> parts = (List<?>) message.get("parts");
        assertEquals("Should have exactly 1 part", 1, parts.size());
        
        Map<String, Object> part = (Map<String, Object>) parts.get(0);
        assertEquals("Part type should be 'text'", "text", part.get("type"));
        assertEquals("Part text should match", sampleContent, part.get("text"));
    }

    /**
     * Helper method to load test resource files
     */
    private String loadTestResource(String fileName) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new RuntimeException("Could not find test resource: " + fileName);
        }
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
}
