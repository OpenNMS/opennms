package org.opennms.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.core.test.annotations.Webapp;
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
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/test.html");
        HttpResponse response = client.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        LogUtils.debugf(this, "got response:\n%s", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("Purple monkey dishwasher."));
    }

    @Test
    @JUnitHttpServer(port=9162, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testWebapp() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/index.html");
        HttpResponse response = client.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        LogUtils.debugf(this, "got response:\n%s", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("This is a webapp."));
    }

    @Test
    @JUnitHttpServer(port=9162, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testWebappWithServlet() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        HttpResponse response = client.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        System.err.println("got response" + responseString);
        LogUtils.debugf(this, "got response:\n%s", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("You are reading this from a servlet!"));
    }

    @Test
    @JUnitHttpServer(port=9162, basicAuth=true, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testBasicAuthSuccess() throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        
        CredentialsProvider cp = client.getCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "istrator");
        cp.setCredentials(new AuthScope("localhost", 9162), credentials);
        
        HttpResponse response = client.execute(method);
        String responseString = EntityUtils.toString(response.getEntity());
        System.err.println("got response " + responseString);
        LogUtils.debugf(this, "got response:\n%s", responseString);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(responseString.contains("You are reading this from a servlet!"));
    }

    @Test
    @JUnitHttpServer(port=9162, basicAuth=true, webapps={
            @Webapp(context="/testContext", path="src/test/resources/test-webapp")
    })
    public void testBasicAuthFailure() throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpUriRequest method = new HttpGet("http://localhost:9162/testContext/monkey");
        
        CredentialsProvider cp = client.getCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "sucks");
        cp.setCredentials(new AuthScope("localhost", 9162), credentials);
        
        HttpResponse response = client.execute(method);
        assertEquals(401, response.getStatusLine().getStatusCode());
    }

}
