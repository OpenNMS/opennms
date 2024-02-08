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
package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;

/**
 * Contract for persisting Acknowledgments
 *
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public interface AcknowledgmentDao extends OnmsDao<OnmsAcknowledgment, Integer> {

    /**
     * <p>findAcknowledgables</p>
     *
     * @param ack a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     * @return a {@link java.util.List} object.
     */
    List<Acknowledgeable> findAcknowledgables(OnmsAcknowledgment ack);

    /**
     * <p>updateAckable</p>
     *
     * @param ackable a {@link org.opennms.netmgt.model.Acknowledgeable} object.
     */
    void updateAckable(Acknowledgeable ackable);
    

    /**
     * <p>processAck</p>
     *
     * @param ack a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     */
    void processAck(OnmsAcknowledgment ack);

    /**
     * <p>processAcks</p>
     *
     * @param acks a {@link java.util.Collection} object.
     */
    void processAcks(Collection<OnmsAcknowledgment> acks);

    /**
     * <p>findLatestAcks</p>
     * 
     * Finds the latest acknowledgement for each refId. The latest acknowledgement is selected based on the most recent
     * ackTime (and highest Id in the case of multiple occuring at the same time).
     *
     * @param from limit results to acks created on or after
     * @return the list of latest acks (empty list in the case of no acks found)
     */
    List<OnmsAcknowledgment> findLatestAcks(Date from);

    /**
     * <p>findLatestAckForRefId</p>
     * 
     * Finds the latest acknowledgement for the given refId. The latest acknowledgement is selected based on the most 
     * recent ackTime (and highest Id in the case of multiple occurring at the same time).
     * 
     * @param refId the refId to search for
     * @return an optional containing the latest ack for the given refId or Optional.empty() if none found
     */
    Optional<OnmsAcknowledgment> findLatestAckForRefId(Integer refId);
}
