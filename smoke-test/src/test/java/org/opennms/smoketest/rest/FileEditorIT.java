/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.rest;

import io.restassured.RestAssured;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.mina.util.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.smoketest.selenium.ResponseData;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static io.restassured.RestAssured.preemptive;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME;


/**
 * Test class for testing API of File Editor
 *
 * @author Alexander Chadfield
 */
public class FileEditorIT {
    private static final Logger LOG = LoggerFactory.getLogger(FileEditorIT.class);

    private static final String REST_FILESYSTEM = "opennms/rest/filesystem";
    private static final String FILE_NAME = "pom.xml";
    private static final String USERNAME = "editor";
    private static final String PASSWORD = "admin";

    @ClassRule
    public static final OpenNMSStack STACK = OpenNMSStack.MINIMAL;

    @Before
    public void setUp() throws InterruptedException {

        RestAssured.baseURI = STACK.opennms().getBaseUrlExternal().toString();
        RestAssured.port = STACK.opennms().getWebPort();
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);

        addUserAPI();

        Thread.sleep(5000);
    }

    /**
     * Test to verify that the normal behaviour will work
     */
    @Test
    public void normalFlow() {
        LOG.info("Normal flow test");
        RequestBody body;

        // upload a new file
        try {
            File file = new File(FILE_NAME);
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("upload", FILE_NAME,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    file))
                    .build();

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error(e.toString());
        }

        // update the file
        body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("upload", "<name>OpenNMS Smoke Test</name>")
                .build();

        try {
            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error(e.toString());
        }

        // get file
        try {
            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "GET", null);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error(e.toString());
        }

        // remove the file
        MediaType mediaType = MediaType.parse("text/plain");
        body = RequestBody.create(mediaType, "");
        try {
            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "DELETE", body);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    /**
     * Test that the File Editor will deny a file with an unsupported file extension
     */
    @Test
    public void unsupportedFileExtension() {
        LOG.info("Wrong file name test");

        // upload a new file
        try {
            File file = new File("README.md");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("upload", file.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    file))
                    .build();

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body);
            Assert.assertEquals(400, resp.code());
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    /**
     * Test that the File Editor will deny changes when invalid XML format is uploaded for an XML file
     */
    @Test
    public void updateFailsOnXmlValidation() {
        LOG.info("Wrong file name test");

        try {
            File file = new File(FILE_NAME);
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("upload", file.getName(),
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    file))
                    .build();

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + file.getName(), "POST", body);
            resp.close();

            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("upload", "OpenNMS Smoke Test")
                    .build();

            resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + file.getName(), "POST", body);

            Assert.assertEquals(400, resp.code());
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    /**
     * Create a Basic Header for the Authentication to server
     *
     * @return
     */
    private String authHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        return "Basic " + new String(encodedAuth);
    }

    /**
     * Generic method to start a request
     *
     * @param url
     * @param method
     * @param body
     * @return
     * @throws IOException
     */
    private Response postRequest(String url, String method, RequestBody body) throws IOException {
        LOG.info("creating request");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .addHeader("Authorization", authHeader(USERNAME, PASSWORD))
                .build();
        Response response = client.newCall(request).execute();

        LOG.info(response.toString());
        return response;
    }

    /**
     * Add an user over API
     */
    private void addUserAPI() {
        LOG.info("User creation request");
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JAXB.marshal(createUser("editor", "File Editor", "editor@opennms.org", "21232F297A57A5A743894A0E4A801FC3" /* admin */, "ROLE_FILESYSTEM_EDITOR", "ROLE_USER"), outputStream);

        final HttpPost post = new HttpPost(STACK.opennms().getBaseUrlExternal().toString() + "/opennms" + "/rest/users");
        post.setEntity(new StringEntity(new String(outputStream.toByteArray()), ContentType.APPLICATION_XML));
        Integer response = 0;
        try {
            response = doRequest(post);
        } catch (IOException e) {
            LOG.error(e.toString());
        } catch (InterruptedException e) {
            LOG.error(e.toString());
        }

        LOG.info(response.toString());
    }

    /**
     * API request to create a user
     * @param request
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected Integer doRequest(final HttpRequestBase request) throws IOException, InterruptedException {
        return getRequest(request).getStatus();
    }

    protected ResponseData getRequest(final HttpRequestBase request) throws ClientProtocolException, IOException, InterruptedException {
        final CountDownLatch waitForCompletion = new CountDownLatch(1);

        final URI uri = request.getURI();
        final HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD));
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        final CloseableHttpClient client = HttpClients.createDefault();

        final ResponseHandler<ResponseData> responseHandler = new ResponseHandler<ResponseData>() {
            @Override
            public ResponseData handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                try {
                    final int status = response.getStatusLine().getStatusCode();
                    String responseText = null;
                    // 400 because we return that if you try to delete
                    // something that is already deleted
                    // 404 because it's OK if it's already not there
                    if (status >= 200 && status < 300 || status == 400 || status == 404) {
                        final HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            responseText = EntityUtils.toString(entity);
                            EntityUtils.consume(entity);
                        }
                        final ResponseData r = new ResponseData(status, responseText);
                        return r;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                } catch (final Exception e) {
                    LOG.warn("Unhandled exception", e);
                    return new ResponseData(-1, null);
                } finally {
                    waitForCompletion.countDown();
                }
            }
        };

        final ResponseData result = client.execute(targetHost, request, responseHandler, context);

        waitForCompletion.await();
        client.close();
        return result;
    }

    /**
     * User object creation method
     * @param userId
     * @param username
     * @param userEmail
     * @param userPasswordHash
     * @param roles
     * @return User Object
     */
    private static OnmsUser createUser(String userId, String username, String userEmail, String userPasswordHash, String... roles) {
        final OnmsUser user = new OnmsUser();
        user.setUsername(userId);
        user.setFullName(username);
        user.setEmail(userEmail);
        user.setPassword(userPasswordHash);
        user.setPasswordSalted(false);
        user.setRoles(Arrays.asList(roles));
        return user;
    }

}

