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

package org.opennms.netmgt.config.threshd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ThresholdingConfigTest extends XmlTestNoCastor<ThresholdingConfig> {

    public ThresholdingConfigTest(ThresholdingConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/thresholding.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        ThresholdingConfig config = new ThresholdingConfig();

        Group group = new Group();
        group.setName("HTTP");
        group.setRrdRepository("/");
        config.addGroup(group);

        Threshold threshold = new Threshold();
        threshold.setDsName("coffee");
        threshold.setDsType("node");
        threshold.setType(ThresholdType.LOW);
        threshold.setValue(1.0d);
        threshold.setRearm(3.0d);
        threshold.setTrigger(1);
        threshold.setSendSustainedEvents(true);
        threshold.setSustainedUEI("sustained");
        group.addThreshold(threshold);

        return Arrays.asList(new Object[][] {
                {
                    new ThresholdingConfig(),
                    "<thresholding-config/>"
                },
                {
                    config,
                    "<thresholding-config>\n" +
                    "   <group name=\"HTTP\" rrdRepository=\"/\">\n" +
                    "      <threshold type=\"low\" ds-type=\"node\" value=\"1.0\" rearm=\"3.0\" trigger=\"1\" " +
                            "ds-name=\"coffee\" sustainedUEI=\"sustained\" send-sustained-events=\"true\"/>\n" +
                    "   </group>\n" +
                    "</thresholding-config>"
                }
        });
    }
}
