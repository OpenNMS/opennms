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
package org.opennms.netmgt.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>NotificationCommandFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NotificationCommandFactory extends NotificationCommandManager {
    /**
     */
    private static NotificationCommandFactory instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private NotificationCommandFactory() {
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (!initialized) {
            getInstance().update();
            initialized = true;
        }
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotificationCommandFactory} object.
     */
    public static synchronized NotificationCommandFactory getInstance() {

        if (instance == null || !initialized) {
            instance = new NotificationCommandFactory();
        }

        return instance;
    }
    
    /**
     * <p>update</p>
     *
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    @Override
    public void update() throws FileNotFoundException, IOException {
        InputStream configIn = new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.NOTIF_COMMANDS_CONF_FILE_NAME));
        parseXML(configIn);
    }
}
