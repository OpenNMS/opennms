package org.opennms.netmgt.model.outage;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="outage-summaries")
@XmlAccessorType(XmlAccessType.NONE)
public class OutageSummaryCollection extends ArrayList<OutageSummary> {
    private static final long serialVersionUID = 1L;
    public OutageSummaryCollection() {}
    public OutageSummaryCollection(List<OutageSummary> outageSummaries) {
        super(outageSummaries);
    }
    @XmlElement(name="outage-summary")
    public List<OutageSummary> getOutageSummaries() {
        return this;
    }
}