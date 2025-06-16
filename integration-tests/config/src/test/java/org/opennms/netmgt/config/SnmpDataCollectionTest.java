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

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.springframework.core.io.FileSystemResource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class SnmpDataCollectionTest {

    /**
     * Verify that the order of the includes in the default 'datacollection-config.xml'
     * file are listed in alphabetical order, with the exception of MIB2 which should
     * always come first.
     *
     * See https://issues.opennms.org/browse/NMS-9643
     */
    @Test
    public void verifySnmpCollectionIncludeOrder() {
        final DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class,
                new FileSystemResource(ConfigurationTestUtils.getFileForConfigFile("datacollection-config.xml")));
        final SnmpCollection snmpCollection = config.getSnmpCollection("default");
        assertNotNull("'default' snmp-collection should exist.", snmpCollection);

        // Current order
        final List<String> includesInOrderSpecified = snmpCollection.getIncludeCollections().stream()
                .map(inc -> inc.getDataCollectionGroup().toLowerCase())
                .collect(Collectors.toList());

        // Exepected order
        final List<String> includesInNaturalOrder = snmpCollection.getIncludeCollections().stream()
                .map(inc -> inc.getDataCollectionGroup().toLowerCase())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        // Special handling for the "MIB2" include, if present
        if (includesInOrderSpecified.indexOf("mib2") == 0) {
            includesInNaturalOrder.remove("mib2");
            includesInNaturalOrder.add(0, "mib2");
        }

        assertEquals("One or more SNMP data collections included in the default configuration are not listed in alphabetical order."
                + " Please sort and try again.", includesInNaturalOrder, includesInOrderSpecified);
    }
}
