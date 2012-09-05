/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
 * @version $Id: $
 */
public class DefaultAckService implements AckService {
    
    private AcknowledgmentDao m_ackDao;
    
    /** {@inheritDoc} */
    public void processAcks(Collection<OnmsAcknowledgment> acks) {
        log().info("processAcks: Processing "+acks.size()+" acknowledgements...");
        for (OnmsAcknowledgment ack : acks) {
            processAck(ack);
        }
    }

    /** {@inheritDoc} */
    public void processAck(OnmsAcknowledgment ack) {
        log().info("processAck: Searching DB for acknowledgables for ack: "+ack);
        List<Acknowledgeable> ackables = m_ackDao.findAcknowledgables(ack);
        
        if (ackables == null || ackables.size() < 1) {
            log().debug("processAck: No acknowledgables found.");
            throw new IllegalStateException("No acknowlegables in the database for ack: "+ack);
        }

        log().debug("processAck: Found "+ackables.size()+". Acknowledging...");
        
        Iterator<Acknowledgeable> it = ackables.iterator();
        while (it.hasNext()) {
            try {
                Acknowledgeable ackable = it.next();

                switch (ack.getAckAction()) {
                case ACKNOWLEDGE:
                    log().debug("processAck: Acknowledging ackable: "+ackable+"...");
                    ackable.acknowledge(ack.getAckUser());
                    log().debug("processAck: Acknowledged ackable: "+ackable);
                    break;
                case UNACKNOWLEDGE:
                    log().debug("processAck: Unacknowledging ackable: "+ackable+"...");
                    ackable.unacknowledge(ack.getAckUser());
                    log().debug("processAck: Unacknowledged ackable: "+ackable);
                    break;
                case CLEAR:
                    log().debug("processAck: Clearing ackable: "+ackable+"...");
                    ackable.clear(ack.getAckUser());
                    log().debug("processAck: Cleared ackable: "+ackable);
                    break;
                case ESCALATE:
                    log().debug("processAck: Escalating ackable: "+ackable+"...");
                    ackable.escalate(ack.getAckUser());
                    log().debug("processAck: Escalated ackable: "+ackable);
                    break;
                default:
                    break;
                }

                m_ackDao.updateAckable(ackable);
                m_ackDao.save(ack);
                m_ackDao.flush();
                
            } catch (Throwable t) {
                log().error("processAck: exception while processing: "+ack+"; "+t, t);
            }
            
        }
        log().info("processAck: Found and processed acknowledgables for the acknowledgement: "+ack);
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

    /**
     * <p>setAckDao</p>
     *
     * @param ackDao a {@link org.opennms.netmgt.dao.AcknowledgmentDao} object.
     */
    public void setAckDao(AcknowledgmentDao ackDao) {
        m_ackDao = ackDao;
    }

    /**
     * <p>getAckDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.AcknowledgmentDao} object.
     */
    public AcknowledgmentDao getAckDao() {
        return m_ackDao;
    }

}
