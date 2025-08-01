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
