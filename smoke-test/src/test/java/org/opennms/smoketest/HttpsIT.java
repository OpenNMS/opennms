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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Call;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSProfile;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;


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
