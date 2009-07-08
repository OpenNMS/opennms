/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

@Provider
public class ProvisionPrefixContextResolver implements ContextResolver<JAXBContext> {
    private final Map<Class<?>, String> m_urls = new HashMap<Class<?>, String>();
    
    public ProvisionPrefixContextResolver() throws JAXBException {
        m_urls.put(Requisition.class, "http://xmlns.opennms.org/xsd/config/model-import");
        m_urls.put(ForeignSource.class, "http://xmlns.opennms.org/xsd/config/foreign-source");
    }

    public JAXBContext getContext(Class<?> objectType) {
        try {
            return new ProvisionJAXBContext(JAXBContext.newInstance(objectType), m_urls.get(objectType));
        } catch (JAXBException e) {
            log().warn("unable to get context for class " + objectType, e);
            return null;
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(ProvisionPrefixContextResolver.class);
    }

}
