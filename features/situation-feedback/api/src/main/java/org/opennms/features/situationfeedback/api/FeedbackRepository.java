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
package org.opennms.features.situationfeedback.api;

import java.util.Collection;
import java.util.List;

/**
 * This interface provides an abstraction over storing/retrieving {@link AlarmFeedback feedback} to some persistent
 * storage provider.
 */
public interface FeedbackRepository {

    /**
     * Persists the given collection of {@link AlarmFeedback feedback} and notifies any
     * {@link AlarmFeedbackListener listeners}.
     *
     * @param feedback the feedback to persist
     * @throws FeedbackException if the feedback could not be persisted
     */
    void persist(List<AlarmFeedback> feedback) throws FeedbackException;

    /**
     * @param situationKey the reduction key of the situation to get feedback for
     * @return all of the feedback applicable to the given situation
     */
    Collection<AlarmFeedback> getFeedback(String situationKey) throws FeedbackException;

    /**
     * @return all of the feedback present in the repository
     */
    List<AlarmFeedback> getAllFeedback() throws FeedbackException;

    /**
     * @return a list of unique SituationFeedback Tags filtered to include only those beginning with @prefix
     */
    List<String> getTags(String prefix) throws FeedbackException;

}
