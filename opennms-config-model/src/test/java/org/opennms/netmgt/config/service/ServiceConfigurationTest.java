/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.service;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.config.service.types.InvokeAtType;

public class ServiceConfigurationTest extends
        XmlTestNoCastor<ServiceConfiguration> {

    public ServiceConfigurationTest(final ServiceConfiguration sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        List<Attribute> attributes = new LinkedList<Attribute>();
        attributes.add(new Attribute("Port", "java.lang.Integer", "58180"));
        attributes.add(new Attribute("Host", "java.lang.String", "127.0.0.1"));

        List<Argument> arguments = new LinkedList<Argument>();
        arguments.add(new Argument("java.lang.String", "admin"));
        arguments.add(new Argument("java.lang.String", "admin"));

        List<Invoke> invokes = new LinkedList<Invoke>();
        invokes.add(new Invoke(InvokeAtType.START, 0, "addAuthorization",
                               arguments));

        List<Service> services = new LinkedList<Service>();
        Service svc = new Service(":Name=HttpAdaptor",
                                  "mx4j.tools.adaptor.http.HttpAdaptor",
                                  attributes, invokes);
        services.add(svc);

        return Arrays.asList(new Object[][] { {
                new ServiceConfiguration(services),
                "<service-configuration>"
                        + "  <service>\n"
                        + "    <name>:Name=HttpAdaptor</name>\n"
                        + "    <class-name>mx4j.tools.adaptor.http.HttpAdaptor</class-name>\n"
                        + "    <attribute>\n"
                        + "      <name>Port</name>\n"
                        + "      <value type=\"java.lang.Integer\">58180</value>\n"
                        + "    </attribute>\n"
                        + "    <attribute>\n"
                        + "      <name>Host</name>\n"
                        + "      <value type=\"java.lang.String\">127.0.0.1</value>\n"
                        + "    </attribute>\n"
                        + "    <invoke at=\"start\" pass=\"0\" method=\"addAuthorization\">\n"
                        + "      <argument type=\"java.lang.String\">admin</argument>\n"
                        + "      <argument type=\"java.lang.String\">admin</argument>\n"
                        + "    </invoke>\n" + "  </service>\n"
                        + "</service-configuration>",
                "target/classes/xsds/service-configuration.xsd", }, });
    }
}
