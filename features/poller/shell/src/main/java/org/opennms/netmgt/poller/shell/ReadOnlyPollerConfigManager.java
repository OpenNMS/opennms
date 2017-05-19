/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.PollerConfigManager;

/**
 * This class can be used to read an up-to-date copy of
 * poller-configuration.xml without affecting the global instance stored
 * in {@link org.opennms.netmgt.config.PollerConfigFactory}.
 *
 * @author jwhite
 */
public class ReadOnlyPollerConfigManager extends PollerConfigManager {

    private ReadOnlyPollerConfigManager(InputStream stream, String localServer, boolean verifyServer) {
        super(stream, localServer, verifyServer);
    }

    public static ReadOnlyPollerConfigManager create() throws IOException {
        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
        try (InputStream is = new FileInputStream(cfgFile)) {
            return new ReadOnlyPollerConfigManager(is, null, false);
        }
    }

    @Override
    public void update() throws IOException {
        // pass
    }

    @Override
    protected void saveXml(String xml) throws IOException {
        // pass
    }
}
