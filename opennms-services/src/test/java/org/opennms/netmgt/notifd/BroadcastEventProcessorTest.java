package org.opennms.netmgt.notifd;

import java.util.Collections;

import junit.framework.TestCase;

public class BroadcastEventProcessorTest extends TestCase {

    private BroadcastEventProcessor m_processor;
    
    protected void setUp() {
        m_processor = new BroadcastEventProcessor();
        m_processor.initExpandRe();
    }

    /**
     * Test calling expandNotifParms to see if the regular expression in
     * m_notifdExpandRE is initialized from NOTIFD_EXPANSION_PARM.
     */
    @SuppressWarnings("unchecked")
    public void testExpandNotifParms() {
        m_processor.expandNotifParms("%foo%", Collections.EMPTY_MAP);
    }
}
