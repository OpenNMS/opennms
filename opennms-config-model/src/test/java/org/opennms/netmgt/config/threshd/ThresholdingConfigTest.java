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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ThresholdingConfigTest extends XmlTestNoCastor<ThresholdingConfig> {

    public ThresholdingConfigTest(ThresholdingConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/thresholding.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        ThresholdingConfig thresholdingConfig = new ThresholdingConfig();
        Group group = new Group();
        group.setName("coffee");
        group.setRrdRepository("${install.share.dir}/rrd/snmp/");
        Expression expression = new Expression();
        expression.setDescription("des");
        expression.setType(ThresholdType.LOW);
        expression.setDsType("node");
        expression.setValue("${value:25.0|value1:23.0|25.9}");
        expression.setRearm("-200.7e+5");
        expression.setTrigger("003498090");
        expression.setFilterOperator(FilterOperator.OR);
        expression.setExpression("coffeePotLevel / coffeePotCapacity * ${percent:200.0 |100.0}");
        Expression expression1 = new Expression();
        expression1.setDescription("des1");
        expression1.setType(ThresholdType.LOW);
        expression1.setDsType("node3");
        expression1.setValue("3456.34");
        expression1.setRearm("0.345");
        expression1.setTrigger("${trigger:023|345}");
        expression1.setFilterOperator(FilterOperator.OR);
        expression1.setExpression("coffeePotLevel / coffeePotCapacity * 250/100");
        List<Expression> expressions = new ArrayList<>();
        expressions.add(expression);
        expressions.add(expression1);
        group.setExpressions(expressions);
        thresholdingConfig.addGroup(group);
        return Arrays.asList(new Object[][] {
            {
                thresholdingConfig,
                "<thresholding-config>\n" +
                        "   <group name=\"coffee\" rrdRepository=\"${install.share.dir}/rrd/snmp/\">\n" +
                        "      <expression description=\"des\" type=\"low\" ds-type=\"node\" value=\"${value:25.0|value1:23.0|25.9}\" rearm=\"-200.7e+5\" " +
                        "trigger=\"003498090\" filterOperator=\"OR\" expression=\"coffeePotLevel / coffeePotCapacity * ${percent:200.0 |100.0}\"/>\n" +
                        "      <expression description=\"des1\" type=\"low\" ds-type=\"node3\" value=\"3456.34\" rearm=\"0.345\" " +
                        "trigger=\"${trigger:023|345}\" filterOperator=\"OR\" expression=\"coffeePotLevel / coffeePotCapacity * 250/100\"/>\n" +
                        "   </group>" +
                        "</thresholding-config>"
            }
        });
    }
}
