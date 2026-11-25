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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.impl.util.JaxbXmlConverter;
import org.opennms.features.config.dao.impl.util.XmlSchema;
import org.opennms.features.config.service.config.FakeXsdForTest;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.io.Resources;

@JUnitConfigurationEnvironment
public class JaxbXmlConverterTest {
    final static String FOREIGN_SOURCES = "/opt/opennms/etc/foreign-sources";

    @Test
    public void testConverter() throws IOException {
        final JaxbXmlConverter converter = new JaxbXmlConverter("provisiond-configuration.xsd", "provisiond-configuration", null);
        final String sourceXml = Resources.toString(
                Resources.getResource("provisiond-configuration.xml"), StandardCharsets.UTF_8);
        final String expectedJson = Resources.toString(
                Resources.getResource("provisiond.json"), StandardCharsets.UTF_8);

        // Verify the rendered JSON
        final String convertedJson = converter.xmlToJson(sourceXml);
        JSONAssert.assertEquals(expectedJson, convertedJson, true);

        // Verify the object mapped from the rendered JSON
        final ProvisiondConfiguration objectFromMappedJson = ConfigConvertUtil.jsonToObject(convertedJson, ProvisiondConfiguration.class);

        Assert.assertEquals("json importThreads Value is not correct", 11L, (long) objectFromMappedJson.getImportThreads());
        Assert.assertEquals("json foreign-source-dir is not correct. " + objectFromMappedJson.getForeignSourceDir(), FOREIGN_SOURCES, objectFromMappedJson.getForeignSourceDir());

        // ProvisiondConfiguration has no JAXB annotations (it is loaded through the config manager as JSON),
        // so JaxbUtils.unmarshal() cannot be used as a reference. Compare against the values in the source XML instead.
        final ProvisiondConfiguration expected = expectedProvisiondConfiguration();
        // Compare the JSON forms first: the model classes have no toString(), so a failure here shows which field differs
        JSONAssert.assertEquals(ConfigConvertUtil.objectToJson(expected), ConfigConvertUtil.objectToJson(objectFromMappedJson), true);
        assertThat(objectFromMappedJson, equalTo(expected));
    }

    private static ProvisiondConfiguration expectedProvisiondConfiguration() {
        final ProvisiondConfiguration config = new ProvisiondConfiguration();
        config.setImportThreads(11L);
        config.setScanThreads(10L);
        config.setRescanThreads(10L);
        config.setWriteThreads(8L);
        config.setRequistionDir("/opt/opennms/etc/imports");
        config.setForeignSourceDir(FOREIGN_SOURCES);
        config.addRequisitionDef(requisitionDef("localhost", "dns://localhost/localhost"));
        config.addRequisitionDef(requisitionDef("the-import-name-to-use", "vmware://ip-address-or-hostname/foreignSource-Name?parameters"));
        return config;
    }

    private static RequisitionDef requisitionDef(final String importName, final String importUrlResource) {
        final RequisitionDef def = new RequisitionDef();
        def.setImportName(importName);
        def.setImportUrlResource(importUrlResource);
        def.setCronSchedule("0 0 0 * * ? *");
        return def;
    }

    @Test
    public void testXsdSearch() throws IOException {
        // check if xsd is not located in xsds path
        JaxbXmlConverter converter = new JaxbXmlConverter("trapd-configuration.xsd", "trapd-configuration", null);
        XmlSchema schema = converter.getXmlSchema();
        Assert.assertNotNull("Fail to find schema!!!", schema);
    }

    @Test
    public void testValidate() {
        FakeXsdForTest test = new FakeXsdForTest(1024, "127.0.0.1");
        Snmpv3User user = new Snmpv3User();
        user.setSecurityName("SecurityName");
        test.addSnmpv3User(user);
        test.setUseAddressFromVarbind(true);
        String xmlStr = JaxbUtils.marshal(test);

        FakeXsdForTest convertedTest = JaxbUtils.unmarshal(FakeXsdForTest.class, xmlStr);
        Assert.assertEquals("Trap port is wrong after conversion!",
                1024, convertedTest.getSnmpTrapPort());
        Assert.assertEquals("SnmpTrapAddress is wrong after conversion!",
                "127.0.0.1", convertedTest.getSnmpTrapAddress());
        Assert.assertEquals("Snmpv3User is wrong after conversion!",
                "SecurityName", convertedTest.getSnmpv3User(0).getSecurityName());
    }
}