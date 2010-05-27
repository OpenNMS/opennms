//This file is part of the OpenNMS(R) Application.
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
package org.opennms.netmgt.mock;

/**
 * @author jsartin
 *
 * CategoryFactory that can be used to provide categories as needed for Unit tests
 * 
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.categories.Categories;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.dao.castor.CastorUtils;

public class MockCategoryFactory implements CatFactory {
	
	private Catinfo m_config;
	
	private static final String CATEGORY_CONFIG = 
		"<catinfo>" +
	    " <header>" +
	    "  <rev>1.3</rev>" +
	    "  <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>" +
	    "  <mstation>checkers</mstation>" +
	    " </header>" +
	    " <categorygroup>" +
	    "  <name>WebConsole</name>" +
	    "  <comment>Service Level Availability by Functional Group</comment>" +
	    "  <common>" +
	    "   <rule><![CDATA[ipaddr IPLIKE *.*.*.*]]></rule>" +
	    "  </common>" +
	    "  <categories>" +
	    "   <category>" +
	    "    <label><![CDATA[Network Interfaces]]></label>" +
	    "    <comment>This is a very simple category</comment>" +
	    "    <normal>99</normal>" +
	    "    <warning>97</warning>" +
	    "    <service>ICMP</service>" +
	    "    <service>SNMP</service>" +
	    "    <rule><![CDATA[(isICMP | isSNMP) & (ipaddr != \"0.0.0.0\")]]></rule>" +
		"   </category>" +
		" <category>" +
	    "    <label><![CDATA[Web Servers]]></label>" +
	    "    <comment>This is a more complex category</comment>" +
	    "    <normal>99</normal>" +
	    "    <warning>97</warning>" +
	    "    <service>HTTP</service>" +
	    "    <service>HTTPS</service>" +
	    "    <rule><![CDATA[isHTTP | isHTTPS]]></rule>" +
		"   </category>" +
		"  </categories>" +
		" </categorygroup>" +
		"</catinfo>";
	
	public MockCategoryFactory() throws MarshalException, ValidationException, IOException {
        this(CATEGORY_CONFIG);
    }
	
	public MockCategoryFactory(String config) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(Catinfo.class, new ByteArrayInputStream(config.getBytes()));
    }

	 /**
     * Return the categories configuration.
     * 
     * @return the categories configuration
     */
    public synchronized Catinfo getConfig() {
        return m_config;
    }
	
	   public synchronized Category getCategory(String name) {
	        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
	        while (enumCG.hasMoreElements()) {
	            Categorygroup cg = enumCG.nextElement();

	            // go through the categories
	            Categories cats = cg.getCategories();

	            Enumeration<Category> enumCat = cats.enumerateCategory();
	            while (enumCat.hasMoreElements()) {
	                Category cat = enumCat.nextElement();
	                if (cat.getLabel().equals(name)) {
	                    return cat;
	                }
	            }
	        }

	        return null;
	    }
	   
	   public synchronized String getEffectiveRule(String catlabel) {
	        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
	        while (enumCG.hasMoreElements()) {
	            Categorygroup cg = enumCG.nextElement();

	            // go through the categories
	            Categories cats = cg.getCategories();

	            Enumeration<Category> enumCat = cats.enumerateCategory();
	            while (enumCat.hasMoreElements()) {
	                Category cat = enumCat.nextElement();
	                if (cat.getLabel().equals(catlabel)) {
	                    String catRule = "(" + cg.getCommon().getRule() + ") & (" + cat.getRule() + ")";
	                    return catRule;
	                }
	            }
	        }

	        return null;
	    }

	public double getNormal(String catlabel) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getWarning(String catlabel) {
		// TODO Auto-generated method stub
		return 0;
	}
}
