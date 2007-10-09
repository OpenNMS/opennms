package org.opennms.netmgt.capsd;

public class EventProcessorFactory {
    
    private CapsdDbSyncer m_capsdDbSyncer;
    private PluginManager m_pluginManager;

    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

    public void setPluginManager(PluginManager pluginManager) {
        m_pluginManager = pluginManager;
    }
    
    public SuspectEventProcessor createSuspectEventProcessor(String ifAddress) {
        return new SuspectEventProcessor(m_capsdDbSyncer, m_pluginManager, ifAddress);
    }

    public static SuspectEventProcessor createSuspectEventProcessor(CapsdDbSyncer capsdDbSyncer, PluginManager pluginManager, String ifAddress) {
        return new SuspectEventProcessor(capsdDbSyncer, pluginManager,
                ifAddress);
    }


}
