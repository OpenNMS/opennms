package org.opennms.smoketest;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.RestClient;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.preemptive;
import static org.junit.Assert.assertEquals;

public class EventConfRestIT {

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private RestClient restClient;


    @Before
    public void setUp() {
        RestAssured.baseURI = stack.opennms().getBaseUrlExternal().toString();
        RestAssured.port = stack.opennms().getWebPort();
        RestAssured.basePath = "/opennms";
        RestAssured.authentication = preemptive()
                .basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME,
                        AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD);
        restClient = stack.opennms().getRestClient();
    }
    @Test
    public void testEventConfUpload() throws IOException {
        String resourcePath = "/EVENT-CONF/3Com.events.xml";
        File tempFile = copyResourceToTempFile(resourcePath);
        try {
            Response response = restClient.uploadEventConfFile(tempFile);
            assertEquals(200, response.getStatus());
        } finally {
            tempFile.deleteOnExit();
        }
    }

    private static File copyResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream is = TestEventConf.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            File tempFile = File.createTempFile("eventconf-", ".xml");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                is.transferTo(fos);
            }
            return tempFile;
        }
    }

}
