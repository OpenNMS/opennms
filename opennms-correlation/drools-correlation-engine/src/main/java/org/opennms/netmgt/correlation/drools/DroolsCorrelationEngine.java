package org.opennms.netmgt.correlation.drools;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

import org.apache.commons.io.IOUtils;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

public class DroolsCorrelationEngine extends AbstractCorrelationEngine implements InitializingBean {

    private WorkingMemory m_workingMemory;
    private Integer m_wideSpreadThreshold = 3;
    private Long m_flapInterval = 60000L;
    private Integer m_flapCount = 3;

    public void correlate(Event e) {
        m_workingMemory.assertObject(e);
        m_workingMemory.fireAllRules();
        System.err.println("Finished fireRules for event "+e.getUei());
    }

    @Override
    protected void timerExpired(Integer timerId) {
        TimerExpired expiration  = new TimerExpired(timerId);
        m_workingMemory.assertObject(expiration);
        m_workingMemory.fireAllRules();
        System.err.println("Timer "+timerId+" expired");
        
    }

    public List<String> getInterestingEvents() {
        String[] ueis = {
                EventConstants.REMOTE_NODE_LOST_SERVICE_UEI,
                EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI
        };
        return Arrays.asList(ueis);
    }

    public void afterPropertiesSet() throws Exception {
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration();
        conf.setJavaLanguageLevel( "1.5" );
        PackageBuilder builder = new PackageBuilder( conf );
        
        Reader rdr = null;
        try {
            rdr = new InputStreamReader( DroolsCorrelationEngine.class.getResourceAsStream( "Correlation.drl" ) );
            builder.addPackageFromDrl( rdr );
        } finally {
            IOUtils.closeQuietly(rdr);
        }

        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( builder.getPackage() );

        m_workingMemory = ruleBase.newWorkingMemory();
        m_workingMemory.setGlobal("engine", this);
        m_workingMemory.setGlobal("wideSpreadThreshold", m_wideSpreadThreshold);
        m_workingMemory.setGlobal("flapInterval", m_flapInterval);
        m_workingMemory.setGlobal("flapCount", m_flapCount);
    }
    
    public int getMemorySize() {
    	return m_workingMemory.getObjects().size();
    }

    public Integer getFlapCount() {
        return m_flapCount;
    }

    public void setFlapCount(Integer flapCount) {
        m_flapCount = flapCount;
    }

    public Long getFlapInterval() {
        return m_flapInterval;
    }

    public void setFlapInterval(Long flapInterval) {
        m_flapInterval = flapInterval;
    }

    public Integer getWideSpreadThreshold() {
        return m_wideSpreadThreshold;
    }

    public void setWideSpreadThreshold(Integer wideSpreadThreshold) {
        m_wideSpreadThreshold = wideSpreadThreshold;
    }


}
