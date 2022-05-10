/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import io.restassured.RestAssured;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;


public class HttpsIT {

    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSContainer.class);

    private final String urlOverHttps = "https://" + STACK.opennms().getHost() + ":" + STACK.opennms().getSSLPort();

    @ClassRule
    public static final OpenNMSStack STACK = OpenNMSStack.withModel(StackModel.newBuilder()
            .withOpenNMS(OpenNMSProfile.newBuilder()
                    .withFile("jetty.keystore", "etc/jetty.keystore")
                    .withFile("jetty.xml", "etc/jetty.xml")
                    .withFile("https.properties", "etc/opennms.properties.d/https.properties")
                    .build())
            .build());

    @Before
    public void setUp() {
        LOG.info("Set up of the test");
        RestAssured.baseURI = STACK.opennms().getBaseUrlExternal().toString();
        RestAssured.port = STACK.opennms().getWebPort();
        RestAssured.basePath = "/opennms/";
    }


    @After
    public void tearDown() {
        LOG.info("Test tear down");
    }

    /**
     * This test will open the page over HTTPS accepting any self-signed certificate which will allow us to verify the page is opened.
     * Test will confirm response 200, Username and Password fields.
     */
    @Test
    public void verifyHTTPSConnection() {
        LOG.info("Verify that the test itself works fine. Empty body.");

        try {
            // This TrustStrategy will make sure that we do not care about Self Signed Cert (true)
            TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
            LOG.info("Trust Strategy is ready");

            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            LOG.info("Before Registry");
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslsf)
                            .register("http", new PlainConnectionSocketFactory())
                            .build();

            LOG.info("Before BasicHTTP");
            BasicHttpClientConnectionManager connectionManager =
                    new BasicHttpClientConnectionManager(socketFactoryRegistry);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
                    .setConnectionManager(connectionManager).build();

            LOG.info("Before getting a response");
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            LOG.info("Getting response from the server");
            ResponseEntity<String> response = new RestTemplate(requestFactory)
                    .exchange(urlOverHttps, HttpMethod.GET, null, String.class);

            LOG.info("Before assert");
            Assert.assertEquals(response.getStatusCode().value(), 200);
            Assert.assertTrue(response.getBody().contains("Username"));
            Assert.assertTrue(response.getBody().contains("Password"));

            LOG.info("we have reached the end of the test");

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOG.error(e.toString());
        }

    }

    //@Test
    public void verifyHttp() {
        String json  = given()
                .auth().basic(AbstractOpenNMSSeleniumHelper.BASIC_AUTH_USERNAME, AbstractOpenNMSSeleniumHelper.BASIC_AUTH_PASSWORD)
                .get("rest/info")
                .then().statusCode(200)
                .extract().response().body().print();

        LOG.info(json.toString());
    }

    //@Test
    public void simpleCode() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:8980").get().build();
        okhttp3.Response resp = client.newCall(request).execute();
        LOG.info(resp.body().toString());
    }

    //@Test
    public void simpleHTTPSCode() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://localhost:8443").get().build();
        okhttp3.Response resp = client.newCall(request).execute();
        LOG.info(resp.body().toString());
    }

    @Test
    public void okHttpTest() throws KeyManagementException, NoSuchAlgorithmException, IOException {
        OkHttpClient client = new OkHttpClient();
        OkHttpClient.Builder clientBuilder = client.newBuilder().readTimeout(6000, TimeUnit.SECONDS);

        LOG.info("**** Allow untrusted SSL connection ****");
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] cArrr = new X509Certificate[0];
                return cArrr;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) throws CertificateException {
            }
        }};

        SSLContext sslContext = SSLContext.getInstance("SSL");

        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        clientBuilder.hostnameVerifier(hostnameVerifier);

        LOG.info("Before getting a request");
        Request request = new Request.Builder().url(urlOverHttps).get().build();

        final Call call = clientBuilder.build().newCall(request);
        LOG.info("Before getting a response");
        okhttp3.Response response = call.execute().networkResponse();

        LOG.info("Before assert");
        Assert.assertEquals(response.code(), 200);

        LOG.info("we have reached the end of the test");
    }

}
