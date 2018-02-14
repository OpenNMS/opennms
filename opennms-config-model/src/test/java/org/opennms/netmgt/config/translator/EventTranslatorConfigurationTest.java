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

package org.opennms.netmgt.config.translator;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EventTranslatorConfigurationTest extends XmlTestNoCastor<EventTranslatorConfiguration> {

    public EventTranslatorConfigurationTest(EventTranslatorConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/translator-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<event-translator-configuration\n" + 
                "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\"\n" + 
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" + 
                "  <translation>\n" + 
                "    <event-translation-spec uei=\"uei.opennms.org/mib2opennms/tspEventPCRRepetitionError\">\n" + 
                "      <!-- Each event can have 1 or more mappings. -->\n" + 
                "      <mappings>\n" + 
                "        <mapping>\n" + 
                "          <!-- This mapping uses the SQL value type to query the DB and change the nodeid of the new event -->\n" + 
                "          <assignment type=\"field\" name=\"nodeid\">\n" + 
                "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'\" >\n" + 
                "              <!-- These are sub value types that are used as parameters to the above sql as in JDBC speak -->\n" + 
                "              <value type=\"parameter\" name=\".1.3.6.1.4.1.6768.6.2.2.5.0\" matches=\"^([A-z]+) ([0-9]+).*\" result=\"${1}-${2}\" />\n" + 
                "              <value type=\"constant\" result=\"169.254.1.1\" />\n" + 
                "            </value>\n" + 
                "          </assignment>\n" + 
                "        </mapping>\n" + 
                "      </mappings>\n" + 
                "    </event-translation-spec>\n" + 
                "  </translation>\n" + 
                "</event-translator-configuration>"
            },
            {
                new EventTranslatorConfiguration(),
                "<event-translator-configuration><translation/></event-translator-configuration>"
            }
        });
    }

    private static EventTranslatorConfiguration getConfig() {
        EventTranslatorConfiguration config = new EventTranslatorConfiguration();
        
        EventTranslationSpec spec = new EventTranslationSpec();
        spec.setUei("uei.opennms.org/mib2opennms/tspEventPCRRepetitionError");
        config.addEventTranslationSpec(spec);

        Mapping mapping = new Mapping();
        spec.addMapping(mapping);

        Assignment assignment = new Assignment();
        assignment.setType("field");
        assignment.setName("nodeid");
        mapping.addAssignment(assignment);

        Value v1 = new Value();
        v1.setType("sql");
        v1.setResult("select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'");
        assignment.setValue(v1);

        Value v2 = new Value();
        v2.setType("parameter");
        v2.setName(".1.3.6.1.4.1.6768.6.2.2.5.0");
        v2.setMatches("^([A-z]+) ([0-9]+).*");
        v2.setResult("${1}-${2}");
        v1.addValue(v2);

        Value v3 = new Value();
        v3.setType("constant");
        v3.setResult("169.254.1.1");
        v1.addValue(v3);

        return config;
    }
}
