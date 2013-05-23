/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd;

/**
 * <p>DefaultProcessorFactory class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DefaultProcessorFactory implements SuspectEventProcessorFactory, RescanProcessorFactory {
    
    private CapsdDbSyncer m_capsdDbSyncer;
    private PluginManager m_pluginManager;

    /**
     * <p>setCapsdDbSyncer</p>
     *
     * @param capsdDbSyncer a {@link org.opennms.netmgt.capsd.CapsdDbSyncer} object.
     */
    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

    /**
     * <p>setPluginManager</p>
     *
     * @param pluginManager a {@link org.opennms.netmgt.capsd.PluginManager} object.
     */
    public void setPluginManager(PluginManager pluginManager) {
        m_pluginManager = pluginManager;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.SuspectEventProcessorFactory#createSuspectEventProcessor(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public SuspectEventProcessor createSuspectEventProcessor(String ifAddress) {
        return new SuspectEventProcessor(m_capsdDbSyncer, m_pluginManager, ifAddress);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.RescanProcessorFactory#createRescanProcessor(int)
     */
    /** {@inheritDoc} */
    @Override
    public RescanProcessor createRescanProcessor(int nodeId) {
        return new RescanProcessor(nodeId, false, m_capsdDbSyncer, m_pluginManager);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.RescanProcessorFactory#createForcedRescanProcessor(int)
     */
    /** {@inheritDoc} */
    @Override
    public RescanProcessor createForcedRescanProcessor(int nodeId) {
        return new RescanProcessor(nodeId, true, m_capsdDbSyncer, m_pluginManager);
    }

}
