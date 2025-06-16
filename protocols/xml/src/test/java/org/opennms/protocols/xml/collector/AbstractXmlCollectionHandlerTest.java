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

        String jsonContent = "{'test':{'key':'value','key2':0}}";
        String json = AbstractXmlCollectionHandler.parseString("Content", jsonContent, node, "127.0.0.1", 300, parameters);
        Assert.assertEquals(jsonContent, json);
    }

}
