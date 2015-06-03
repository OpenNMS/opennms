package org.opennms.web.rest;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name = "names")
public final class GraphNameCollection extends JaxbListWrapper<String> {

    private static final long serialVersionUID = 1L;

    public GraphNameCollection() {
        super();
    }

    public GraphNameCollection(Collection<? extends String> names) {
        super(names);
    }

    @XmlElement(name = "name")
    public List<String> getObjects() {
        return super.getObjects();
    }
}