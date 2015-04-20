package org.opennms.web.svclayer.support;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name = "rtc-nodes")
public class RtcNodeList extends JaxbListWrapper<RtcNode> {
    private static final long serialVersionUID = 1L;

    public RtcNodeList() { super(); }
    public RtcNodeList(final Collection<? extends RtcNode> nodes) {
        super(nodes);
    }

    @XmlElement(name="rtc-node")
    public List<RtcNode> getObjects() {
        return super.getObjects();
    }
}
