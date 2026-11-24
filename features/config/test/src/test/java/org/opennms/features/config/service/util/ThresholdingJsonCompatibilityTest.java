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
package org.opennms.features.config.service.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.FilterOperator;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Parameter;
import org.opennms.netmgt.config.threshd.ResourceFilter;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.io.Resources;

/**
 * The threshold model maps to configuration manager JSON through Jackson annotations on the model classes, which
 * reproduce the JAXB names (singular list elements, camelCase attributes, {@code @XmlEnumValue} constants). It used
 * to rely on a JAXB annotation module registered in the shared {@link ConfigConvertUtil} mapper, which also changed
 * the mapping of every other configuration stored there.
 *
 * <p>The files under {@code thresholding-json/} were written by that earlier mapper for the configurations built
 * below. Matching them byte for byte means configurations already stored in the database keep loading and saving
 * unchanged. The XML tests cover the upgrade path: XML imported through the XSD, then read with
 * {@link ConfigConvertUtil}.</p>
 */
public class ThresholdingJsonCompatibilityTest {

    @Test
    public void threshdJsonIsUnchanged() throws Exception {
        assertStoredJson("threshd-configuration.json", fullThreshdConfiguration(), ThreshdConfiguration.class);
    }

    @Test
    public void thresholdingJsonIsUnchanged() throws Exception {
        assertStoredJson("thresholding-config.json", fullThresholdingConfig(), ThresholdingConfig.class);
    }

    @Test
    public void filterOperatorIsReadCaseInsensitively() {
        for (final String value : new String[]{"and", "AND", "And"}) {
            final ThresholdingConfig config = ConfigConvertUtil.jsonToObject(
                    "{\"group\":[{\"name\":\"g\",\"rrdRepository\":\"/r\",\"threshold\":[{\"ds-name\":\"x\",\"filterOperator\":\""
                            + value + "\"}]}]}",
                    ThresholdingConfig.class);
            assertThat(config.getGroup("g").getThresholds().get(0).getFilterOperator(), equalTo(FilterOperator.AND));
        }
    }

    @Test
    public void threshdXmlImportedThroughTheSchemaReadsBackUnchanged() throws IOException {
        assertXmlImportRoundTrips("threshd-configuration.xml", "threshd-configuration", ThreshdConfiguration.class);
    }

    @Test
    public void thresholdingXmlImportedThroughTheSchemaReadsBackUnchanged() throws IOException {
        assertXmlImportRoundTrips("thresholds.xml", "thresholding-config", ThresholdingConfig.class);
    }

    private static <T> void assertStoredJson(final String resource, final T config, final Class<T> type) throws Exception {
        final String stored = Resources.toString(Resources.getResource("thresholding-json/" + resource), StandardCharsets.UTF_8).trim();
        final String written = ConfigConvertUtil.objectToJson(config);

        JSONAssert.assertEquals(stored, written, true);
        assertThat("property order is unchanged too", written, equalTo(stored));
        assertThat("stored JSON reads back to the same object", ConfigConvertUtil.jsonToObject(stored, type), equalTo(config));
    }

    private static <T> void assertXmlImportRoundTrips(final String resource, final String rootElement, final Class<T> type) throws IOException {
        final String xml = Resources.toString(Resources.getResource(resource), StandardCharsets.UTF_8);
        // Same conversion the upgrade's importConfig runs, built from the definition importSchemaFromXsd registers.
        final ConfigDefinition definition = XsdHelper.buildConfigDefinition("test", "thresholding.xsd", rootElement, "/rest/cm", false);
        final String importedJson = XsdHelper.getConverter(definition).xmlToJson(xml);

        final T fromXml = JaxbUtils.unmarshal(type, xml);
        final T fromJson = ConfigConvertUtil.jsonToObject(importedJson, type);
        assertThat(fromJson, equalTo(fromXml));
        assertThat(ConfigConvertUtil.jsonToObject(ConfigConvertUtil.objectToJson(fromJson), type), equalTo(fromXml));
    }

    private static ThreshdConfiguration fullThreshdConfiguration() {
        final ThreshdConfiguration config = new ThreshdConfiguration();
        for (final ServiceStatus status : ServiceStatus.values()) {
            final Package pkg = new Package();
            pkg.setName("pkg-" + status);
            pkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
            pkg.addSpecific("192.0.2.1");
            final IncludeRange include = new IncludeRange();
            include.setBegin("192.0.2.0");
            include.setEnd("192.0.2.255");
            pkg.addIncludeRange(include);
            final ExcludeRange exclude = new ExcludeRange();
            exclude.setBegin("192.0.2.100");
            exclude.setEnd("192.0.2.110");
            pkg.addExcludeRange(exclude);
            pkg.addIncludeUrl("file:/opt/opennms/etc/include");
            final Service service = new Service();
            service.setName("SNMP");
            service.setInterval(300000L);
            service.setUserDefined(Boolean.FALSE);
            service.setStatus(status);
            final Parameter parameter = new Parameter();
            parameter.setKey("thresholding-group");
            parameter.setValue("mib2");
            service.addParameter(parameter);
            pkg.addService(service);
            pkg.addOutageCalendar("maintenance");
            config.addPackage(pkg);
        }
        return config;
    }

    private static ThresholdingConfig fullThresholdingConfig() {
        final ThresholdingConfig config = new ThresholdingConfig();
        final Group group = new Group();
        group.setName("mib2");
        group.setRrdRepository("/opt/opennms/share/rrd/snmp/");
        for (final ThresholdType type : ThresholdType.values()) {
            final Threshold threshold = new Threshold();
            populate(threshold, type, FilterOperator.AND);
            threshold.setDsName("ifInOctets");
            group.addThreshold(threshold);

            final Expression expression = new Expression();
            populate(expression, type, FilterOperator.OR);
            expression.setExpression("ifInOctets * 8 / ifSpeed");
            group.addExpression(expression);
        }
        config.addGroup(group);
        return config;
    }

    private static void populate(final Basethresholddef def, final ThresholdType type, final FilterOperator operator) {
        def.setRelaxed(Boolean.TRUE);
        def.setDescription("description of " + type);
        def.setType(type);
        def.setDsType("if");
        def.setValue("90.0");
        def.setRearm("75.0");
        def.setTrigger("3");
        def.setDsLabel("ifName");
        def.setExprLabel("utilization");
        def.setTriggeredUEI("uei.opennms.org/test/triggered");
        def.setRearmedUEI("uei.opennms.org/test/rearmed");
        def.setFilterOperator(operator);
        final ResourceFilter filter = new ResourceFilter();
        filter.setField("ifHighSpeed");
        filter.setContent("^[1-9]+[0-9]*$");
        def.addResourceFilter(filter);
    }
}
