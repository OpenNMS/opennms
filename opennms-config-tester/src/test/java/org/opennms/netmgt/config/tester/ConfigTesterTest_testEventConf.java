/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.tester;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.xml.eventconf.Events;

public class ConfigTesterTest_testEventConf {

    @Test
    public void testEventConfWithValidVbNumber() throws IOException {
        assertNotNull(testEventConf("<vbnumber>1</vbnumber><vbvalue>0</vbvalue>"));
    }

    @Test(expected = MarshallingResourceFailureException.class)
    public void testEventConfWithMissingVbNumberButExistingVbValue() throws IOException {
        testEventConf("<!-- vbnumber missing --><vbvalue>0</vbvalue>");
    }

    @Test
    public void testEventConfWithMissingVbNumberAndExistingVbValue() throws IOException {
        testEventConf("<!-- vbnumber and vbvalue missing -->");
    }

    private Events testEventConf(String varbindContent) throws IOException {
        // Tests NMS-9821
        String xml = String.format("<events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">%n" +
                "   <event>%n" +
                "      <mask>%n" +
                "         <maskelement>%n" +
                "            <mename>id</mename>%n" +
                "            <mevalue>.1.3.6.1.4.1.4874.2.2.30</mevalue>%n" +
                "         </maskelement>%n" +
                "         <varbind>%n" +
                "            %s%n" +
                "         </varbind>%n" +
                "      </mask>%n" +
                "      <uei>uei.opennms.org/vendor/juniper/traps/juniCliSecurityAlertPriority0</uei>%n" +
                "      <event-label>Juniper-CLI-MIB defined trap event: juniCliSecurityAlert</event-label>%n" +
                "      <descr>blah</descr>%n" +
                "      <logmsg dest=\"logndisplay\">Juniper CLI Security Alert.</logmsg>%n" +
                "      <severity>Major</severity>%n" +
                "   </event>%n" +
                "</events>", varbindContent);
        return JaxbUtils.unmarshal(Events.class, new StringReader(xml));
    }
}
