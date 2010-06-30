/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Jul 29: Eliminate generics warning in onEvent. - dj@opennms.org
 * 
 * Created: August 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.queued;

import java.util.Set;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>Queued class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Queued extends AbstractServiceDaemon implements EventListener {
    
    private volatile EventIpcManager m_eventMgr; 
    private volatile RrdStrategy m_rrdStrategy;

    /**
     * <p>Constructor for Queued.</p>
     */
    public Queued() {
        super("OpenNMS.Queued");
    }
    
    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    /**
     * <p>setRrdStrategy</p>
     *
     * @param rrdStrategy a {@link org.opennms.netmgt.rrd.RrdStrategy} object.
     */
    public void setRrdStrategy(RrdStrategy rrdStrategy) {
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
    public void onEvent(Event e) {
        String fileList = EventUtils.getParm(e, "filesToPromote");
        Set<String> files = commaDelimitedListToSet(fileList);

        logFilePromotion(files);
        
        m_rrdStrategy.promoteEnqueuedFiles(files);
    }

    @SuppressWarnings("unchecked")
    private Set<String> commaDelimitedListToSet(String fileList) {
        return StringUtils.commaDelimitedListToSet(fileList);
    }
    
    private void logFilePromotion(Set<String> files) {
        if (!log().isDebugEnabled()) {
            return;
        }
        
        for(String file : files) {
            debugf("Promoting file: %s", file);
        }
    }

}
