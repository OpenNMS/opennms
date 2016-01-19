package org.opennms.core.test.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.json.JSONException;
import org.junit.Assert;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class JsonTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonTest.class);

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    static {
        final AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        DEFAULT_OBJECT_MAPPER.getDeserializationConfig().withAnnotationIntrospector(introspector);
        DEFAULT_OBJECT_MAPPER.getSerializationConfig().withAnnotationIntrospector(introspector);
    }

    private JsonTest(){}

    public static <T> String marshalToJson(T object) throws IOException {
        LOG.debug("Reference Object: {}", object);
        String json = DEFAULT_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        LOG.debug("JSON: {}", json);
        return json;
    }

    public static <T> T unmarshalFromJson(String json, Class<T> expectedResultType) throws IOException {
        return DEFAULT_OBJECT_MAPPER.readValue(json, expectedResultType);
    }

    // reads all data from the given input stream
    public static String read(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copy(inputStream, output);
            return output.toString();
        }
    }

    public static void assertJsonEquals(String expected, String actual) {
        try {
            JSONAssert.assertEquals(expected, actual, true);
        } catch (JSONException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
