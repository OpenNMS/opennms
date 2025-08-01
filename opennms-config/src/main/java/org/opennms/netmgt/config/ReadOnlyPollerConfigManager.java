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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.utils.ConfigFileConstants;

/**
 * This class can be used to read an up-to-date copy of
 * poller-configuration.xml without affecting the global instance stored
 * in {@link org.opennms.netmgt.config.PollerConfigFactory}.
 *
 * @author jwhite
 */
public class ReadOnlyPollerConfigManager extends PollerConfigManager {

    private ReadOnlyPollerConfigManager(InputStream stream) {
        super(stream);
    }

    public static ReadOnlyPollerConfigManager create() throws IOException {
        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
        try (InputStream is = new FileInputStream(cfgFile)) {
            return new ReadOnlyPollerConfigManager(is);
        }
    }

    @Override
    protected void saveXml(String xml) throws IOException {
        // pass
    }
}
