/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

/**
 * @author jsartin
 *
 * CategoryFactory that can be used to provide categories as needed for Unit tests
 * 
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;

public class MockCategoryFactory implements CatFactory {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
	
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

    @Override
    public Lock getReadLock() {
        return m_readLock;
    }
    
    @Override
    public Lock getWriteLock() {
        return m_writeLock;
    }

	 /**
     * Return the categories configuration.
     * 
     * @return the categories configuration
     */
    @Override
    public synchronized Catinfo getConfig() {
        return m_config;
    }
	
    @Override
	   public synchronized Category getCategory(final String name) {
	       for (final Categorygroup cg : m_config.getCategorygroupCollection()) {
	           for (final Category cat : cg.getCategories().getCategoryCollection()) {
	                if (cat.getLabel().equals(name)) {
	                    return cat;
	                }
	            }
	        }

	        return null;
	    }
	   
    @Override
	   public synchronized String getEffectiveRule(final String catlabel) {
	       for (final Categorygroup cg : m_config.getCategorygroupCollection()) {
	           for (final Category cat : cg.getCategories().getCategoryCollection()) {
	                if (cat.getLabel().equals(catlabel)) {
	                    return "(" + cg.getCommon().getRule() + ") & (" + cat.getRule() + ")";
	                }
	            }
	        }

	        return null;
	    }

    @Override
	public double getNormal(String catlabel) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
	public double getWarning(String catlabel) {
		// TODO Auto-generated method stub
		return 0;
	}
}
