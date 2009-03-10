package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy;


public class PluginWrapperTest {
    
    @Test
    public void testChoices() throws Exception {
        String policyClass = MatchingSnmpInterfacePolicy.class.getName();
        
        PluginWrapper wrapper = new PluginWrapper(policyClass);
        assertTrue("required keys must contain matchBehavior", wrapper.getRequired().containsKey("matchBehavior"));
        assertTrue("action must contain DISABLE_COLLECTION", wrapper.getRequired().get("action").contains("DISABLE_COLLECTION"));
    }

}
