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

package org.opennms.netmgt.config.ro;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.PollOutagesConfig;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;

public class OutagesConfigReadOnlyDao extends AbstractReadOnlyConfigDao<Outages> implements PollOutagesConfig {

    private final String fileName = ConfigFileConstants.getFileName(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME);

    private final long cacheLengthInMillis = SystemProperties.getLong("org.opennms.netmgt.config.ro.OutagesConfig.cacheTtlMillis", DEFAULT_CACHE_MILLIS);

    @Override
    public Outages getConfig() {
        return getByKey(Outages.class, fileName, cacheLengthInMillis);
    }

    @Override
    public boolean isNodeIdInOutage(long lnodeid, String outName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInterfaceInOutage(String linterface, String outName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCurTimeInOutage(String outName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTimeInOutage(long time, String outName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void update() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Outage getOutage(String name) {
        Outages outages = getConfig();
        return outages == null ? null : outages.getOutage(name);
    }

}
