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

package org.opennms.features.situationfeedback.rest.model;

import org.opennms.features.situationfeedback.api.AlarmFeedback;

public class AlarmFeedbackDTO implements AlarmFeedback {

    private final String situationKey; // Situation ReductionKey

    // this may not be nesc but may prove helpful
    private final String situationThumbprint; // thumbprint of situation at
                                              // time of feedback;
    
    private final String alarmKey; // Alarm ReductionKey

    private final FeedbackType feedbackType;

    private final String reason;

    private final String user;

    private final long timestamp;

    public AlarmFeedbackDTO(String situationKey, String situationThumbprint, String alarmKey, FeedbackType feedbackType, String reason, String user,
            long timestamp) {
        this.situationKey = situationKey;
        this.situationThumbprint = situationThumbprint;
        this.alarmKey = alarmKey;
        this.feedbackType = feedbackType;
        this.reason = reason;
        this.user = user;
        this.timestamp = timestamp;
    }

    @Override
    public String getSituationKey() {
        return situationKey;
    }

    @Override
    public String getSituationThumbprint() {
        return situationThumbprint;
    }

    @Override
    public String getAlarmKey() {
        return alarmKey;
    }

    @Override
    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override 
    public String toString() {
        return "Feedback[" + getFeedbackType() + ":" + getSituationKey() + ":" + getAlarmKey() + ":" + getReason() + "]";
    }
}
