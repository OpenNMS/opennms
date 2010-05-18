/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 30, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.correlation.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 *
 */
public class DroolsCorrelationEngine extends AbstractCorrelationEngine {

    private WorkingMemory m_workingMemory;
    private List<String> m_interestingEvents;
    private List<Resource> m_rules;
    private Map<String, Object> m_globals = new HashMap<String, Object>();
    private String m_name;
    
    @Override
    public synchronized void correlate(Event e) {
        log().info("Begin correlation for Event " + e.getDbid() + " uei: " + e.getUei());
        m_workingMemory.insert(e);
        m_workingMemory.fireAllRules();
        log().info("End correlation for Event " + e.getDbid() + " uei: " + e.getUei());
    }

    @Override
    protected synchronized void timerExpired(Integer timerId) {
        log().info("Begin processing for Timer " + timerId);
        TimerExpired expiration  = new TimerExpired(timerId);
        m_workingMemory.insert(expiration);
        m_workingMemory.fireAllRules();
        log().info("End processing for Timer " + timerId);
    }

    @Override
    public List<String> getInterestingEvents() {
        return m_interestingEvents;
    }
    
    public void setInterestingEvents(List<String> ueis) {
        m_interestingEvents = ueis;
    }
    
    public void setRulesResources(List<Resource> rules) {
        m_rules = rules;
    }
    
    public void setGlobals(Map<String, Object> globals) {
        m_globals = globals;
    }

    public void initialize() throws Exception {
        Properties props = new Properties();
        props.setProperty("drools.dialect.java.compiler.lnglevel", "1.5");
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration(props);
        PackageBuilder builder = new PackageBuilder( conf );
        
        loadRules(builder);

        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

        m_workingMemory = ruleBase.newStatefulSession();
        m_workingMemory.setGlobal("engine", this);
        
        for (Map.Entry<String, Object> entry : m_globals.entrySet()) {
            m_workingMemory.setGlobal(entry.getKey(), entry.getValue());
        }

    }

    private void loadRules(PackageBuilder builder) throws DroolsParserException, IOException {
        
        for (Resource rulesFile : m_rules) {
            Reader rdr = null;
            try {
                rdr = new InputStreamReader( rulesFile.getInputStream(), "UTF-8" );
                builder.addPackageFromDrl( rdr );
            } finally {
                IOUtils.closeQuietly(rdr);
            }
        }
    }
    
    public int getMemorySize() {
        int count = 0;
        for(Iterator<?> it = m_workingMemory.iterateObjects(); it.hasNext(); it.next()) {
            count++;
        }
    	return count;
    }
    
    @SuppressWarnings("unchecked")
    public List<Object> getMemoryObjects() {
        List<Object> objects = new LinkedList<Object>();
        for(Iterator<Object> it = m_workingMemory.iterateObjects(); it.hasNext(); it.next()) {
            
        }
        return objects;
    }

    public void setName(String name) {
        m_name = name;
    }
    
    public String getName() {
        return m_name;
    }

    public void setGlobal(String name, Object value) {
        m_workingMemory.setGlobal(name, value);
    }
    
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
