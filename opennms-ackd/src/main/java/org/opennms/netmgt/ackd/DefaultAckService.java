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
package org.opennms.netmgt.ackd;

import java.util.List;

import org.opennms.netmgt.dao.AcknowledgementDao;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.Acknowledgment;

public class DefaultAckService implements AckService {
    
    private AcknowledgementDao m_ackDao;

    public void proccessAck(List<Acknowledgment> acks) {
        for (Acknowledgment ack : acks) {
            processAck(ack);
        }
    }

    public void processAck(Acknowledgment ack) {
        boolean hasAcknowledged = false;
        List<Acknowledgeable> ackables = m_ackDao.findAcknowledgable(ack);
        for (Acknowledgeable ackable : ackables) {
            ackable.acknowledge(ack.getAckTime(), ack.getAckUser());
            m_ackDao.updateAckable(ackable);
            hasAcknowledged = true;
        }
        
        if (hasAcknowledged) {
            m_ackDao.save(ack);
        }
    }

    public void setAckDao(AcknowledgementDao ackDao) {
        m_ackDao = ackDao;
    }

    public AcknowledgementDao getAckDao() {
        return m_ackDao;
    }

}
