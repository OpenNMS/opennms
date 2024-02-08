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
