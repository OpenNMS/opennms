/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
