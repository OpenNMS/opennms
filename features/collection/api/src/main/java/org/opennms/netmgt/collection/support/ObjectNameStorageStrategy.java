/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import javax.management.ObjectName;

import org.apache.commons.jexl2.JexlContext;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectNameStorageStrategy extends JexlIndexStorageStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectNameStorageStrategy.class);
    private static final String QUOTE = "\"";

    public ObjectNameStorageStrategy() {
        super();
    }

    @Override
    public void updateContext(JexlContext context, CollectionResource resource) {
        try {
            ObjectName oname = new ObjectName(resource.getInstance());
            context.set("ObjectName", oname);
            context.set("domain", oname.getDomain() == null ? "" : oname.getDomain());
            oname.getKeyPropertyList().entrySet().forEach((entry) -> {
                final String value = entry.getValue();
                if (value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
                    context.set(entry.getKey(), ObjectName.unquote(entry.getValue()));
                } else {
                    context.set(entry.getKey(), entry.getValue());
                }
            });
        } catch (javax.management.MalformedObjectNameException e) {
            LOG.debug("getResourceNameFromIndex(): malformed object name: {}", resource.getInstance(), e);
        }
    }
}
