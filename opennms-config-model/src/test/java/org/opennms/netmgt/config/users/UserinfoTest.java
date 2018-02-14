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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.users;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class UserinfoTest extends XmlTestNoCastor<Userinfo> {

    public UserinfoTest(final Userinfo sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Userinfo ui = new Userinfo();
        ui.setHeader(new Header(".9", "Thursday, November 3, 2011 9:28:08 PM GMT", "master.nmanage.com"));

        final User admin = new User("admin", "Administrator", "Default administrator, do not delete");
        admin.setPassword("21232F297A57A5A743894A0E4A801FC3", false);
        admin.addRole("ROLE_ADMIN");

        final User rtc = new User("rtc", "RTC", "RTC user, do not delete");
        rtc.setPassword("68154466F81BFB532CD70F8C71426356", false);
        rtc.addRole("ROLE_RTC");

        ui.setUsers(Arrays.asList(admin, rtc));

        return Arrays.asList(new Object[][] {
            {
                ui,
                "<userinfo>\n" + 
                "    <header>\n" + 
                "        <rev>.9</rev>\n" + 
                "        <created>Thursday, November 3, 2011 9:28:08 PM GMT</created>\n" + 
                "        <mstation>master.nmanage.com</mstation>\n" + 
                "    </header>\n" + 
                "    <users>\n" + 
                "        <user>\n" + 
                "            <user-id>admin</user-id>\n" + 
                "            <full-name>Administrator</full-name>\n" + 
                "            <user-comments>Default administrator, do not delete</user-comments>\n" + 
                "            <password salt=\"false\">21232F297A57A5A743894A0E4A801FC3</password>\n" + 
                "            <role>ROLE_ADMIN</role>\n" + 
                "        </user>\n" + 
                "        <user>\n" + 
                "            <user-id>rtc</user-id>\n" + 
                "            <full-name>RTC</full-name>\n" + 
                "            <user-comments>RTC user, do not delete</user-comments>\n" + 
                "            <password salt=\"false\">68154466F81BFB532CD70F8C71426356</password>\n" + 
                "            <role>ROLE_RTC</role>\n" + 
                "        </user>\n" + 
                "    </users>\n" + 
                "</userinfo>",
                "src/main/resources/xsds/users.xsd"
            }
        });
    }
}
