/*
 * This file is part of the OpenNMS(R) Application.
*
* OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
* OpenNMS(R) is a derivative work, containing both original code, included code and modified
* code that was published under the GNU General Public License. Copyrights for modified
* and included code are below.
*
* OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
*
* Modifications:
* 
* Created: July 9, 2010
*
* Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*
* For more information contact:
*      OpenNMS Licensing       <license@opennms.org>
*      http://www.opennms.org/
*      http://www.opennms.com/
*/
package org.opennms.netmgt.tools.spectrum;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.LogUtils;
import org.opennms.test.mock.MockLogAppender;
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
