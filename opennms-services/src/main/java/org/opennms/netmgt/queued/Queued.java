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

package org.opennms.netmgt.queued;

import java.util.Set;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>Queued class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Queued extends AbstractServiceDaemon implements EventListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(Queued.class);

    private static final String LOG4J_CATEGORY = "queued";
    
    private volatile EventIpcManager m_eventMgr; 

    /*
     * There are currently 2 possible strategies to be used here:
     * - QueuingRrdStrategy (the standard behavior)
     * - QueuingTcpRrdStrategy (the modified behavior when org.opennms.rrd.usetcp=true)
     * This is the reason why we should use an indirect reference, otherwise we will experiment NMS-4989
     */
    private volatile RrdStrategy<?,?> m_rrdStrategy;

    /**
     * <p>Constructor for Queued.</p>
     */
    public Queued() {
        super(LOG4J_CATEGORY);
    }
    
    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    /**
     * <p>getRrdStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public RrdStrategy<?,?> getRrdStrategy() {
        return m_rrdStrategy;
    }

    /**
     * <p>setRrdStrategy</p>
     *
     * @param rrdStrategy a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public void setRrdStrategy(RrdStrategy<?,?> rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }
    
    
    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        Assert.state(m_eventMgr != null, "setEventIpcManager must be set");
        Assert.state(m_rrdStrategy != null, "rrdStrategy must be set");
        
        
        m_eventMgr.addEventListener(this, EventConstants.PROMOTE_QUEUE_DATA_UEI);
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(Event e) {
        String fileList = EventUtils.getParm(e, EventConstants.PARM_FILES_TO_PROMOTE);
        Set<String> files = commaDelimitedListToSet(fileList);

        logFilePromotion(files);
        
        m_rrdStrategy.promoteEnqueuedFiles(files);
    }

    private Set<String> commaDelimitedListToSet(String fileList) {
        return StringUtils.commaDelimitedListToSet(fileList);
    }
    
    private void logFilePromotion(Set<String> files) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        
        for(String file : files) {
            LOG.debug("Promoting file: {}", file);
        }
    }

    public static String getLoggingCateogy() {
        return LOG4J_CATEGORY;
    }
}
