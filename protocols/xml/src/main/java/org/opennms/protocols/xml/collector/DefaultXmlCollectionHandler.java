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

package org.opennms.protocols.xml.collector;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The default implementation of the interface XmlCollectionHandler based on AbstractXmlCollectionHandler.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultXmlCollectionHandler extends AbstractXmlCollectionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultXmlCollectionHandler.class);

    @Override
    protected void processXmlResource(CollectionSetBuilder builder, Resource collectionResource, String resourceTypeName, String group) { };

    @Override
    protected void fillCollectionSet(String urlString, Request request, CollectionAgent agent, CollectionSetBuilder builder, XmlSource source) throws Exception {
        final Document doc = getXmlDocument(urlString, request);
        LOG.debug("fillCollectionSet: parsed document for source url '{}' collection", urlString);
        fillCollectionSet(agent, builder, source, doc);
    }

}
