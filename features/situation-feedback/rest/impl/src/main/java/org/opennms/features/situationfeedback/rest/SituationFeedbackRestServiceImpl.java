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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.WebApplicationException;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedback.FeedbackType;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;

public class SituationFeedbackRestServiceImpl implements SituationFeedbackRestService {

    private final AlarmDao alarmDao;

    private final FeedbackRepository repository;

    public SituationFeedbackRestServiceImpl(AlarmDao alarmDao, FeedbackRepository feedbackRepository) {
        this.alarmDao = Objects.requireNonNull(alarmDao);
        this.repository = Objects.requireNonNull(feedbackRepository);
    }

    @Override
    public Collection<AlarmFeedback> getFeedback(String situationKey) {
        // TODO - filtering?? User/Fingerprint/
        try {
            return repository.getFeedback(situationKey);
        } catch (FeedbackException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public void setFeedback(List<AlarmFeedback> feedback) {
        // Update Situation in case of false_neg and false_pos
        feedback.stream().filter(f -> (f.getFeedbackType() == FeedbackType.FALSE_NEGATIVE)).forEach(c -> addCorrelation(c, alarmDao));
        feedback.stream().filter(f -> (f.getFeedbackType() == FeedbackType.FALSE_POSITVE)).forEach(c -> removeCorrelation(c, alarmDao));
        try {
            repository.persist(feedback);
        } catch (FeedbackException e) {
            throw new WebApplicationException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    protected static void removeCorrelation(AlarmFeedback feedback, AlarmDao alarmDao) {
        OnmsAlarm situation = alarmDao.findByReductionKey(feedback.getSituationKey());
        OnmsAlarm alarm = alarmDao.findByReductionKey(feedback.getAlarmKey());
        if (situation == null || alarm == null) {
            return;
        }
        situation.getRelatedAlarms().remove(alarm);
        alarmDao.saveOrUpdate(situation);
        // TODO - require any logging?

        // FIXME - must update AlarmChangeNotifier
    }

    protected static void addCorrelation(AlarmFeedback feedback, AlarmDao alarmDao) {
        OnmsAlarm situation = alarmDao.findByReductionKey(feedback.getSituationKey());
        OnmsAlarm alarm = alarmDao.findByReductionKey(feedback.getAlarmKey());
        if (situation == null || alarm == null) {
            return;
        }
        situation.getRelatedAlarms().add(alarm);
        alarmDao.saveOrUpdate(situation);
    }

}
