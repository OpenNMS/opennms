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

package org.opennms.core.ipc.common.aws.sqs;

import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

public class ManagedSQSConfig extends MapBasedSQSConfig {

    public ManagedSQSConfig(ConfigurationAdmin configAdmin) throws IOException {
        // Use the system properties as initial values and overwrite
        // the values with the properties from org.opennms.core.ipc.aws.sqs.cfg
        super(getConfigMapFromConfigAdmin(configAdmin, getConfigMapFromSystemProperties()));
    }

    private static Map<String, String> getConfigMapFromConfigAdmin(ConfigurationAdmin configAdmin, Map<String, String> sqsConfig) throws IOException {
        final Dictionary<String, Object> properties = configAdmin.getConfiguration(AmazonSQSConstants.AWS_CONFIG_PID).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                sqsConfig.put(key, (String)properties.get(key));
            }
        }
        return sqsConfig;
    }
}
