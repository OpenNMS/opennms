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

import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlDataCollection;

/**
 * The Class XML Collection Handler.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public interface XmlCollectionHandler  {

    /**
     * Collect.
     *
     * @param agent the collection agent
     * @param collection the XML collection configuration
     * @param parameters the collector parameters
     * @return the XML collection set
     * @throws CollectionException the collection exception
     */
    public CollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException;

    /**
     * Sets the RRD repository.
     *
     * @param repository the new RRD repository
     */
    public void setRrdRepository(RrdRepository repository);

    /**
     * Sets the service name associated with this Collection Handler.
     *
     * @param serviceName the new service name
     */
    public void setServiceName(String serviceName);

    public String parseUrl(final NodeDao nodeDao, final String unformattedUrl, final CollectionAgent agent, final Integer collectionStep, final Map<String, String> parameters) throws IllegalArgumentException;

    public Request parseRequest(final NodeDao nodeDao, final Request unformattedRequest, final CollectionAgent agent, final Integer collectionStep, final Map<String, String> parameters) throws IllegalArgumentException;

}
