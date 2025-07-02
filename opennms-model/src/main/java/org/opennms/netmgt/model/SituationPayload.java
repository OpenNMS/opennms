package org.opennms.netmgt.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class SituationPayload {

    private final List<Integer> alarmIdList;
    private final String diagnosticText;
    private final String description;
    private final String feedback;

    @JsonCreator
    public SituationPayload(
            @JsonProperty("alarmIdList") List<Integer> alarmIdList,
            @JsonProperty("diagnosticText") String diagnosticText,
            @JsonProperty("description") String description,
            @JsonProperty("feedback") String feedback) {
        this.alarmIdList = alarmIdList;
        this.diagnosticText = diagnosticText;
        this.description = description;
        this.feedback = feedback;
    }

    public List<Integer> getAlarmIdList() {
        return alarmIdList;
    }

    public String getDiagnosticText() {
        return diagnosticText;
    }

    public String getDescription() {
        return description;
    }

    public String getFeedback() {
        return feedback;
    }
}
