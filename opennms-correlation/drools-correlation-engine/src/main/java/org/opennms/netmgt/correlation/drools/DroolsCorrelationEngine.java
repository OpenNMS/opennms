package org.opennms.netmgt.correlation.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

public class DroolsCorrelationEngine extends AbstractCorrelationEngine {

    private WorkingMemory m_workingMemory;
    private List<String> m_interestingEvents;
    private List<Resource> m_rules;
    private Map<String, Object> m_globals = new HashMap<String, Object>();
    private String m_name;
    
    @Override
    public synchronized void correlate(Event e) {
        System.err.println("Begin correlation for Event " + e.getDbid() + " uei: " + e.getUei());
        m_workingMemory.assertObject(e);
        m_workingMemory.fireAllRules();
        System.err.println("End correlation for Event " + e.getDbid() + " uei: " + e.getUei());
    }

    @Override
    protected synchronized void timerExpired(Integer timerId) {
        System.err.println("Begin processing for Timer " + timerId);
        TimerExpired expiration  = new TimerExpired(timerId);
        m_workingMemory.assertObject(expiration);
        m_workingMemory.fireAllRules();
        System.err.println("End processing for Timer " + timerId);
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
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
        conf.setJavaLanguageLevel( "1.5" );
        PackageBuilder builder = new PackageBuilder( conf );
        
        loadRules(builder);

        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

        m_workingMemory = ruleBase.newWorkingMemory();
        m_workingMemory.setGlobal("engine", this);
        
        for (Map.Entry<String, Object> entry : m_globals.entrySet()) {
            m_workingMemory.setGlobal(entry.getKey(), entry.getValue());
        }

    }

    private void loadRules(PackageBuilder builder) throws DroolsParserException, IOException {
        
        for (Resource rulesFile : m_rules) {
            Reader rdr = null;
            try {
                rdr = new InputStreamReader( rulesFile.getInputStream() );
                builder.addPackageFromDrl( rdr );
            } finally {
                IOUtils.closeQuietly(rdr);
            }
        }
    }
    
    public int getMemorySize() {
    	return m_workingMemory.getObjects().size();
    }
    
    @SuppressWarnings("unchecked")
    public List<Object> getMemoryObjects() {
        return m_workingMemory.getObjects();
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
    
}
