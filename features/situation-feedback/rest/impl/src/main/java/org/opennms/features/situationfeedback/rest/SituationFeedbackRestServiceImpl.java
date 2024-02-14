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
