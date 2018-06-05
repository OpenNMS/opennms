/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

