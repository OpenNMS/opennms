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
package org.opennms.features.situationfeedback.elastic;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedback.FeedbackType;

import com.google.gson.annotations.SerializedName;

public class FeedbackDocument {

    private static final int DOCUMENT_VERSION = 1;

    /**
     * Flow timestamp in milliseconds.
     */
    @SerializedName("@timestamp")
    private long timestamp;

    /**
     * Schema version.
     */
    @SerializedName("@version")
    private final Integer version = DOCUMENT_VERSION;

    @SerializedName("alarm_key")
    private String alarmKey;

    @SerializedName("feedback_type")
    private String feedbackType;

    @SerializedName("situation_fingerprint")
    private String situationFingerprint;

    @SerializedName("situation_key")
    private String situationKey;

    @SerializedName("reason")
    private String reason;

    @SerializedName("root_cause")
    private Boolean isRootCause;

    @SerializedName("tags")
    private List<String> tags = new ArrayList<>();

    @SerializedName("user")
    private String user;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getVersion() {
        return version;
    }

    public String getAlarmKey() {
        return alarmKey;
    }

    public void setAlarmKey(String alarmKey) {
        this.alarmKey = alarmKey;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getSituationFingerprint() {
        return situationFingerprint;
    }

    public void setSituationFingerprint(String situationFingerprint) {
        this.situationFingerprint = situationFingerprint;
    }

    public String getSituationKey() {
        return situationKey;
    }

    public void setSituationKey(String situationKey) {
        this.situationKey = situationKey;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getIsRootCause() {
        return isRootCause;
    }

    public void setIsRootCause(Boolean isRootCause) {
        this.isRootCause = isRootCause;
    }

    public List<String> getTags() {
        return tags;
    }

    private void setTags(List<String> tags) {
        this.tags.addAll(tags);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static FeedbackDocument from(AlarmFeedback feedback) {
        FeedbackDocument doc = new FeedbackDocument();
        doc.setTimestamp(feedback.getTimestamp());
        doc.setAlarmKey(feedback.getAlarmKey());
        doc.setFeedbackType(feedback.getFeedbackType().toString());
        doc.setReason(feedback.getReason());
        doc.setSituationFingerprint(feedback.getSituationFingerprint());
        doc.setSituationKey(feedback.getSituationKey());
        doc.setIsRootCause(feedback.getRootCause());
        doc.setTags(feedback.getTags());
        doc.setUser(feedback.getUser());
        return doc;
    }

    public static AlarmFeedback toAlarmFeedback(FeedbackDocument doc) {
        return AlarmFeedback.newBuilder()
                .withSituationKey(doc.situationKey)
                .withSituationFingerprint(doc.situationFingerprint)
                .withAlarmKey(doc.alarmKey)
                .withFeedbackType(FeedbackType.valueOfOrUnknown(doc.feedbackType))
                .withReason(doc.reason)
                .withRootCause(doc.isRootCause)
                .withUser(doc.user)
                .withTags(doc.tags)
                .withTimestamp(doc.timestamp)
                .build();
    }
}
