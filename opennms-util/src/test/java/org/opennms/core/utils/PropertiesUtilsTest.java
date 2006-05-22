//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.core.utils;

import java.util.Properties;

import junit.framework.TestCase;

public class PropertiesUtilsTest extends TestCase {

    private Properties m_props;

    protected void setUp() throws Exception {
        m_props = new Properties();
        m_props.setProperty("prop.one", "one");
        m_props.setProperty("prop.two", "two");
        m_props.setProperty("prop.three", "3");
        m_props.setProperty("prop.four", "${prop.three}+1");
        m_props.setProperty("prop.five", "${prop.three}+${prop.two}");
        m_props.setProperty("prop.six", "${prop.five}+${prop.one}");
        m_props.setProperty("prop.infinite1", "${prop.infinite1}");
        m_props.setProperty("prop.infinite2", "calling ${prop.infinite5}");
        m_props.setProperty("prop.infinite3", "call ${prop.infinite2} again");
        m_props.setProperty("prop.infinite4", "x${prop.three}+${prop.infinite3}x");
        m_props.setProperty("prop.infinite5", "call ${prop.infinite4} ");
        
        
    }

    protected void tearDown() throws Exception {
    }
    
    public void testNull() {
        assertNull(PropertiesUtils.substitute(null, m_props));
    }

    public void testNoSubstitution() {
        assertEquals("nosubst", PropertiesUtils.substitute("nosubst", m_props));
        assertEquals("no${subst", PropertiesUtils.substitute("no${subst", m_props));
        assertEquals("no}subst", PropertiesUtils.substitute("no}subst", m_props));
        assertEquals("no${no.such.property}subst", PropertiesUtils.substitute("no${no.such.property}subst", m_props));
    }
    
    public void testSingleSubstitution() {
        assertEquals("xonex", PropertiesUtils.substitute("x${prop.one}x", m_props));
        assertEquals("onebegin", PropertiesUtils.substitute("${prop.one}begin", m_props));
        assertEquals("endone", PropertiesUtils.substitute("end${prop.one}", m_props));
    }
    
    public void testMultiSubstition() {
        assertEquals("xoneytwoz", PropertiesUtils.substitute("x${prop.one}y${prop.two}z", m_props));
        assertEquals("wonextwoy3z", PropertiesUtils.substitute("w${prop.one}x${prop.two}y${prop.three}z", m_props));
        assertEquals("onetwo3", PropertiesUtils.substitute("${prop.one}${prop.two}${prop.three}", m_props));
    }
    
    public void testRecursiveSubstitution() {
        assertEquals("a3+1b", PropertiesUtils.substitute("a${prop.four}b", m_props));
        assertEquals("a3+twob", PropertiesUtils.substitute("a${prop.five}b", m_props));
        assertEquals("a3+two+oneb", PropertiesUtils.substitute("a${prop.six}b", m_props));
    }
    
    public void testSimpleInfiniteRecursion() {
        try {
            String val = PropertiesUtils.substitute("a${prop.infinite1}b", m_props);
            fail("Unexpected value "+val);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().indexOf("prop.infinite1") >= 0);
        }
    }

    public void testComplexInfiniteRecursion() {
        try {
            String val = PropertiesUtils.substitute("a${prop.infinite5}b", m_props);
            fail("Unexpected value "+val);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().indexOf("prop.infinite5") >= 0);
        }
    }
}
