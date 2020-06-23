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
