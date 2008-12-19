package org.opennms.netmgt.provision.persist;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="foreign-sources")
public class ForeignSourceWrapper {
    @XmlElement(name="foreign-source")
    Collection<OnmsForeignSource> m_fs;
    
    public ForeignSourceWrapper() {
    }

    public ForeignSourceWrapper(Collection<OnmsForeignSource> foreignSources) {
        m_fs = foreignSources;
    }
}

