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
package org.opennms.netmgt.provision.service.requisition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.LocationAwareRequisitionClient;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;

public class RequisitionUrlConnectionTest {

    @Before
    public void setUp() {
        GenericURLFactory.initialize();
    }

    @Test
    public void canRetrieveRequisition() throws Exception {
        Requisition expectedRequisition = new Requisition();
        LocationAwareRequisitionClient client = mock(LocationAwareRequisitionClient.class, RETURNS_DEEP_STUBS);
        when(client.requisition()
                .withRequisitionProviderType("test")
                .withParameters(any())
                .execute()
                .get()).thenReturn(expectedRequisition);

        try {
            RequisitionUrlConnection.setClient(client);
            final String requisitionAsStr = urlToString("requisition://test/");
            Requisition actualRequisition = JaxbUtils.unmarshal(Requisition.class, requisitionAsStr);
            assertEquals(expectedRequisition, actualRequisition);
        } finally {
            RequisitionUrlConnection.setClient(null);
        }
    }

    @Test
    public void canParseParameters() throws MalformedURLException {
        final URL url = new URL("requisition://user:pass@test:42/some/deep/path?location=abc&parm1=1");
        Map<String, String> params = RequisitionUrlConnection.getParameters(url);
        assertThat(params,
                Matchers.<Map<String, String>>equalTo(new ImmutableMap.Builder<String, String>()
                        .put("path", "/some/deep/path")
                        .put("location", "abc")
                        .put("parm1", "1")
                        .put("type", "test")
                        .put("username", "user")
                        .put("password", "pass")
                        .build()
                ));
    }

    /**
     * Since the URLs often appear in XML documents, we've added support
     * for delimiting the parameters using semi-colons (;) in addition
     * to ampersands (&).
     *
     * @throws MalformedURLException
     */
    @Test
    public void canParseSemicolonDelimitedParameters() throws MalformedURLException {
        final URL url = new URL("requisition://type/path?k1=v1;k2=v2&k3=v3&k4=v4");
        Map<String, String> params = RequisitionUrlConnection.getParameters(url);
        assertThat(params,
                Matchers.<Map<String, String>>equalTo(new ImmutableMap.Builder<String, String>()
                        .put("type", "type")
                        .put("path", "/path")
                        .put("k1", "v1")
                        .put("k2", "v2")
                        .put("k3", "v3")
                        .put("k4", "v4")
                        .build()
                ));
    }

    public static String urlToString(String urlAsStr) {
        try {
            final URL url = new URL(urlAsStr);
            try (final InputStream is = url.openStream();
                 final InputStreamReader isr = new InputStreamReader(is)) {
                return CharStreams.toString(isr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
