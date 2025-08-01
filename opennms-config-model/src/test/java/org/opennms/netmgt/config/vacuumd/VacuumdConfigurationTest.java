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
package org.opennms.netmgt.config.vacuumd;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class VacuumdConfigurationTest extends
        XmlTestNoCastor<VacuumdConfiguration> {

    public VacuumdConfigurationTest(final VacuumdConfiguration sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        // Simplest config
        VacuumdConfiguration minimalistVacuumdConfig = new VacuumdConfiguration();
        minimalistVacuumdConfig.setPeriod(1);

        // A config with every option set
        VacuumdConfiguration vacuumdConfig;
        List<Statement> statements = new ArrayList<>();
        Statement statement = new Statement(
                                            "INSERT 1000000 INTO bankaccount;",
                                            false);
        statements.add(statement);

        List<Automation> automations = Arrays.asList(new Automation("testAutomation", 3000,
                                               "testTrigger", "testAction",
                                               "testAutoEvent",
                                               "testActionEvent", false));

        List<Trigger> triggers = Arrays.asList(new Trigger("testTrigger", "testDataSource", ">=",
                                      0, statement));

        List<Action> actions = Arrays.asList(new Action("testTrigger", "testDataSource", statement));

        List<AutoEvent> autoEvents = Arrays.asList(new AutoEvent("testAutoEvent", "testField",
                                            new Uei("testUei")));

        List<Assignment> assignments = Arrays.asList(new Assignment("field", "uei", "testUei"));

        List<ActionEvent> actionEvents = Arrays.asList(new ActionEvent("testActionEvent", true, assignments));

        vacuumdConfig = new VacuumdConfiguration(1, statements, automations,
                                                 triggers, actions,
                                                 autoEvents, actionEvents);

        return Arrays.asList(new Object[][] {
                {
                        minimalistVacuumdConfig,
                        "<VacuumdConfiguration period=\"1\">"
                                + "<automations/>" + "<triggers/>"
                                + "<actions/>" + "<auto-events/>"
                                + "<action-events/>"
                                + "</VacuumdConfiguration>",
                        "target/classes/xsds/vacuumd-configuration.xsd" },
                {
                        vacuumdConfig,
                        "<VacuumdConfiguration period=\"1\">"
                                + "<statement transactional=\"false\">INSERT 1000000 INTO bankaccount;</statement>"
                                + "<automations>"
                                + "    <automation name=\"testAutomation\" interval=\"3000\""
                                + " trigger-name=\"testTrigger\" action-name=\"testAction\""
                                + " auto-event-name=\"testAutoEvent\" action-event=\"testActionEvent\" active=\"false\"/>"
                                + "</automations>"
                                + "<triggers>"
                                + "    <trigger name=\"testTrigger\" data-source=\"testDataSource\" operator=\"&gt;=\" row-count=\"0\">"
                                + "        <statement transactional=\"false\">INSERT 1000000 INTO bankaccount;</statement>"
                                + "    </trigger>"
                                + "</triggers>"
                                + "<actions>"
                                + "    <action name=\"testTrigger\" data-source=\"testDataSource\">"
                                + "        <statement transactional=\"false\">INSERT 1000000 INTO bankaccount;</statement>"
                                + "    </action>"
                                + "</actions>"
                                + "<auto-events>"
                                + "    <auto-event name=\"testAutoEvent\" fields=\"testField\">"
                                + "        <uei>testUei</uei>"
                                + "    </auto-event>"
                                + "</auto-events>"
                                + "<action-events>"
                                + "    <action-event name=\"testActionEvent\" for-each-result=\"true\">"
                                + "        <assignment type=\"field\" name=\"uei\" value=\"testUei\"/>"
                                + "    </action-event>" + "</action-events>"
                                + "</VacuumdConfiguration>",
                        "target/classes/xsds/vacuumd-configuration.xsd" } });
    }
}
