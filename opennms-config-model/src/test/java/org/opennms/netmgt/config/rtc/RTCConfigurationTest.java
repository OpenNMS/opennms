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

package org.opennms.netmgt.config.rtc;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class RTCConfigurationTest extends XmlTestNoCastor<RTCConfiguration> {

    public RTCConfigurationTest(RTCConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/rtc-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        RTCConfiguration config = new RTCConfiguration();
        config.setUpdaters(10);
        config.setSenders(5);
        config.setRollingWindow("24h");
        config.setMaxEventsBeforeResend(100);
        config.setLowThresholdInterval("20s");
        config.setHighThresholdInterval("45s");
        config.setUserRefreshInterval("2m");
        config.setErrorsBeforeUrlUnsubscribe(5);

        return Arrays.asList(new Object[][] {
            {
                config,
                "<RTCConfiguration\n" + 
                "        updaters=\"10\"\n" + 
                "        senders=\"5\"\n" + 
                "        rollingWindow=\"24h\"\n" + 
                "        maxEventsBeforeResend=\"100\"\n" + 
                "        lowThresholdInterval=\"20s\"\n" + 
                "        highThresholdInterval=\"45s\"\n" + 
                "        userRefreshInterval=\"2m\"\n" + 
                "        errorsBeforeUrlUnsubscribe=\"5\"/>"
            }
        });
    }
}
