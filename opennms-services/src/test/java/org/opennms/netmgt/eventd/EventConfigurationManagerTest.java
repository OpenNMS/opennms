package org.opennms.netmgt.eventd;

import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.test.ConfigurationTestUtils;

import junit.framework.TestCase;

public class EventConfigurationManagerTest extends TestCase {
    public void testLoadConfigurationSingleConfig() throws Exception {
        EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/singleConfig/eventconf.xml"));
    }

    public void testLoadConfigurationTwoDeepConfig() throws Exception {
        EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/twoDeepConfig/eventconf.xml"));
    }

    public void testLoadConfigurationThreeDeepConfig() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/threeDeepConfig/eventconf.xml"));
        } catch (ValidationException e) {
            if (e.getMessage().contains("cannot include other configuration files")) {
                caughtExceptionThatWeWanted = true;
            } else {
                throw e;
            }
        }
        
        if (!caughtExceptionThatWeWanted) {
            fail("Did not get the exception that we wanted");
        }
    }
    
    public void testLoadConfigurationTwoDeepConfigWithGlobal() throws Exception {
        boolean caughtExceptionThatWeWanted = false;
        
        try {
            EventConfigurationManager.loadConfiguration(ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/eventd/twoDeepConfigWithGlobal/eventconf.xml"));
        } catch (ValidationException e) {
            if (e.getMessage().contains("cannot have a 'global' element")) {
                caughtExceptionThatWeWanted = true;
            } else {
                throw e;
            }
        }
        
        if (!caughtExceptionThatWeWanted) {
            fail("Did not get the exception that we wanted");
        }
    }
}
