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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlSource;

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

            @Override
            protected void fillCollectionSet(String urlString,
                    Request request, CollectionAgent agent,
                    XmlCollectionSet collectionSet, XmlSource source)
                    throws Exception {
            }
        };
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("mynode.local");
        OnmsAssetRecord asset = new OnmsAssetRecord();
        asset.setSerialNumber("1001");
        node.setAssetRecord(asset);
        String url = handler.parseString("URL", "http://{nodeLabel}/{ipAddress}/serial/{serialNumber}/{step}", node, "127.0.0.1", 300);
        Assert.assertEquals("http://mynode.local/127.0.0.1/serial/1001/300", url);
        String multiline = "<data>\n   <source label='{nodeLabel}'/>\n</data>";
        String xml = handler.parseString("Content", multiline, node, "127.0.0.1", 300);
        Assert.assertEquals("<data>\n   <source label='mynode.local'/>\n</data>", xml);
    }

}
