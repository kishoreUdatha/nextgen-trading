package com.example.common.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.Set;

public class SchemaValidator {
    private final ObjectMapper om = new ObjectMapper();
    private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    public void validate(String schemaName, String json) {
        try (InputStream is = getClass().getResourceAsStream("/schemas/" + schemaName)) {
            if (is == null) throw new RuntimeException("Schema not found: " + schemaName);
            JsonSchema schema = factory.getSchema(is);
            JsonNode node = om.readTree(json);
            Set<ValidationMessage> errors = schema.validate(node);
            if (!errors.isEmpty()) throw new RuntimeException("Schema validation failed: " + errors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
