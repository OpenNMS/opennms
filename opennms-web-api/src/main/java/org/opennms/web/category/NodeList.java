package org.opennms.web.category;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name="nodes")
public class NodeList extends JaxbListWrapper<AvailabilityNode> {
    private static final long serialVersionUID = 1L;

    public NodeList() { super(); }
    public NodeList(final Collection<? extends AvailabilityNode> nodes) {
        super(nodes);
    }

    public static NodeList forNodes(final Collection<? extends org.opennms.netmgt.xml.rtc.Node> nodes) {
        final NodeList nl = new NodeList();
        for (final org.opennms.netmgt.xml.rtc.Node n : nodes) {
            nl.add(new AvailabilityNode(n));
        }
        return nl;
    }

    @XmlElement(name="node")
    public List<AvailabilityNode> getObjects() {
        return super.getObjects();
    }
}
