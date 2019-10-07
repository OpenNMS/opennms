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
