/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern;

public class LinkPatternTest {

    @Test
    public void testSimplePattern() {
        LinkPattern p = new LinkPattern("foo", "bar");
        
        assertEquals("bar", p.resolveTemplate("foo"));
        assertNull(p.resolveTemplate("monkey"));
    }
    
    @Test
    public void testTemplatePattern() {
        LinkPattern p = new LinkPattern("([a-z]{2})-([a-z]{3})([0-9]{4})-to-([a-z]{3})([0-9]{4})-dwave", "$1-$4$5-to-$2$3-dwave");
        
        assertEquals("nc-ral0002-to-ral0001-dwave", p.resolveTemplate("nc-ral0001-to-ral0002-dwave"));
        assertEquals("nc-ral0001-to-ral0002-dwave", p.resolveTemplate("nc-ral0002-to-ral0001-dwave"));
        assertNull(p.resolveTemplate("nc-fasdfasdfasdf-dwave"));
    }
}
