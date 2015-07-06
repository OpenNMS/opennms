/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;


import java.util.Arrays;
import java.util.Collection;


import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.JaxbTest;
import org.opennms.netmgt.correlation.drools.config.EngineConfiguration;
import org.opennms.netmgt.correlation.drools.config.Global;
import org.opennms.netmgt.correlation.drools.config.RuleSet;

import bsh.ParseException;

public class DroolsConfigurationTest extends JaxbTest<EngineConfiguration> {

    public DroolsConfigurationTest(final String schemaFile, final EngineConfiguration sampleObject, final String sampleXml, final String sampleJson) {
        super(sampleObject, sampleXml, sampleJson, schemaFile);
    }

    static class ConfigBuilder {
        EngineConfiguration m_engineConfiguration = new EngineConfiguration();
        RuleSet m_currentRuleSet;

        public EngineConfiguration get() {
            return m_engineConfiguration;
        }

        public ConfigBuilder addRuleSet(String name) {
            m_currentRuleSet = new RuleSet();
            m_currentRuleSet.setName(name);

            m_engineConfiguration.addRuleSet(m_currentRuleSet);
            return this;
        }

        public ConfigBuilder addRuleFile(String path) {
            m_currentRuleSet.addRuleFile(path);
            return this;
        }

        public ConfigBuilder addEvent(String uei) {
            m_currentRuleSet.addEvent(uei);
            return this;
        }

        public ConfigBuilder setAppContext(String path) {
            m_currentRuleSet.setAppContext(path);
            return this;
        }

        public <T> ConfigBuilder addGlobalValue(String name, Class<T> type, T val) {
            Global global = new Global();
            global.setName(name);
            global.setType(type.getName());
            global.setValue(val.toString());

            m_currentRuleSet.addGlobal(global);

            return this;
        }

        public ConfigBuilder addGlobalRef(String name, Class<?> type, String ref) {
            Global global = new Global();
            global.setName(name);
            global.setType(type.getName());
            global.setRef(ref);

            m_currentRuleSet.addGlobal(global);

            return this;
        }

    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {   "target/classes/xsds/drools-engine.xsd",
                new ConfigBuilder().get(), 
                "<engine-configuration/>",
                "{\"rule-set\":[]}"
            },
            {	"target/classes/xsds/drools-engine.xsd", 
                new ConfigBuilder()
                .addRuleSet("locationMonitorRules")
                .addRuleFile("file:src/test/opennms-home/etc/LocationMonitorRules.drl")
                .addEvent("uei.opennms.org/remote/nodes/nodeLostService")
                .addEvent("uei.opennms.org/remote/nodes/nodeRegainedService")
                .addGlobalValue("WIDE_SPREAD_THRESHOLD", Integer.class, 3)
                .addGlobalValue("FLAP_INTERVAL", Long.class, 1000L)
                .addGlobalValue("FLAP_COUNT", Integer.class, 3)
                .addRuleSet("nodeParentRules")
                .addRuleFile("file:src/test/opennms-home/etc/NodeParentRules.drl")
                .addEvent("uei.opennms.org/nodes/nodeDown")
                .addEvent("uei.opennms.org/nodes/nodeUp")
                .setAppContext("file:src/test/opennms-home/etc/nodeParentRules-context.xml")
                .addGlobalValue("POLL_INTERVAL", Long.class, 3000L)
                .addGlobalRef("nodeService", NodeService.class, "nodeService")
                .get(), 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                        "<engine-configuration>\n" + 
                        "  <rule-set name=\"locationMonitorRules\">\n" + 
                        "    <rule-file>file:src/test/opennms-home/etc/LocationMonitorRules.drl</rule-file>\n" + 
                        "    <event>uei.opennms.org/remote/nodes/nodeLostService</event>\n" + 
                        "    <event>uei.opennms.org/remote/nodes/nodeRegainedService</event>\n" + 
                        "    <global name=\"WIDE_SPREAD_THRESHOLD\" type=\"java.lang.Integer\" value=\"3\"/>\n" + 
                        "    <global name=\"FLAP_INTERVAL\" type=\"java.lang.Long\" value=\"1000\" />\n" + 
                        "    <global name=\"FLAP_COUNT\" type=\"java.lang.Integer\" value=\"3\" />\n" + 
                        "  </rule-set>\n" + 
                        "  <rule-set name=\"nodeParentRules\">\n" + 
                        "    <rule-file>file:src/test/opennms-home/etc/NodeParentRules.drl</rule-file>\n" + 
                        "    <event>uei.opennms.org/nodes/nodeDown</event>\n" + 
                        "    <event>uei.opennms.org/nodes/nodeUp</event>\n" + 
                        "    <app-context>file:src/test/opennms-home/etc/nodeParentRules-context.xml</app-context>\n" + 
                        "    <global name=\"POLL_INTERVAL\" type=\"java.lang.Long\" value=\"3000\" />\n" + 
                        "    <global name=\"nodeService\" type=\"org.opennms.netmgt.correlation.drools.NodeService\" ref=\"nodeService\" />\n" + 
                        "  </rule-set>\n" + 
                        "</engine-configuration>\n" + 
                        "",
                "{\n" + 
                "   \"rule-set\" : [ {\n" + 
                "      \"name\" : \"locationMonitorRules\",\n" + 
                "      \"rule-file\" : [ \"file:src/test/opennms-home/etc/LocationMonitorRules.drl\" ],\n" + 
                "      \"event\" : [ \"uei.opennms.org/remote/nodes/nodeLostService\", \"uei.opennms.org/remote/nodes/nodeRegainedService\" ],\n" + 
                "      \"global\" : [ {\n" + 
                "         \"name\" : \"WIDE_SPREAD_THRESHOLD\",\n" + 
                "         \"type\" : \"java.lang.Integer\",\n" + 
                "         \"value\" : \"3\"\n" + 
                "      }, {\n" + 
                "         \"name\" : \"FLAP_INTERVAL\",\n" + 
                "         \"type\" : \"java.lang.Long\",\n" + 
                "         \"value\" : \"1000\"\n" + 
                "      }, {\n" + 
                "         \"name\" : \"FLAP_COUNT\",\n" + 
                "         \"type\" : \"java.lang.Integer\",\n" + 
                "         \"value\" : \"3\"\n" + 
                "      } ]\n" + 
                "   }, {\n" + 
                "      \"name\" : \"nodeParentRules\",\n" + 
                "      \"rule-file\" : [ \"file:src/test/opennms-home/etc/NodeParentRules.drl\" ],\n" + 
                "      \"event\" : [ \"uei.opennms.org/nodes/nodeDown\", \"uei.opennms.org/nodes/nodeUp\" ],\n" + 
                "      \"app-context\" : \"file:src/test/opennms-home/etc/nodeParentRules-context.xml\",\n" + 
                "      \"global\" : [ {\n" + 
                "         \"name\" : \"POLL_INTERVAL\",\n" + 
                "         \"type\" : \"java.lang.Long\",\n" + 
                "         \"value\" : \"3000\"\n" + 
                "      }, {\n" + 
                "         \"name\" : \"nodeService\",\n" + 
                "         \"type\" : \"org.opennms.netmgt.correlation.drools.NodeService\",\n" + 
                "         \"ref\" : \"nodeService\"\n" + 
                "      } ]\n" + 
                "   } ]\n" + 
                "}"
            }
        });

    }

}
