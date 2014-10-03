/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionResource;

/**
 * The Class XmlSingleInstanceCollectionResource.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlSingleInstanceCollectionResource extends XmlCollectionResource {

    /**
     * Instantiates a new XML single instance collection resource.
     *
     * @param agent the agent
     */
    public XmlSingleInstanceCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionResource#getResourceTypeName()
     */
    @Override
    public String getResourceTypeName() {
        return CollectionResource.RESOURCE_TYPE_NODE;
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionResource#getInstance()
     */
    @Override
    public String getInstance() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "node[" + m_agent.getNodeId() + "].nodeSnmp[]";
    }
}
