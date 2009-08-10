/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.acknowledgments.AckService;

/**
 * This class provides the work of acknowledging <code>Acknowledgables</code> associated with
 * an <code>Acknowledgment</code>.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class DefaultAckService implements AckService {
    
    //@Autowired
    private AcknowledgmentDao m_ackDao;
    
    public void processAcks(Collection<OnmsAcknowledgment> acks) {
        log().info("processAcks: Processing "+acks.size()+" acknowledgements...");
        for (OnmsAcknowledgment ack : acks) {
            processAck(ack);
        }
    }

    public void processAck(OnmsAcknowledgment ack) {
        log().debug("processAck: Searching DB for acknowledgables for ack: "+ack);
        List<Acknowledgeable> ackables = m_ackDao.findAcknowledgables(ack);
        
        if (ackables == null || ackables.size() < 1) {
            log().debug("processAck: No acknowledgables found.");
            throw new IllegalStateException("No acknowlegables in the database for ack: "+ack);
        }

        log().debug("processAck: Found "+ackables.size()+". Acknowledging...");
        for (Acknowledgeable ackable : ackables) {
            switch (ack.getAckAction()) {
            case ACKNOWLEDGE:
                log().debug("processAck: Acknowledging ackable: "+ackable);
                ackable.acknowledge(ack.getAckUser());
                break;
            case UNACKNOWLEDGE:
                log().debug("processAck: Unacknowledging ackable: "+ackable);
                ackable.unacknowledge(ack.getAckUser());
            case CLEAR:
                log().debug("processAck: Clearing ackable: "+ackable);
                ackable.clear(ack.getAckUser());
                break;
            case ESCALATE:
                log().debug("processAck: Escalating ackable: "+ackable);
                ackable.escalate(ack.getAckUser());
            default:
                break;
            }
            
            m_ackDao.updateAckable(ackable);
            m_ackDao.save(ack);
            m_ackDao.flush();
        }
    }

    private Logger log() {
        return ThreadCategory.getInstance();
    }

    public void setAckDao(AcknowledgmentDao ackDao) {
        m_ackDao = ackDao;
    }

    public AcknowledgmentDao getAckDao() {
        return m_ackDao;
    }

}
