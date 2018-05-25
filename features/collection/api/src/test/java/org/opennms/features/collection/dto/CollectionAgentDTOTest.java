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

package org.opennms.features.collection.dto;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.model.ResourcePath;

public class CollectionAgentDTOTest extends XmlTestNoCastor<CollectionAgentDTO> {

    public CollectionAgentDTOTest(CollectionAgentDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        CollectionAgentDTO collectionAgentDTO = new CollectionAgentDTO();
        collectionAgentDTO.setAddress(InetAddressUtils.getInetAddress("192.168.1.1"));
        collectionAgentDTO.setAttribute("k1", "v1");
        collectionAgentDTO.setStoreByForeignSource(true);
        collectionAgentDTO.setNodeId(99);
        collectionAgentDTO.setNodeLabel("switch");
        collectionAgentDTO.setForeignSource("fs");
        collectionAgentDTO.setForeignId("fid");
        collectionAgentDTO.setLocationName("HQ");
        collectionAgentDTO.setStorageResourcePath(ResourcePath.get("tmp", "foo"));
        collectionAgentDTO.setSavedSysUpTime(149);

        return Arrays.asList(new Object[][] {
            {
                collectionAgentDTO,
                "<agent address=\"192.168.1.1\" store-by-fs=\"true\" node-id=\"99\" node-label=\"switch\" foreign-source=\"fs\" foreign-id=\"fid\" location=\"HQ\" storage-resource-path=\"tmp/foo\" sys-up-time=\"149\">\n" +
                "   <attribute key=\"k1\"><![CDATA[v1]]></attribute>\n" +
                "</agent>"
            }
        });
    }
}
