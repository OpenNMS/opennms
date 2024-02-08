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
package org.opennms.netmgt.config.scriptd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ScriptdConfigurationTest extends XmlTestNoCastor<ScriptdConfiguration> {

    public ScriptdConfigurationTest(ScriptdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/scriptd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<scriptd-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/scriptd\">\n" + 
                "   <engine language=\"beanshell\" className=\"bsh.util.BeanShellBSFEngine\" extensions=\"bsh\"/>\n" + 
                "   <start-script language=\"beanshell\">println(&quot;start&quot;)</start-script>\n" + 
                "   <stop-script language=\"beanshell\">println(&quot;stop&quot;)</stop-script>\n" + 
                "   <event-script language=\"beanshell\">\n" +
                "     <uei name=\"com.company.uei/testTrap\"/>\n" +
                "     println(&quot;onEvent&quot;)\n" +
                "   </event-script>\n" +
                "</scriptd-configuration>"
            },
            {
                new ScriptdConfiguration(),
                "<scriptd-configuration/>"
            }
        });
    }

    private static ScriptdConfiguration getConfig() {
        ScriptdConfiguration config = new ScriptdConfiguration();
        
        Engine engine = new Engine();
        engine.setLanguage("beanshell");
        engine.setClassName("bsh.util.BeanShellBSFEngine");
        engine.setExtensions("bsh");
        config.addEngine(engine);

        StartScript start = new StartScript();
        start.setLanguage("beanshell");
        start.setContent("println(\"start\")");
        config.addStartScript(start);

        StopScript stop = new StopScript();
        stop.setLanguage("beanshell");
        stop.setContent("println(\"stop\")");
        config.addStopScript(stop);

        EventScript script = new EventScript();
        script.setLanguage("beanshell");
        script.setContent("\n" + 
                "     println(\"onEvent\")\n" + 
                "   ");
        config.addEventScript(script);

        Uei uei = new Uei();
        uei.setName("com.company.uei/testTrap");
        script.addUei(uei);

        return config;
    }
}
