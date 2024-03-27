/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.jvnet.hk2.annotations.Optional;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.selenium.ResponseData;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body, USERNAME, PASSWORD);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error("Upload of a new file failed. Response code: {}", e.toString());
        }

        // update the file
        body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("upload", "<name>OpenNMS Smoke Test</name>")
                .build();

        try {
            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body, USERNAME, PASSWORD);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error("Update the context of a new file failed. Response code: {}", e.toString());
        }

        // get file
        try {
            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "GET", null, USERNAME, PASSWORD);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error("Getting a list of files failed. Response code: {}}", e.toString());
        }

        // remove the file
        MediaType mediaType = MediaType.parse("text/plain");
        body = RequestBody.create(mediaType, "");
        try {
            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "DELETE", body, USERNAME, PASSWORD);
            Assert.assertEquals(200, resp.code());
        } catch (IOException e) {
            LOG.error("Removing of a file failed. Response code: {}", e.toString());
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

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body, USERNAME, PASSWORD);
            Assert.assertEquals(400, resp.code());
        } catch (IOException e) {
            LOG.error("Upload of unsupported file failed. Response code: {}", e.toString());
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

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + file.getName(), "POST", body, USERNAME, PASSWORD);
            resp.close();

            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("upload", "OpenNMS Smoke Test")
                    .build();

            resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + file.getName(), "POST", body, USERNAME, PASSWORD);

            Assert.assertEquals(400, resp.code());
        } catch (IOException e) {
            LOG.error("Update of the context with incorrect data failed. Response code: {}}", e.toString());
        }
    }

    @Test
    public void uploadFailUserWithoutFileEditorRole() {
        LOG.info("Uploading a file with incorrect role");
        RequestBody body;

        // upload a new file
        try {
            File file = new File(FILE_NAME);
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("upload", FILE_NAME,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    file))
                    .build();

            Response resp = postRequest(STACK.opennms().getBaseUrlExternal() + REST_FILESYSTEM + "/contents?f=" + FILE_NAME, "POST", body, "admin", PASSWORD);
            Assert.assertEquals(403, resp.code());
        } catch (IOException e) {
            LOG.error("Test of uploading of a new file with incorrect role failed. Response code: {}", e.toString());
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
    private Response postRequest(String url, String method, @Nullable RequestBody body, String username, String password) throws IOException {
        LOG.info("creating request");

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .addHeader("Authorization", authHeader(username, password))
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
            response = AbstractOpenNMSSeleniumHelper.doRequest(post);
        } catch (IOException | InterruptedException e) {
            LOG.debug(String.format("Adding a user failed. Response code: %s", e.toString()));
        }

        LOG.info(response.toString());
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

