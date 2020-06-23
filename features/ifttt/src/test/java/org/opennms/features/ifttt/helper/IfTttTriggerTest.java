/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.core.test.MockLogAppender;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClients.class)
public class IfTttTriggerTest {
    private static final String TEST_KEY = "abc123def456";
    private static final String TEST_EVENT = "xyz";

    @Before
    public void setup() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void triggerTest() throws IOException {
        final IfTttTrigger ifTttTrigger = new IfTttTrigger();
        final CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);

        when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK"));

        final CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);

        when(closeableHttpClient.execute(Matchers.anyObject())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                HttpPost httpPost = invocationOnMock.getArgumentAt(0, HttpPost.class);
                Assert.assertEquals("POST https://maker.ifttt.com/trigger/" + TEST_EVENT + "/with/key/" + TEST_KEY + " HTTP/1.1", httpPost.getRequestLine().toString());
                Assert.assertEquals("{\"value1\":\"abc1\",\"value2\":\"abc2\",\"value3\":\"abc3\"}", IOUtils.toString(httpPost.getEntity().getContent()));
                return closeableHttpResponse;
            }
        });

        mockStatic(HttpClients.class);
        when(HttpClients.createDefault()).thenReturn(closeableHttpClient);

        ifTttTrigger.key(TEST_KEY).event(TEST_EVENT).value1("abc1").value2("will-be-overwritten").value2("abc2").value3("abc3").trigger();
    }
}
