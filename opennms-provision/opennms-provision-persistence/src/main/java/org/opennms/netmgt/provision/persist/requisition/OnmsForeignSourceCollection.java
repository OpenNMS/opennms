package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;

@XmlRootElement(name="foreign-sources")
public class OnmsForeignSourceCollection extends LinkedList<ForeignSource> {

	private static final long serialVersionUID = 1L;

	public OnmsForeignSourceCollection() {
        super();
    }

    public OnmsForeignSourceCollection(Collection<? extends ForeignSource> c) {
        super(c);
    }

    @XmlElement(name="foreign-source")
    public List<ForeignSource> getForeignSources() {
        return this;
    }

    public void setForeignSources(List<ForeignSource> foreignSources) {
        clear();
        addAll(foreignSources);
    }
}

