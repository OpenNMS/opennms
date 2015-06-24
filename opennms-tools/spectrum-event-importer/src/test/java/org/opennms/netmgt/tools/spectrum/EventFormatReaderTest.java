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

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.springframework.core.io.FileSystemResource;

/**
 * @author jeffg
 *
 */
public class EventFormatReaderTest {
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void oneArgConstructor() throws IOException {
        @SuppressWarnings("unused")
        EventFormatReader reader = new EventFormatReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/Eventfff0034e"));
    }
    
    @Test
    public void readCovergenceSystemHaltEventFormat() throws IOException {
        EventFormatReader reader = new EventFormatReader(new FileSystemResource("src/test/resources/sonus-traps/CsEvFormat/Eventfff0034e"));
        EventFormat ef = reader.getEventFormat();
        
        Assert.assertEquals("Check the contents against known good copy",
                            "{d \"%w- %d %m-, %Y - %T\"} - A \"systemHalt\" event has occurred, from {t} device, named {m}.\n" +
                            "\n" +
                            "\"system: report that a system halt has been initiated\"\n" +
                            "\n" +
                            "(event [{e}])\n", ef.getContents());
        
        Assert.assertEquals("Format should have four substitution tokens", 4, ef.getSubstTokens().size());
        Assert.assertEquals("Date stamp substitution token", "{d \"%w- %d %m-, %Y - %T\"}", ef.getSubstTokens().get(0));
        Assert.assertEquals("Model type substitution token", "{t}", ef.getSubstTokens().get(1));
        Assert.assertEquals("Model name substitution token", "{m}", ef.getSubstTokens().get(2));
        Assert.assertEquals("Event code substitution token", "{e}", ef.getSubstTokens().get(3));
    }
}
