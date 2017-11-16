/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;

public class TopologyReloadEvent {

    private final OpenNMSSeleniumTestCase testCase;

    public TopologyReloadEvent(OpenNMSSeleniumTestCase testCase) {
        this.testCase = Objects.requireNonNull(testCase);
    }

    // Send an event to force reload of topology
    public void send() throws InterruptedException, IOException {
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_TOPOLOGY_UEI, getClass().getSimpleName());
        builder.setTime(new Date());
        builder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
        testCase.sendPost("/rest/events", JaxbUtils.marshal(builder.getEvent()), 202);
        Thread.sleep(5000); // Wait to allow the event to be processed
    }
}
