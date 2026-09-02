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
package org.opennms.core.wsman.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.time.Duration;

import org.junit.Test;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.utils.CachingWSManClientFactory.SharedClient;

public class CachingWSManClientFactoryTest {

    @Test
    public void passesThroughEndpointsWithoutKerberosEncryption() throws MalformedURLException {
        WSManClientFactory delegate = mock(WSManClientFactory.class);
        WSManClient first = mock(WSManClient.class);
        WSManClient second = mock(WSManClient.class);
        when(delegate.getClient(any())).thenReturn(first, second);
        CachingWSManClientFactory factory = new CachingWSManClientFactory(delegate);

        WSManEndpoint endpoint = new WSManEndpoint.Builder("http://win.example.org:5985/wsman")
                .withBasicAuth("user", "pass")
                .build();

        // A fresh client every time, handed back unwrapped
        assertSame(first, factory.getClient(endpoint));
        assertSame(second, factory.getClient(endpoint));
        assertEquals(0, factory.size());
    }

    @Test
    public void sharesClientsForKerberosEncryptedEndpoints() throws Exception {
        WSManClientFactory delegate = mock(WSManClientFactory.class);
        WSManClient shared = mock(WSManClient.class);
        when(delegate.getClient(any())).thenReturn(shared);
        CachingWSManClientFactory factory = new CachingWSManClientFactory(delegate);

        WSManEndpoint endpoint = new WSManEndpoint.Builder("http://win.example.org:5985/wsman")
                .withKerberosEncryption()
                .build();
        // An equal endpoint built separately must hit the same cache entry
        WSManEndpoint sameEndpoint = new WSManEndpoint.Builder("http://win.example.org:5985/wsman")
                .withKerberosEncryption()
                .build();

        WSManClient a = factory.getClient(endpoint);
        WSManClient b = factory.getClient(sameEndpoint);
        assertTrue(a instanceof SharedClient);
        assertNotSame(a, b);
        assertSame(shared, ((SharedClient) a).getDelegate());
        assertSame(shared, ((SharedClient) b).getDelegate());
        verify(delegate, times(1)).getClient(any());
        assertEquals(1, factory.size());

        // Closing the handle must not close the shared client
        a.close();
        b.close();
        verify(shared, never()).close();

        // Operations go to the shared client
        a.identify();
        verify(shared, times(1)).identify();

        // Closing the factory releases it
        factory.close();
        verify(shared, times(1)).close();
        assertEquals(0, factory.size());
    }

    @Test
    public void distinguishesEndpointsByEveryField() throws Exception {
        WSManClientFactory delegate = mock(WSManClientFactory.class);
        when(delegate.getClient(any())).thenAnswer(inv -> mock(WSManClient.class));
        CachingWSManClientFactory factory = new CachingWSManClientFactory(delegate);

        factory.getClient(new WSManEndpoint.Builder("http://a.example.org:5985/wsman").withKerberosEncryption().build());
        factory.getClient(new WSManEndpoint.Builder("http://b.example.org:5985/wsman").withKerberosEncryption().build());
        factory.getClient(new WSManEndpoint.Builder("http://a.example.org:5985/wsman").withKerberosEncryption()
                .withBasicAuth("svc", "secret").build());
        factory.getClient(new WSManEndpoint.Builder("http://a.example.org:5985/wsman").withKerberosEncryption()
                .withBasicAuth("svc", "other").build());
        factory.getClient(new WSManEndpoint.Builder("http://a.example.org:5985/wsman").withKerberosEncryption()
                .withConnectionTimeout(5000).build());

        verify(delegate, times(5)).getClient(any());
        assertEquals(5, factory.size());
    }

    @Test
    public void expiresIdleClients() throws Exception {
        WSManClientFactory delegate = mock(WSManClientFactory.class);
        WSManClient shared = mock(WSManClient.class);
        when(delegate.getClient(any())).thenReturn(shared);
        CachingWSManClientFactory factory = new CachingWSManClientFactory(delegate, Duration.ofMillis(50));

        WSManEndpoint endpoint = new WSManEndpoint.Builder("http://win.example.org:5985/wsman")
                .withKerberosEncryption()
                .build();
        factory.getClient(endpoint);
        assertEquals(1, factory.size());

        Thread.sleep(200);
        assertEquals(0, factory.size());
        verify(shared, times(1)).close();
    }
}
