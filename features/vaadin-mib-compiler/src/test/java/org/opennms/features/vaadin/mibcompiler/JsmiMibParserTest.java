/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.mibcompiler;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.vaadin.mibcompiler.api.MibParser;
import org.opennms.features.vaadin.mibcompiler.services.JsmiMibParser;

/**
 * The Test Class for JsmiMibParser.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class JsmiMibParserTest extends AbstractMibParserTest {

    @Override
    public MibParser getMibParser() {
        return new JsmiMibParser();
    }

    /**
     * Test bad MIB.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBadMib() throws Exception {
        if (parser.parseMib(new File(MIB_DIR, "SONUS-COMMON-MIB.txt"))) {
            Assert.fail();
        } else {
            List<String> dependencies = parser.getMissingDependencies();
            Assert.assertEquals(4, dependencies.size());
            Assert.assertNotNull(parser.getFormattedErrors());
            Assert.assertEquals("[SNMPv2-SMI, SNMPv2-TC, SONUS-SMI, SONUS-TC]", dependencies.toString());
        }
    }

}
