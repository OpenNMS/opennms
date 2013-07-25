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

package org.opennms.netmgt.config.ackd;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AckdConfigurationTest extends XmlTestNoCastor<AckdConfiguration> {

    public AckdConfigurationTest(final AckdConfiguration sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {

        ReaderSchedule schedule = new ReaderSchedule(60L, "s");
        List<Parameter> parameters = new ArrayList<Parameter>(1);
        parameters.add(new Parameter("readmail-config", "localhost"));
        Reader reader = new Reader("JavaMailReader", false, schedule,
                                   parameters);
        Readers readers = new Readers(reader);
        AckdConfiguration ackdConfig = new AckdConfiguration(
                                                             true,
                                                             "~^ack$",
                                                             "~^unack$",
                                                             "~^(resolve|clear)$",
                                                             "~^esc$",
                                                             "~.*Re:.*Notice #([0-9]+).*",
                                                             "~.*alarmid:([0-9]+).*",
                                                             readers);
        return Arrays.asList(new Object[][] {
                {
                        ackdConfig,
                        "<ackd-configuration alarm-sync=\"true\" ack-expression=\"~^ack$\""
                                + " unack-expression=\"~^unack$\" escalate-expression=\"~^esc$\""
                                + " clear-expression=\"~^(resolve|clear)$\""
                                + " notifyid-match-expression=\"~.*Re:.*Notice #([0-9]+).*\""
                                + " alarmid-match-expression=\"~.*alarmid:([0-9]+).*\">"
                                + "<readers>"
                                + "<reader enabled=\"false\" reader-name=\"JavaMailReader\">"
                                + "<reader-schedule interval=\"60\" unit=\"s\"/>"
                                + "<parameter key=\"readmail-config\" value=\"localhost\" />"
                                + "</reader>" + "</readers>"
                                + "</ackd-configuration>",
                        "target/classes/xsds/ackd-configuration.xsd", },
                { new AckdConfiguration(), "<ackd-configuration/>",
                        "target/classes/xsds/ackd-configuration.xsd" } });
    }
}
