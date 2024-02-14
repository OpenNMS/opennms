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
package org.opennms.netmgt.config.wsman.credentials;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class WsmanConfigTest extends XmlTestNoCastor<WsmanConfig> {

    public WsmanConfigTest(WsmanConfig sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getWsmanConfig(),
                    new File("src/test/resources/wsman-config.xml"),
                    "target/classes/xsds/wsman-config.xsd"
                }
        });
    }


    @Test(expected = Exception.class)
    public void badConfigFailsValidation() throws IOException, Exception {
        validateXmlString(Files.readString(Path.of("src/test/resources/wsman-config-bad.xml")));
    }

    private static WsmanConfig getWsmanConfig() {
        WsmanConfig wsmanConfig = new WsmanConfig();
        wsmanConfig.setRetry(2);
        wsmanConfig.setTimeout(1500);
        wsmanConfig.setGssAuth(true);

        Definition definition = new Definition();
        definition.setSsl(false);
        definition.setPort(5985);
        definition.setPath("ws-man");
        definition.setUsername("Administrator");
        definition.setPassword("!Administrator#");
        definition.getSpecific().add("172.23.1.2");
        definition.setProductVendor("Microsoft");
        definition.setProductVersion("OS: Vista");
        wsmanConfig.getDefinition().add(definition);

        return wsmanConfig;
    }
}
