package org.opennms.core.cm.rest.internal;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Test;

import com.google.common.io.Resources;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class SwaggerConverterTest {

    @Test
    public void canConvertXsd() throws IOException {
        final XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        final URL url = Resources.getResource("vacuumd-configuration-test.xsd");
        try (InputStream is = url.openStream()) {
            schemaCol.read(new StreamSource(is));
        }

        SwaggerConverter converter = new SwaggerConverter();
        OpenAPI actualApi = converter.convert(schemaCol);

        final String expectedSwaggerAsString = Resources.toString(
                Resources.getResource("swagger.vacuumd.json"), StandardCharsets.UTF_8);
        SwaggerParseResult result = new OpenAPIParser()
                .readContents(expectedSwaggerAsString, null, null);
        final OpenAPI expectedApi = result.getOpenAPI();

        assertThat(actualApi, equalTo(expectedApi));
    }
}
