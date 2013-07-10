/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.collector;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.protocols.xml.config.XmlDataCollection;

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
        AbstractXmlCollectionHandler handler = new AbstractXmlCollectionHandler() {
            @Override
            public XmlCollectionSet collect(CollectionAgent agent,
                    XmlDataCollection collection,
                    Map<String, Object> parameters)
                            throws CollectionException {
                return null;
            }

            @Override
            protected void processXmlResource(
                    XmlCollectionResource collectionResource,
                    AttributeGroupType attribGroupType) {
            }
        };
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("mynode.local");
        OnmsAssetRecord asset = new OnmsAssetRecord();
        asset.setSerialNumber("1001");
        node.setAssetRecord(asset);
        String url = handler.parseString("URL", "http://{nodeLabel}/serial/{serialNumber}", node, "127.0.0.1");
        Assert.assertEquals("http://mynode.local/serial/1001", url);
        String multiline = "<data>\n   <source label='{nodeLabel}'/>\n</data>";
        String xml = handler.parseString("Content", multiline, node, "127.0.0.1");
        Assert.assertEquals("<data>\n   <source label='mynode.local'/>\n</data>", xml);
    }

}
