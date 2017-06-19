/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.jmx.wrappers;

import com.google.common.base.Throwables;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import java.io.IOException;

/**
 * A Wrapper for a JMX Object Name used in JEXL expressions.
 */
public class ObjectNameWrapper extends AbstractWrapper {

    /**
     * The wrapped object name.
     */
    private final ObjectName objectName;

    private ObjectNameWrapper(final MBeanServerConnection connection, final ObjectName objectName) {
        super(connection);
        this.objectName = objectName;
    }

    @Override
    public Object get(final String attributeName) {
        try {
            return this.wrap(this.connection.getAttribute(this.objectName,
                                                          attributeName));

        } catch (final JMException | IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static ObjectNameWrapper create(final MBeanServerConnection connection, final ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new IllegalArgumentException("object name must not be a pattern");
        }

        return new ObjectNameWrapper(connection,
                                     objectName);
    }

    public static ObjectNameWrapper create(final MBeanServerConnection connection, final String objectNameString) {
        final ObjectName objectName;
        try {
            objectName = new ObjectName(objectNameString);

        } catch (final MalformedObjectNameException e) {
            throw Throwables.propagate(e);
        }

        return create(connection,
                      objectName);
    }
}
