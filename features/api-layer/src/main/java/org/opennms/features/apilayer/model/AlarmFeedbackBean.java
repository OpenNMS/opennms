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

package org.opennms.features.apilayer.model;

import java.util.Objects;

import org.opennms.integration.api.v1.model.AlarmFeedback;

import com.google.common.base.Enums;

public class AlarmFeedbackBean implements AlarmFeedback {

    private final String situationKey;
    private final String situationFingerprint;
    private final String alarmKey;
    private final Type feedbackType;
    private final String reason;
    private final String user;
    private final long timestamp;

    public AlarmFeedbackBean(org.opennms.features.situationfeedback.api.AlarmFeedback feedback) {
        situationKey = Objects.requireNonNull(feedback.getSituationKey());
        situationFingerprint = feedback.getSituationFingerprint();
        alarmKey = Objects.requireNonNull(feedback.getAlarmKey());
        feedbackType = Objects.requireNonNull(Enums.getIfPresent(AlarmFeedback.Type.class,
                feedback.getFeedbackType().toString()).get());
        reason = feedback.getReason();
        user = feedback.getUser();
        timestamp = feedback.getTimestamp();
    }

    @Override
    public String getSituationKey() {
        return situationKey;
    }

    @Override
    public String getSituationFingerprint() {
        return situationFingerprint;
    }

    @Override
    public String getAlarmKey() {
        return alarmKey;
    }

    @Override
    public Type getFeedbackType() {
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
    public Long getTimestamp() {
        return timestamp;
    }
}
