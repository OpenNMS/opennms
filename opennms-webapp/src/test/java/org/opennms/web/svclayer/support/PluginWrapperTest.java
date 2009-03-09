package org.opennms.web.svclayer.support;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy;


public class PluginWrapperTest {
    
    @Test
    public void testChoices() throws Exception {
        String policyClass = MatchingSnmpInterfacePolicy.class.getName();
        
        PluginWrapper wrapper = new PluginWrapper(policyClass);
        System.err.println(wrapper.getChoices().get("action"));
        
        
    }

}
