/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
package org.opennms.features.ifttt.helper;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.core.test.MockLogAppender;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IfTttTriggerTest {
    private static final String TEST_KEY = "abc123def456";
    private static final String TEST_EVENT = "xyz";

    @Before
    public void setup() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void triggerTest() throws IOException, Exception {
        final IfTttTrigger ifTttTrigger = new IfTttTrigger();
        final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);

        when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));

        final CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);

        when(closeableHttpClient.execute(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpPost httpPost = invocationOnMock.getArgument(0);
                Assert.assertEquals("POST https://maker.ifttt.com/trigger/" + TEST_EVENT + "/with/key/" + TEST_KEY + " HTTP/1.1", httpPost.getRequestLine().toString());
                Assert.assertEquals("{\"value1\":\"abc1\",\"value2\":\"abc2\",\"value3\":\"abc3\"}", IOUtils.toString(httpPost.getEntity().getContent()));
                return closeableHttpResponse;
            }
        });

        mockStatic(HttpClients.class);
        HttpClientBuilder httpClientBuilder = mock(HttpClientBuilder.class);
        when(HttpClients.custom()).thenReturn(httpClientBuilder);
        when(httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)).thenReturn(httpClientBuilder);
        when(httpClientBuilder.setSSLContext(any())).thenReturn(httpClientBuilder);
        when(httpClientBuilder.build()).thenReturn(closeableHttpClient);

        ifTttTrigger.key(TEST_KEY).event(TEST_EVENT).value1("abc1").value2("will-be-overwritten").value2("abc2").value3("abc3").trigger();
    }
}
