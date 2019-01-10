/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;

public class MockAcknowledgmentDao extends AbstractMockDao<OnmsAcknowledgment, Integer> implements AcknowledgmentDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsAcknowledgment ack) {
        ack.setId(m_id.incrementAndGet());
    }

    @Override
    public Integer getId(final OnmsAcknowledgment ack) {
        return ack.getId();
    }

    @Override
    public List<Acknowledgeable> findAcknowledgables(final OnmsAcknowledgment ack) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void updateAckable(final Acknowledgeable ackable) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void processAck(final OnmsAcknowledgment ack) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void processAcks(final Collection<OnmsAcknowledgment> acks) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsAcknowledgment> findLatestAcks() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Optional<OnmsAcknowledgment> findLatestAckForRefId(Integer refId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
