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

package org.opennms.netmgt.config.tl1d;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class Tl1dConfigurationTest extends XmlTestNoCastor<Tl1dConfiguration> {

    public Tl1dConfigurationTest(Tl1dConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/tl1d-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<tl1d-configuration>\n" +
                "  <tl1-element host=\"127.0.0.1\" \n" +
                "               port=\"15001\" \n" +
                "               password=\"opennms\" \n" +
                "               reconnect-delay=\"30000\" \n" +
                "               tl1-client-api=\"org.opennms.netmgt.tl1d.Tl1ClientImpl\"\n" +
                "               tl1-message-parser=\"org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor\" \n" +
                "               userid=\"opennms\"/>\n" +
                "</tl1d-configuration>"
            },
            {
                new Tl1dConfiguration(),
                "<tl1d-configuration/>"
            }
        });
    }

    private static Tl1dConfiguration getConfig() {
        Tl1dConfiguration config = new Tl1dConfiguration();
        
        Tl1Element el = new Tl1Element();
        el.setHost("127.0.0.1");
        el.setPort(15001);
        el.setPassword("opennms");
        el.setReconnectDelay(30000L);
        el.setTl1ClientApi("org.opennms.netmgt.tl1d.Tl1ClientImpl");
        el.setTl1MessageParser("org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor");
        el.setUserid("opennms");
        config.addTl1Element(el);

        return config;
    }
}

