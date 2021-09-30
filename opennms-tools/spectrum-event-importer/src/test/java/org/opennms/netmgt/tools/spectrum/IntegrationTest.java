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
import java.io.PrintWriter;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.springframework.core.io.FileSystemResource;


public class IntegrationTest {
    private List<AlertMapping> m_alertMappings;
    private List<EventDisposition> m_eventDispositions;
    
    @Before
    public void setUp() throws IOException {
        MockLogAppender.setupLogging();
        AlertMapReader amReader = new AlertMapReader(new FileSystemResource("src/test/resources/sonus-traps/AlertMap"));
        EventDispositionReader edReader = new EventDispositionReader(new FileSystemResource("src/test/resources/sonus-traps/EventDisp"));
        m_alertMappings = amReader.getAlertMappings();
        m_eventDispositions = edReader.getEventDispositions();
    }
    
    @Test
    @Ignore
    public void integrateIpUnityInterfaceHighError() throws IOException {
        AlertMapping mapping = null;
        EventDisposition disposition = null;
        EventFormat format = null;
        
        for (AlertMapping thisMapping : m_alertMappings) {
            if (thisMapping.getEventCode().equals("0xfff002af")) {
                mapping = thisMapping;
                break;
            }
        }
        Assert.assertNotNull(mapping);
        Assert.assertEquals("Trap-OID is .1.3.6.1.4.1.5134.1.4", ".1.3.6.1.4.1.5134.1.4", mapping.getTrapOid());
        Assert.assertEquals("Trap generic-type is 0", "0", mapping.getTrapGenericType());
        Assert.assertEquals("Trap specific-type is 298", "298", mapping.getTrapSpecificType());
        
        for (EventDisposition thisDisp : m_eventDispositions) {
            if (thisDisp.getEventCode().equals("0xfff002af")) {
                disposition = thisDisp;
                break;
            }
        }
        Assert.assertNotNull(disposition);
        
        EventFormatReader efReader = new EventFormatReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/Eventfff002af"));
        format = efReader.getEventFormat();
        Assert.assertNotNull(format);
    }
    
    @Test
    public void initializeImporterSonusTraps() throws Exception {
        SpectrumTrapImporter importer = new SpectrumTrapImporter();
        importer.setBaseUei("uei.opennms.org/import/Spectrum");
        importer.setCustomEventsDir(new FileSystemResource("src/test/resources/sonus-traps"));
        importer.setModelTypeAssetField("manufacturer");
        importer.setOutputWriter(new PrintWriter(System.out));
        importer.setReductionKeyBody("%dpname%:%uei%:%interface%");
        importer.afterPropertiesSet();
        Assert.assertEquals("751 alert-mappings", 751, importer.getAlertMappings().size());
        Assert.assertEquals("757 event-dispositions", 757, importer.getEventDispositions().size());
        Assert.assertEquals("677 event-formats", 677, importer.getEventFormats().keySet().size());
        for (EventFormat fmt : importer.getEventFormats().values()) {
            Assert.assertTrue("Event format's associated code looks kosher", fmt.getEventCode().matches("0x.*"));
            Assert.assertTrue("Event format contents more than ten characters long", fmt.getContents().length() > 10);
        }
    }
}
