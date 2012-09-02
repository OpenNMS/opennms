/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;

public class OutageTest extends XmlTest<Outage> {

    public OutageTest(final Outage sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Outage outage = new Outage();
        outage.setName("junit test");
        outage.setType("weekly");
        final Interface intf = new Interface();
        intf.setAddress("match-any");
        outage.addInterface(intf);
        final Time time = new Time();
        time.setDay("monday");
        time.setBegins("13:30:00");
        time.setEnds("14:45:00");
        outage.addTime(time);
        
        return Arrays.asList(new Object[][] {
            {
                outage,
                "<outage name='junit test' type='weekly'>\n" +
                "    <time day='monday' begins='13:30:00' ends='14:45:00'/>\n" +
                "    <interface address='match-any'/>\n" +
                "</outage>\n",
                "target/classes/xsds/poll-outages.xsd"
            }
        });
    }

}
