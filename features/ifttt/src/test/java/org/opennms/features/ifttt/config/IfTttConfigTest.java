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
package org.opennms.features.ifttt.config;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class IfTttConfigTest extends XmlTestNoCastor<IfTttConfig> {

    public IfTttConfigTest(final IfTttConfig sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {

        IfTttConfig ifTttConfig = new IfTttConfig();
        ifTttConfig.setKey("key");
        ifTttConfig.setEnabled(true);
        ifTttConfig.setPollInterval(30L);

        /**
         * Package 1
         */
        TriggerPackage triggerPackage1 = new TriggerPackage();
        triggerPackage1.setCategoryFilter("Package1");
        triggerPackage1.setOnlyUnacknowledged(true);

        TriggerSet triggerSet11 = new TriggerSet();
        triggerSet11.setName("11");
        Trigger trigger111 = new Trigger();
        trigger111.setEventName("111");
        trigger111.setDelay(111);
        trigger111.setValue1("value1111");
        trigger111.setValue2("value1112");
        trigger111.setValue3("value1113");
        triggerSet11.getTriggers().add(trigger111);
        Trigger trigger112 = new Trigger();
        trigger112.setEventName("112");
        trigger112.setDelay(112);
        trigger112.setValue1("value1121");
        trigger112.setValue2("value1122");
        trigger112.setValue3("value1123");
        triggerSet11.getTriggers().add(trigger112);

        triggerPackage1.getTriggerSets().add(triggerSet11);

        TriggerSet triggerSet12 = new TriggerSet();
        triggerSet12.setName("12");
        Trigger trigger121 = new Trigger();
        trigger121.setEventName("121");
        trigger121.setDelay(121);
        trigger121.setValue1("value1211");
        trigger121.setValue2("value1212");
        trigger121.setValue3("value1213");
        triggerSet12.getTriggers().add(trigger121);
        Trigger trigger122 = new Trigger();
        trigger122.setEventName("122");
        trigger122.setDelay(122);
        trigger122.setValue1("value1221");
        trigger122.setValue2("value1222");
        trigger122.setValue3("value1223");
        triggerSet12.getTriggers().add(trigger122);

        triggerPackage1.getTriggerSets().add(triggerSet12);

        ifTttConfig.getTriggerPackages().add(triggerPackage1);

        TriggerPackage triggerPackage2 = new TriggerPackage();
        triggerPackage2.setCategoryFilter("Foo2");
        triggerPackage2.setReductionKeyFilter("uei.*");
        triggerPackage2.setOnlyUnacknowledged(true);

        TriggerSet triggerSet21 = new TriggerSet();
        triggerSet21.setName("21");
        Trigger trigger211 = new Trigger();
        trigger211.setEventName("211");
        trigger211.setDelay(211);
        trigger211.setValue1("value2111");
        trigger211.setValue2("value2112");
        trigger211.setValue3("value2113");
        triggerSet21.getTriggers().add(trigger211);
        Trigger trigger212 = new Trigger();
        trigger212.setEventName("212");
        trigger212.setDelay(212);
        trigger212.setValue1("value2121");
        trigger212.setValue2("value2122");
        trigger212.setValue3("value2123");
        triggerSet21.getTriggers().add(trigger212);

        triggerPackage2.getTriggerSets().add(triggerSet21);

        TriggerSet triggerSet22 = new TriggerSet();
        triggerSet22.setName("22");
        Trigger trigger221 = new Trigger();
        trigger221.setEventName("221");
        trigger221.setDelay(221);
        trigger221.setValue1("value2211");
        trigger221.setValue2("value2212");
        trigger221.setValue3("value2213");
        triggerSet22.getTriggers().add(trigger221);
        Trigger trigger222 = new Trigger();
        trigger222.setEventName("222");
        trigger222.setDelay(222);
        trigger222.setValue1("value2221");
        trigger222.setValue2("value2222");
        trigger222.setValue3("value2223");
        triggerSet22.getTriggers().add(trigger222);

        triggerPackage2.getTriggerSets().add(triggerSet22);

        ifTttConfig.getTriggerPackages().add(triggerPackage2);

        return Arrays.asList(new Object[][]{
                {
                        ifTttConfig,
                        "<ifttt-config enabled=\"true\" key=\"key\" pollInterval=\"30\">\n" +
                                "   <trigger-package categoryFilter=\"Package1\" onlyUnacknowledged=\"true\" reductionKeyFilter=\"\">\n" +
                                "      <trigger-set name=\"11\">\n" +
                                "         <trigger delay=\"111\" eventName=\"111\">\n" +
                                "            <value1>value1111</value1>\n" +
                                "            <value2>value1112</value2>\n" +
                                "            <value3>value1113</value3>\n" +
                                "         </trigger>\n" +
                                "         <trigger delay=\"112\" eventName=\"112\">\n" +
                                "            <value1>value1121</value1>\n" +
                                "            <value2>value1122</value2>\n" +
                                "            <value3>value1123</value3>\n" +
                                "         </trigger>\n" +
                                "      </trigger-set>\n" +
                                "      <trigger-set name=\"12\">\n" +
                                "         <trigger delay=\"121\" eventName=\"121\">\n" +
                                "            <value1>value1211</value1>\n" +
                                "            <value2>value1212</value2>\n" +
                                "            <value3>value1213</value3>\n" +
                                "         </trigger>\n" +
                                "         <trigger delay=\"122\" eventName=\"122\">\n" +
                                "            <value1>value1221</value1>\n" +
                                "            <value2>value1222</value2>\n" +
                                "            <value3>value1223</value3>\n" +
                                "         </trigger>\n" +
                                "      </trigger-set>\n" +
                                "   </trigger-package>\n" +
                                "   <trigger-package categoryFilter=\"Foo2\" onlyUnacknowledged=\"true\" reductionKeyFilter=\"uei.*\">\n" +
                                "      <trigger-set name=\"21\">\n" +
                                "         <trigger delay=\"211\" eventName=\"211\">\n" +
                                "            <value1>value2111</value1>\n" +
                                "            <value2>value2112</value2>\n" +
                                "            <value3>value2113</value3>\n" +
                                "         </trigger>\n" +
                                "         <trigger delay=\"212\" eventName=\"212\">\n" +
                                "            <value1>value2121</value1>\n" +
                                "            <value2>value2122</value2>\n" +
                                "            <value3>value2123</value3>\n" +
                                "         </trigger>\n" +
                                "      </trigger-set>\n" +
                                "      <trigger-set name=\"22\">\n" +
                                "         <trigger delay=\"221\" eventName=\"221\">\n" +
                                "            <value1>value2211</value1>\n" +
                                "            <value2>value2212</value2>\n" +
                                "            <value3>value2213</value3>\n" +
                                "         </trigger>\n" +
                                "         <trigger delay=\"222\" eventName=\"222\">\n" +
                                "            <value1>value2221</value1>\n" +
                                "            <value2>value2222</value2>\n" +
                                "            <value3>value2223</value3>\n" +
                                "         </trigger>\n" +
                                "      </trigger-set>\n" +
                                "   </trigger-package>\n" +
                                "</ifttt-config>",
                        null
                }
        });
    }

    @Test
    public void testUnsetCategoryFilter() {
        TriggerPackage triggerPackage = new TriggerPackage();
        Assert.assertEquals("", triggerPackage.getCategoryFilter());
    }

    @Test
    public void testUnsetReductionKeyFilter() {
        TriggerPackage triggerPackage = new TriggerPackage();
        Assert.assertEquals("", triggerPackage.getReductionKeyFilter());
    }

    @Test
    public void testSetCategoryFilter() {
        TriggerPackage triggerPackage = new TriggerPackage();
        triggerPackage.setCategoryFilter("foo|bar");
        Assert.assertEquals("foo|bar", triggerPackage.getCategoryFilter());
    }

    @Test
    public void testSetReductionKeyFilter() {
        TriggerPackage triggerPackage = new TriggerPackage();
        triggerPackage.setReductionKeyFilter("uei.*");
        Assert.assertEquals("uei.*", triggerPackage.getReductionKeyFilter());
    }
}

