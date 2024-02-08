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
package org.opennms.protocols.json.collector;

import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.protocols.xml.config.Request;

import net.sf.json.JSONObject;

/**
 * The Mock Class for DefaultJSONCollectionHandler.
 * <p>This file is created in order to avoid calling a real server to retrieve a valid file and  parse a provided sample file through MockDocumentBuilder</p>
 * 
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockDefaultJsonCollectionHandler extends DefaultJsonCollectionHandler {

    /* (non-Javadoc)
     * @see org.opennms.protocols.json.collector.AbstractJsonCollectionHandler#getJSONObject(java.lang.String, org.opennms.protocols.xml.config.Request)
     */
    @Override
    protected JSONObject getJSONObject(String urlString, Request request) {
        return MockDocumentBuilder.getJSONDocument();
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#parseUrl(java.lang.String, org.opennms.netmgt.collectd.CollectionAgent, java.lang.Integer)
     */
    @Override
    public String parseUrl(NodeDao nodeDao, String unformattedUrl, CollectionAgent agent, Integer collectionStep, final Map<String,String> parameters) {
        return unformattedUrl.replace("{ipaddr}", "127.0.0.1");
    }
}

