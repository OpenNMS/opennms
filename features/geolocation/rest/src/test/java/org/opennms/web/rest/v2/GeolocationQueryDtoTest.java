package org.opennms.web.rest.v2;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeolocationQueryDtoTest {

    /**
     * see NMS-18052
     */
    @Test()
    public void testCustomDeserializer() {
        final String json = "{\n" +
                "  \"strategy\": null,\n" +
                "  \"severityFilter\": null,\n" +
                "  \"includeAcknowledgedAlarms\": \"malicious code\"\n" +
                "}";

        try {
            new ObjectMapper().readValue(json, GeolocationQueryDTO.class);
        } catch (final IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertFalse(e.getMessage().contains("malicious code"));
            assertTrue(e.getMessage().contains("Error mapping JSON to Boolean value, details omitted."));
        }
    }
}
