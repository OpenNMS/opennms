/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;

import java.util.Set;

public abstract class SurveillanceViewDetailTable extends Table {
    private SurveillanceViewService m_surveillanceViewService;
    protected boolean m_enabled;

    public SurveillanceViewDetailTable(String title, SurveillanceViewService surveillanceViewService, boolean enabled) {
        super(title);

        m_surveillanceViewService = surveillanceViewService;
        m_enabled = enabled;
        
        setSizeFull();
        setPageLength(5);
    }

    protected SurveillanceViewService getSurveillanceViewService() {
        return m_surveillanceViewService;
    }

    public abstract void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories);
}
