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
package org.opennms.netmgt.asterisk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the default Asterisk configuration data.
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AsteriskConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AsteriskConfig.class);

    /**
     * This loads the configuration file.
     *
     * @return a Properties object representing the configuration properties
     * @throws java.io.IOException if any.
     */
    public static synchronized Properties getProperties() throws IOException {
        LOG.debug("Loading Asterisk configuration properties.");
        Properties properties = new Properties();
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.ASTERISK_CONFIG_FILE_NAME);
        InputStream in = new FileInputStream(configFile);
        properties.load(in);
        in.close();
        return properties;
    }
}
