/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.eventd;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EventdConfigurationTest extends XmlTestNoCastor<EventdConfiguration> {

    public EventdConfigurationTest(final EventdConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/eventd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final EventdConfiguration config = new EventdConfiguration();
        config.setTCPAddress("127.0.0.1");
        config.setTCPPort(5817);
        config.setUDPAddress("127.0.0.1");
        config.setUDPPort(5817);
        config.setReceivers(5);
        config.setGetNextEventID("SELECT nextval('eventsNxtId')");
        config.setSocketSoTimeoutRequired("yes");
        config.setSocketSoTimeoutPeriod(3000);

        return Arrays.asList(new Object[][] {
            {
                config,
                "<EventdConfiguration\n" + 
                        "        TCPAddress=\"127.0.0.1\"\n" + 
                        "        TCPPort=\"5817\"\n" + 
                        "        UDPAddress=\"127.0.0.1\"\n" + 
                        "        UDPPort=\"5817\"\n" + 
                        "        receivers=\"5\"\n" + 
                        "        getNextEventID=\"SELECT nextval('eventsNxtId')\"\n" + 
                        "        socketSoTimeoutRequired=\"yes\"\n" + 
                        "        socketSoTimeoutPeriod=\"3000\">\n" + 
                        "</EventdConfiguration>"
            }
        });
    }
}
