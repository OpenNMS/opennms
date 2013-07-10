/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.opennms.netmgt.dao.api.SurveillanceViewConfigDao;
import org.opennms.web.api.Util;

/**
 * <p>SurveillanceViewNavBarEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class SurveillanceViewNavBarEntry extends LocationBasedNavBarEntry {
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;

    /** {@inheritDoc} */
    @Override
    public DisplayStatus evaluate(HttpServletRequest request) {
        if (m_surveillanceViewConfigDao.getViews().getViewCount() > 0 && m_surveillanceViewConfigDao.getDefaultView() != null) {
            setUrl("surveillanceView.htm?viewName=" + Util.htmlify(m_surveillanceViewConfigDao.getDefaultView().getName()));

            return super.evaluate(request);
        } else {
            return DisplayStatus.NO_DISPLAY;
        }
    }

    /**
     * <p>getSurveillanceViewConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.SurveillanceViewConfigDao} object.
     */
    public SurveillanceViewConfigDao getSurveillanceViewConfigDao() {
        return m_surveillanceViewConfigDao;
    }

    /**
     * <p>setSurveillanceViewConfigDao</p>
     *
     * @param surveillanceViewConfigDao a {@link org.opennms.netmgt.dao.api.SurveillanceViewConfigDao} object.
     */
    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceViewConfigDao) {
        m_surveillanceViewConfigDao = surveillanceViewConfigDao;
    }
}
