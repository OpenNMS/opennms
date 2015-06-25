package org.opennms.web.rest;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.model.PrefabGraph;

@XmlRootElement(name = "prefab-graphs")
public final class PrefabGraphCollection extends JaxbListWrapper<PrefabGraph> {
    private static final long serialVersionUID = 1L;
    public PrefabGraphCollection() {
        super();
    }
    public PrefabGraphCollection(Collection<? extends PrefabGraph> graphs) {
        super(graphs);
    }
    @XmlElement(name = "prefab-graph")
    public List<PrefabGraph> getObjects() {
        return super.getObjects();
    }
}