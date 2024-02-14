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
package org.opennms.protocols.xml.collector;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;

import com.google.common.collect.ImmutableMap;

public class XmlCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public XmlCollectorComplianceTest() {
        super(XmlCollector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .build();
    }

    public Map<String, Object> getRequiredBeans() {
        XmlDataCollectionConfig config = mock(XmlDataCollectionConfig.class);
        when(config.getRrdRepository()).thenReturn("target");
        when(config.buildRrdRepository(COLLECTION)).thenReturn(new RrdRepository());

        XmlRrd xmlRrd = new XmlRrd();
        xmlRrd.setStep(300);
        XmlDataCollection collection = new XmlDataCollection();
        collection.setXmlRrd(xmlRrd);
        XmlDataCollectionConfigDao xmlDataCollectionConfigDao = mock(XmlDataCollectionConfigDao.class);
        when(xmlDataCollectionConfigDao.getDataCollectionByName(COLLECTION)).thenReturn(collection);
        when(xmlDataCollectionConfigDao.getConfig()).thenReturn(config);

        return new ImmutableMap.Builder<String, Object>()
                .put("xmlDataCollectionConfigDao", xmlDataCollectionConfigDao)
                .build();
    }
}
