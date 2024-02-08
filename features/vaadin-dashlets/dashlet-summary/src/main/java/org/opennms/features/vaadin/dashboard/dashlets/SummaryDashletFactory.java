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
