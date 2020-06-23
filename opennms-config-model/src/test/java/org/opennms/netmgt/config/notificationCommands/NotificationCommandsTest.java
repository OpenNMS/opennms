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

package org.opennms.netmgt.config.notificationCommands;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class NotificationCommandsTest extends XmlTestNoCastor<NotificationCommands> {

    public NotificationCommandsTest(NotificationCommands sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/notificationCommands.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getNotificationCommands(),
                "<notification-commands>\n" + 
                "    <header>\n" + 
                "        <ver>.9</ver>\n" + 
                "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                "        <mstation>master.nmanage.com</mstation>\n" + 
                "    </header>\n" +
                "    <command binary=\"false\">\n" + 
                "        <name>javaEmail</name>\n" + 
                "        <execute>org.opennms.netmgt.notifd.JavaMailNotificationStrategy</execute>\n" + 
                "        <comment>class for sending email notifications</comment>\n" + 
                "        <contact-type>email</contact-type>\n" + 
                "        <argument streamed=\"false\">\n" + 
                "            <switch>-subject</switch>\n" + 
                "        </argument>\n" +
                "    </command>\n" +
                "</notification-commands>"
            }
        });
    }

    private static NotificationCommands getNotificationCommands() {
        NotificationCommands commands = new NotificationCommands();

        Header header = new Header();
        header.setVer(".9");
        header.setCreated("Wednesday, February 6, 2002 10:10:00 AM EST");
        header.setMstation("master.nmanage.com");
        commands.setHeader(header);

        Command command = new Command();
        command.setBinary(false);
        command.setName("javaEmail");
        command.setExecute("org.opennms.netmgt.notifd.JavaMailNotificationStrategy");
        command.setComment("class for sending email notifications");
        command.setContactType("email");
        commands.addCommand(command);

        Argument arg = new Argument();
        arg.setStreamed(false);
        arg.setSwitch("-subject");
        command.addArgument(arg);

        return commands;
    }
}
