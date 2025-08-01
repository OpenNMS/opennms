/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.mock;

/**
 * @author jsartin
 *
 * CategoryFactory that can be used to provide categories as needed for Unit tests
 * 
 */

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.CategoryGroup;
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
	
	public MockCategoryFactory() throws IOException {
        this(CATEGORY_CONFIG);
    }
	
	public MockCategoryFactory(final String config) throws IOException {
        m_config = JaxbUtils.unmarshal(Catinfo.class, config);
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
	       for (final CategoryGroup cg : m_config.getCategoryGroups()) {
	           for (final Category cat : cg.getCategories()) {
	                if (cat.getLabel().equals(name)) {
	                    return cat;
	                }
	            }
	        }

	        return null;
	    }
	   
    @Override
	   public synchronized String getEffectiveRule(final String catlabel) {
	       for (final CategoryGroup cg : m_config.getCategoryGroups()) {
	           for (final Category cat : cg.getCategories()) {
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
