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
