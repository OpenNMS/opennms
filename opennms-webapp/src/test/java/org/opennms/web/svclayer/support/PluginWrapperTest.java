package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.policies.NodeCategorySettingPolicy;


public class PluginWrapperTest {
    
    @Test
    public void testChoices() throws Exception {
        PluginWrapper wrapper = new PluginWrapper(MatchingSnmpInterfacePolicy.class);
        assertTrue("required keys must contain matchBehavior", wrapper.getRequiredItems().containsKey("matchBehavior"));
        assertTrue("action must contain DISABLE_COLLECTION", wrapper.getRequiredItems().get("action").contains("DISABLE_COLLECTION"));
    }

    @Test
    public void testRequired() throws Exception {
        PluginWrapper wrapper = new PluginWrapper(NodeCategorySettingPolicy.class);
        assertTrue("category should be required", wrapper.getRequired().get("category"));
        assertFalse("type should not be required", wrapper.getRequired().get("type"));
    }
}
