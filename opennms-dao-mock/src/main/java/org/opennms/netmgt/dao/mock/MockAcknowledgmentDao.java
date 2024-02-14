/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.mock;

import java.util.Collection;
import java.util.Date;
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
    public List<OnmsAcknowledgment> findLatestAcks(Date from) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Optional<OnmsAcknowledgment> findLatestAckForRefId(Integer refId) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
