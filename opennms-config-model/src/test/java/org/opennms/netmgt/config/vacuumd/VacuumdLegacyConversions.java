/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.vacuumd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;

public class VacuumdLegacyConversions {

    @Test
    public void statementToAutomation() throws Exception {
        String statementSql = "SELECT * FROM events;";
        String vacuumdConfigXml = "<VacuumdConfiguration period=\"1\">"
                + "<statement>" + statementSql + "</statement>"
                + "<automations/>" + "<triggers/>" + "<actions/>"
                + "<auto-events/>" + "<action-events/>"
                + "</VacuumdConfiguration>";

        VacuumdConfiguration vacuumdConfig = JaxbUtils.unmarshal(VacuumdConfiguration.class,
                                                                 vacuumdConfigXml);

        assertEquals(0, vacuumdConfig.getStatementCount());
        assertEquals(1, vacuumdConfig.getAutomations().getAutomationCount());
        assertEquals(1, vacuumdConfig.getActions().getActionCount());
        Action action = vacuumdConfig.getActions().getAction(0);
        assertEquals(statementSql, action.getStatement().getContent());
    }

    @Test
    public void autoEventToActionEvent() throws Exception {
        String eventUei = "uei.opennms.org/vacuumd/alarmEscalated";
        String vacuumdConfigXml = "<VacuumdConfiguration period=\"1\">"
                + "<automations>"
                + "  <automation name=\"escalate\" interval=\"60000\" active=\"true\""
                + "     auto-event-name=\"escalationEvent\" />"
                + "</automations>" + "<triggers/>" + "<actions/>"
                + "<auto-events>"
                + "  <auto-event name=\"escalationEvent\" >" + "     <uei>"
                + eventUei + "</uei>" + "  </auto-event>" + "</auto-events>"
                + "<action-events/>" + "</VacuumdConfiguration>";

        VacuumdConfiguration vacuumdConfig = JaxbUtils.unmarshal(VacuumdConfiguration.class,
                                                                 vacuumdConfigXml);

        assertEquals(0, vacuumdConfig.getAutoEvents().getAutoEventCount());
        assertEquals(1, vacuumdConfig.getActionEvents().getActionEventCount());
        ActionEvent actionEvent = vacuumdConfig.getActionEvents().getActionEvent(0);
        assertEquals(eventUei, actionEvent.getAssignment(0).getValue());
    }
}
