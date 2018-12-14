/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
     * @return the list of latest acks (empty list in the case of no acks found)
     */
    List<OnmsAcknowledgment> findLatestAcks();

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
