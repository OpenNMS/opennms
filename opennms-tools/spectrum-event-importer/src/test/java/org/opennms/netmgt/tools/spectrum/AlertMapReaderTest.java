/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.test.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

/**
 * @author jeffg
 *
 */
public class AlertMapReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        AlertMapReader reader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
    }
    
    @Test
    public void readSonusAlertMap() throws IOException {
        AlertMapReader reader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
        List<AlertMapping> mappings = reader.getAlertMappings();
        
        int singleVarbind = 0;
        
        Assert.assertEquals("There should exist 751 alert-mappings in this AlertMap", 751, mappings.size());
        
        for (AlertMapping mapping : mappings) {
            if (mapping.getOidMappings().size() == 0) {
                String ec = mapping.getEventCode();
                Assert.assertTrue("Only ten specific alert-mappings should have no OID-mappings; " + ec + " is not one of them", mapping.getEventCode().matches("^0xfff00((17[23cde])|(18[03cde])|(34e))$"));
            }
            
            LogUtils.debugf(this, "Alert-mapping for alert code %s to event code %s has %d OID-mappings", mapping.getAlertCode(), mapping.getEventCode(), mapping.getOidMappings().size());
            if (mapping.getOidMappings().size() == 1) {
                singleVarbind++;
            }
        }

        Assert.assertEquals("86 alert-mapings should have exactly one OID-mapping", 86, singleVarbind);
    }
}
