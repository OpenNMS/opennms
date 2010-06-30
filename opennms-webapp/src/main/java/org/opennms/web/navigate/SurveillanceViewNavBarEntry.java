/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Dec 08: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 Daniel J. Gregor, Jr.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.navigate;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.dao.SurveillanceViewConfigDao;
import org.opennms.web.Util;

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
     * @return a {@link org.opennms.netmgt.dao.SurveillanceViewConfigDao} object.
     */
    public SurveillanceViewConfigDao getSurveillanceViewConfigDao() {
        return m_surveillanceViewConfigDao;
    }

    /**
     * <p>setSurveillanceViewConfigDao</p>
     *
     * @param surveillanceViewConfigDao a {@link org.opennms.netmgt.dao.SurveillanceViewConfigDao} object.
     */
    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceViewConfigDao) {
        m_surveillanceViewConfigDao = surveillanceViewConfigDao;
    }
}
