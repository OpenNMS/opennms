package org.opennms.netmgt.capsd;


public class DefaultProcessorFactory implements SuspectEventProcessorFactory, RescanProcessorFactory {
    
    private CapsdDbSyncer m_capsdDbSyncer;
    private PluginManager m_pluginManager;

    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

    public void setPluginManager(PluginManager pluginManager) {
        m_pluginManager = pluginManager;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.SuspectEventProcessorFactory#createSuspectEventProcessor(java.lang.String)
     */
    public SuspectEventProcessor createSuspectEventProcessor(String ifAddress) {
        return new SuspectEventProcessor(m_capsdDbSyncer, m_pluginManager, ifAddress);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.RescanProcessorFactory#createRescanProcessor(int)
     */
    public RescanProcessor createRescanProcessor(int nodeId) {
        return new RescanProcessor(nodeId, false, m_capsdDbSyncer, m_pluginManager);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.RescanProcessorFactory#createForcedRescanProcessor(int)
     */
    public RescanProcessor createForcedRescanProcessor(int nodeId) {
        return new RescanProcessor(nodeId, true, m_capsdDbSyncer, m_pluginManager);
    }

}
