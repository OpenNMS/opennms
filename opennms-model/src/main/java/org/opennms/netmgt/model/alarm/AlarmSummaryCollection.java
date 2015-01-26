package org.opennms.netmgt.model.alarm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="alarm-summaries")
@XmlAccessorType(XmlAccessType.NONE)
public class AlarmSummaryCollection extends ArrayList<AlarmSummary> {
    private static final long serialVersionUID = 1L;
    public AlarmSummaryCollection() {}
    public AlarmSummaryCollection(List<AlarmSummary> alarmSummaries) {
        super(alarmSummaries);
    }
    @XmlElement(name="alarm-summary")
    public List<AlarmSummary> getAlarmSummaries() {
        return this;
    }
}