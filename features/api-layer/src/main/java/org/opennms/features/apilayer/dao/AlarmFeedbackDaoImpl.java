/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.features.situationfeedback.api.AlarmFeedbackListener;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.integration.api.v1.dao.AlarmFeedbackDao;
import org.opennms.integration.api.v1.model.AlarmFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmFeedbackDaoImpl implements AlarmFeedbackDao {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmFeedbackDaoImpl.class);

    /**
     * The repository interface used to persist/retrieve feedback.
     */
    private final FeedbackRepository feedbackRepository;

    /**
     * The collection of listeners interested in alarm feedback, populated via runtime binding.
     */
    private static final Collection<AlarmFeedbackListener> alarmFeedbackListeners = new CopyOnWriteArrayList<>();

    /**
     * @param feedbackRepository the repository for persisting/retrieving feedback
     */
    public AlarmFeedbackDaoImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = Objects.requireNonNull(feedbackRepository);
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
    public List<AlarmFeedback> getFeedback() {
        try {
            return feedbackRepository.getAllFeedback()
                    .stream()
                    .map(ModelMappers::toFeedback)
                    .collect(Collectors.toList());
        } catch (FeedbackException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submitFeedback(List<AlarmFeedback> alarmFeedback) {
        List<org.opennms.features.situationfeedback.api.AlarmFeedback> mappedFeedback = alarmFeedback.stream()
                .map(ModelMappers::fromFeedback)
                .collect(Collectors.toList());

        try {
            feedbackRepository.persist(mappedFeedback);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }

        alarmFeedbackListeners.forEach(listener -> {
            try {
                listener.handleAlarmFeedback(mappedFeedback);
            } catch (Exception e) {
                LOG.warn("Failed to notify listener of alarm feedback", e);
            }
        });
    }
}
