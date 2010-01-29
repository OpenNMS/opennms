package org.opennms.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.core.utils.LogUtils;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitHttpServerExecutionListener.class
})
public class JUnitHttpServerTest {

    @Test
    @JUnitHttpServer(port=9162)
    public void testServer() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod("http://localhost:9162/test.html");
        int response = client.executeMethod(method);
        LogUtils.debugf(this, "got response:\n%s", method.getResponseBodyAsString());
        assertEquals(200, response);
        assertTrue(method.getResponseBodyAsString().contains("Purple monkey dishwasher."));
    }
}
