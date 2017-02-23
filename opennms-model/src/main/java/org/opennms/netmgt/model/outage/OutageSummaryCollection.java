package org.opennms.netmgt.model.outage;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name="outage-summaries")
@JsonRootName("outage-summaries")
@XmlAccessorType(XmlAccessType.NONE)
public class OutageSummaryCollection extends JaxbListWrapper<OutageSummary> {
    private static final long serialVersionUID = 1L;
    public OutageSummaryCollection() { super(); }
    public OutageSummaryCollection(final Collection<? extends OutageSummary> summaries) {
        super(summaries);
    }

    @XmlElement(name="outage-summary")
    @JsonProperty("outage-summary")
    public List<OutageSummary> getObjects() {
        return super.getObjects();
    }
}