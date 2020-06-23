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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SituationFeedbackRestServiceImpl implements SituationFeedbackRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SituationFeedbackRestServiceImpl.class);

    private final AlarmDao alarmDao;

    private final FeedbackRepository repository;

    public SituationFeedbackRestServiceImpl(AlarmDao alarmDao, FeedbackRepository feedbackRepository) {
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.repository = Objects.requireNonNull(feedbackRepository);
    }

    @Override
    public Collection<String> getTags(String prefix) {
        try {
            return repository.getTags(prefix);
        } catch (FeedbackException e) {
            LOG.error("Error retrieving tags for [{}]: {}", prefix, e.getMessage());
            return Collections.emptyList();
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
    }
}
