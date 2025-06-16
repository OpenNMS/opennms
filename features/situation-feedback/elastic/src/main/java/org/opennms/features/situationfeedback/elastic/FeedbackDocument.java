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
