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
package org.opennms.netmgt.config.service;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ServiceConfigurationTest extends
        XmlTestNoCastor<ServiceConfiguration> {

    public ServiceConfigurationTest(final ServiceConfiguration sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        List<Attribute> attributes = new LinkedList<>();
        attributes.add(new Attribute("Port", "java.lang.Integer", "58180"));
        attributes.add(new Attribute("Host", "java.lang.String", "127.0.0.1"));

        List<Argument> arguments = new LinkedList<>();
        arguments.add(new Argument("java.lang.String", "admin"));
        arguments.add(new Argument("java.lang.String", "admin"));

        List<Invoke> invokes = new LinkedList<>();
        invokes.add(new Invoke(InvokeAtType.START, 0, "addAuthorization",
                               arguments));

        List<Service> services = new LinkedList<>();
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
