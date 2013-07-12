/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.dashboard.dashlets;

import org.opennms.features.vaadin.dashboard.model.AbstractDashletFactory;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.AlarmDao;

/**
 * This class implements a factory used for instantiating new dashlet instances.
 *
 * @author Christian Pape
 */
public class SummaryDashletFactory extends AbstractDashletFactory {
    /**
     * The {@link AlarmDao} used
     */
    private AlarmDao m_alarmDao;

    /**
     * Constructor for instantiating a new factory.
     */
    public SummaryDashletFactory(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    /**
     * Method for instatiating a new {@link Dashlet} instance.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return a new {@link Dashlet} instance
     */
    public Dashlet newDashletInstance(DashletSpec dashletSpec) {
        return new SummaryDashlet(getName(), dashletSpec, m_alarmDao);
    }

    /**
     * Returns the help content {@link String}
     *
     * @return the help content
     */
    @Override
    public String getHelpContentHTML() {
        return "This Dashlet provides a overview of unacknowledged and acknowledged alarms in relation to\n" +
                " a given timeslot. It also indicates a trend for these alarms. On the left you see a table \n" +
                " by severity and on the right a table by the three important UEIs. The timeslot used is \n" +
                " configurable via a parameter in seconds. The trend indicates the relation between unacknowledged\n" +
                " to acknowledged alarms. For example the count of unacknowledged alarms is twice the number \n" +
                " of acknowledged alarms the arrow is up and red and indicates that it seems to getting\n" +
                " from bad to worse.";
    }
}
