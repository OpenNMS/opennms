/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.dao.api.MonitoringLocationDao;

/**
 * <p>DistributedStatusNavBarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DistributedStatusNavBarEntry extends LocationBasedNavBarEntry {

    private MonitoringLocationDao m_monitoringLocationDao;

    /** {@inheritDoc} */
    @Override
    public DisplayStatus evaluate(HttpServletRequest request) {
        if (m_monitoringLocationDao.findAll().size() > 0) {
            return super.evaluate(request);
        } else {
            return DisplayStatus.NO_DISPLAY;
        }
    }

    /**
     * <p>getMonitoringLocationDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.MonitoringLocationDao} object.
     */
    public MonitoringLocationDao getMonitoringLocationDao() {
        return m_monitoringLocationDao;
    }

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param monitoringLocationDao a {@link org.opennms.netmgt.dao.api.MonitoringLocationDao} object.
     */
    public void setMonitoringLocationDao(MonitoringLocationDao monitoringLocationDao) {
        m_monitoringLocationDao = monitoringLocationDao;
    }
}
