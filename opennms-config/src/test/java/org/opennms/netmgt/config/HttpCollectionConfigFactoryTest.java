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
package org.opennms.netmgt.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.dao.api.HttpCollectionConfigDao;
import org.opennms.netmgt.rrd.RrdRepository;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class HttpCollectionConfigFactoryTest {

    @Test
    public void testHttpCollectionConfigFactoryReader() throws IOException {
        InputStream rdr = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<http-datacollection-config  \n" + 
                "    xmlns:http-dc=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" \n" + 
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
                "    xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/http-datacollection http://www.opennms.org/xsd/config/http-datacollection-config.xsd\" \n" + 
                "    rrdRepository=\"${install.share.dir}/rrd/snmp/\" >\n" + 
                "  <http-collection name=\"default\">\n" + 
                "    <rrd step=\"300\">\n" + 
                "      <rra>RRA:AVERAGE:0.5:1:8928</rra>\n" + 
                "      <rra>RRA:AVERAGE:0.5:12:8784</rra>\n" + 
                "      <rra>RRA:MIN:0.5:12:8784</rra>\n" + 
                "      <rra>RRA:MAX:0.5:12:8784</rra>\n" + 
                "    </rrd>\n" + 
                "    <uris>\n" + 
                "      <uri name=\"test-document-count\">\n" + 
                "        <url path=\"/test/resources/httpcolltest.html\"\n" + 
                "             user-agent=\"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/412 (KHTML, like Gecko) Safari/412\" \n" + 
                "             matches=\".*([0-9]+).*\" response-range=\"100-399\" >\n" + 
                "        </url>\n" + 
                "        <attributes>\n" + 
                "          <attrib alias=\"documentCount\" match-group=\"1\" type=\"counter32\"/>\n" + 
                "        </attributes>\n" + 
                "      </uri>\n" + 
                "    </uris>\n" + 
                "  </http-collection>\n" + 
                "</http-datacollection-config>").getBytes(StandardCharsets.UTF_8));
        new HttpCollectionConfigFactory(rdr);
        assertNotNull(HttpCollectionConfigFactory.getConfig());
    }

    @After
    public void tearDown() {
        HttpCollectionConfigFactory.setInstance(null);
        HttpCollectionConfigFactory.setConfigDao(null);
    }

    @Test
    public void initDoesNotReadTheConfig() {
        final HttpCollectionConfigDao dao = mock(HttpCollectionConfigDao.class);
        HttpCollectionConfigFactory.setConfigDao(dao);
        HttpCollectionConfigFactory.setInstance(null);

        HttpCollectionConfigFactory.init();

        assertNotNull(HttpCollectionConfigFactory.getInstance());
        verifyNoInteractions(dao);
    }

    @Test
    public void readsCollectionsFromTheDao() {
        final HttpCollectionConfigDao dao = mock(HttpCollectionConfigDao.class);
        when(dao.getConfig()).thenReturn(config("/var/rrd/snmp/", "Apache", 60));
        HttpCollectionConfigFactory.setConfigDao(dao);
        HttpCollectionConfigFactory.init();
        final HttpCollectionConfigFactory factory = HttpCollectionConfigFactory.getInstance();

        assertThat(factory.getHttpCollection("apache").getName(), equalTo("Apache"));
        assertThat(factory.getStep("apache"), equalTo(60));
        assertThat(factory.getRRAList("apache"), contains("RRA:AVERAGE:0.5:1:2016"));
        assertThat(factory.getRrdPath(), equalTo("/var/rrd/snmp"));

        final RrdRepository repository = factory.getRrdRepository("apache");
        assertThat(repository.getRrdBaseDir(), equalTo(new File("/var/rrd/snmp")));
        assertThat(repository.getStep(), equalTo(60));
        assertThat(repository.getHeartBeat(), equalTo(120));

        assertThrows(IllegalArgumentException.class, () -> factory.getHttpCollection("missing"));
    }

    @Test
    public void seesConfigChangesOfTheDao() {
        final HttpCollectionConfigDao dao = mock(HttpCollectionConfigDao.class);
        when(dao.getConfig()).thenReturn(config("/var/rrd/snmp/", "first", 60));
        HttpCollectionConfigFactory.setConfigDao(dao);
        HttpCollectionConfigFactory.init();
        final HttpCollectionConfigFactory factory = HttpCollectionConfigFactory.getInstance();
        assertThat(factory.getStep("first"), equalTo(60));

        when(dao.getConfig()).thenReturn(config("/var/rrd/snmp/", "second", 300));

        assertThat(factory.getStep("second"), equalTo(300));
        assertThrows(IllegalArgumentException.class, () -> factory.getHttpCollection("first"));
    }

    private static HttpDatacollectionConfig config(final String rrdRepository, final String collectionName, final int step) {
        return JaxbUtils.unmarshal(HttpDatacollectionConfig.class,
                "<http-datacollection-config xmlns=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" rrdRepository=\"" + rrdRepository + "\">"
                + "<http-collection name=\"" + collectionName + "\">"
                + "<rrd step=\"" + step + "\"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>"
                + "<uris><uri name=\"u\"><url path=\"/\"/></uri></uris>"
                + "</http-collection>"
                + "</http-datacollection-config>");
    }

}
