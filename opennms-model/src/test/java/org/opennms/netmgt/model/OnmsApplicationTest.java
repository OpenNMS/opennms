package org.opennms.netmgt.model;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.core.test.xml.XmlTest;

import java.io.IOException;


public class OnmsApplicationTest {

    @Test
    public void testMarshalXml() {
        OnmsApplication application = new OnmsApplication();
        application.setId(100);
        application.setName("Dummy");

        String applicationString = XmlTest.marshalToXmlWithJaxb(application);
        XmlTest.assertXmlEquals(
                "<application id=\"100\">\n" +
                "   <name>Dummy</name>\n" +
                "</application>\n",
                applicationString);
    }

    @Test
    public void testMarshalJson() throws IOException {
        OnmsApplication application = new OnmsApplication();
        application.setId(100);
        application.setName("Dummy");

        String applicationString = JsonTest.marshalToJson(application);
        JsonTest.assertJsonEquals("{\"name\" : \"Dummy\", \"id\" : 100}", applicationString);
    }
}
