package org.opennms.netmgt.correlation.drools;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

public class DroolsCorrelationEngine extends AbstractCorrelationEngine implements InitializingBean {

    private WorkingMemory m_workingMemory;

    public void correlate(Event e) {
        m_workingMemory.assertObject(e);
        m_workingMemory.fireAllRules();
        System.err.println("Finished fireRules for event "+e.getUei());
    }

    public List<String> getInterestingEvents() {
        String[] ueis = {
                EventConstants.REMOTE_NODE_LOST_SERVICE_UEI,
                EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI
        };
        return Arrays.asList(ueis);
    }

    public void afterPropertiesSet() throws Exception {
        PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl( new InputStreamReader( DroolsCorrelationEngine.class.getResourceAsStream( "Correlation.drl" ) ) );

        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

        m_workingMemory = ruleBase.newWorkingMemory();
        m_workingMemory.setGlobal("engine", this);
    }
    
    public int getMemorySize() {
    	return m_workingMemory.getObjects().size();
    }

}
