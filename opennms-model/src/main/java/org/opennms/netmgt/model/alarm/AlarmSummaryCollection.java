package org.opennms.netmgt.model.alarm;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name="alarm-summaries")
@JsonRootName("alarm-summaries")
@XmlAccessorType(XmlAccessType.NONE)
public class AlarmSummaryCollection extends JaxbListWrapper<AlarmSummary> {
    private static final long serialVersionUID = 1L;
    public AlarmSummaryCollection() {}
    public AlarmSummaryCollection(Collection<? extends AlarmSummary> alarmSummaries) {
        super(alarmSummaries);
    }
    @XmlElement(name="alarm-summary")
    @JsonProperty("alarm-summary")
    public List<AlarmSummary> getObjects() {
        return super.getObjects();
    }
}