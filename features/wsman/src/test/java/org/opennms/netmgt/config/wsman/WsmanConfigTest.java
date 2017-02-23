/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wsman;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

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
                    null
                }
        });
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
