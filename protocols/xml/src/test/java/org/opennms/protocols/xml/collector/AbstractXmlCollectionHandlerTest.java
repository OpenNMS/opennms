/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;

/**
 * The Test Class for AbstractXmlCollectionHandler.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AbstractXmlCollectionHandlerTest {

    /**
     * Test parse string.
     *
     * @throws Exception the exception
     */
    @Test
    public void testParseString() throws Exception {
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("mynode.local");
        OnmsAssetRecord asset = new OnmsAssetRecord();
        asset.setSerialNumber("1001");
        node.setAssetRecord(asset);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("port", "80");
        String url = AbstractXmlCollectionHandler.parseString("URL", "http://{nodeLabel}:{parameter:port}/{ipAddress}/serial/{serialNumber}/{step}", node, "127.0.0.1", 300, parameters);
        Assert.assertEquals("http://mynode.local:80/127.0.0.1/serial/1001/300", url);
        String multiline = "<data>\n   <source label='{nodeLabel}'/>\n</data>";
        String xml = AbstractXmlCollectionHandler.parseString("Content", multiline, node, "127.0.0.1", 300, parameters);
        Assert.assertEquals("<data>\n   <source label='mynode.local'/>\n</data>", xml);
    }

}
