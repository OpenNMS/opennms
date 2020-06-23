/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.dao.thresholding.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

public class OverrideableThreshdDaoImpl extends AbstractThreshdDao implements OverrideableThreshdDao {
    private ThreshdConfiguration threshdConfiguration;

    public OverrideableThreshdDaoImpl() {
        super();
    }

    @Override
    public synchronized void overrideConfig(ThreshdConfiguration config) {
        threshdConfiguration = Objects.requireNonNull(config);
        rebuildPackageIpListMap();
    }

    @Override
    public synchronized void overrideConfig(InputStream config) {
        Objects.requireNonNull(config);

        try (Reader reader = new InputStreamReader(config)) {
            overrideConfig(JaxbUtils.unmarshal(ThreshdConfiguration.class, reader));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveConfig() {
        // no-op
    }

    @Override
    public void onConfigChanged() {
        // no-op
    }

    @Override
    public synchronized ThreshdConfiguration getReadOnlyConfig() {
        return threshdConfiguration;
    }

    @Override
    public synchronized void reload() {
        if (threshdConfiguration == null) {
            threshdConfiguration = new ThreshdConfiguration();
        }
        super.reload();
    }
}
