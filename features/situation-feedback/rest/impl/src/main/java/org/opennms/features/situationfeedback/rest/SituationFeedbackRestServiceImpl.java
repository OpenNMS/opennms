/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
package org.opennms.features.situationfeedback.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedbackListener;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

public class SituationFeedbackRestServiceImpl implements SituationFeedbackRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SituationFeedbackRestServiceImpl.class);

    private final AlarmDao alarmDao;

    private final AlarmEntityNotifier alarmEntityNotifier;

    private final FeedbackRepository repository;

    private final TransactionOperations transactionTemplate;

    /**
     * The collection of listeners interested in alarm feedback, populated via runtime binding.
     */
    private static final Collection<AlarmFeedbackListener> alarmFeedbackListeners = new ArrayList<>();

    public SituationFeedbackRestServiceImpl(AlarmDao alarmDao, AlarmEntityNotifier alarmEntityNotifier, FeedbackRepository feedbackRepository, TransactionOperations transactionOperations) {
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.alarmEntityNotifier = Objects.requireNonNull(alarmEntityNotifier);
        this.repository = Objects.requireNonNull(feedbackRepository);
        this.transactionTemplate = Objects.requireNonNull(transactionOperations);
    }

    public synchronized void onBind(AlarmFeedbackListener alarmFeedbackListener, Map properties) {
        LOG.debug("bind called with {}: {}", alarmFeedbackListener, properties);

        if (alarmFeedbackListener != null) {
            alarmFeedbackListeners.add(alarmFeedbackListener);
        }
    }

    public synchronized void onUnbind(AlarmFeedbackListener alarmFeedbackListener, Map properties) {
        LOG.debug("Unbind called with {}: {}", alarmFeedbackListener, properties);

        if (alarmFeedbackListener != null) {
            alarmFeedbackListeners.remove(alarmFeedbackListener);
        }
    }    

    @Override
    public Collection<AlarmFeedback> getFeedback(int situationId) {
        try {
            return repository.getFeedback(getReductionKey(situationId));
        } catch (FeedbackException e) {
            LOG.error("Error retrieving alarm correlation feedback for [{}]: {}", situationId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getReductionKey(int situationId) throws FeedbackException {
        OnmsAlarm situation = alarmDao.get(situationId);
        if (situation == null) {
            throw new FeedbackException("No Situation found with ID " + situationId);
        }
        return situation.getReductionKey();
    }

    @Override
    public void setFeedback(int situationId, List<AlarmFeedback> feedback) {
        try {
            repository.persist(feedback);
        } catch (Exception e) {
            throw new WebApplicationException("Failed to execute query: " + e.getMessage(), e);
        }

        alarmFeedbackListeners.forEach(listener -> {
            try {
                listener.handleAlarmFeedback(feedback);
            } catch (Exception e) {
                LOG.warn("Failed to notify listener of alarm feedback", e);
            }
        });
    }
}
