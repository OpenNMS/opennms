package org.opennms.netmgt.provision.persist;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;

@XmlRootElement(name="foreign-sources")
public class ForeignSourceWrapper {
    @XmlElement(name="foreign-source")
    Collection<ForeignSource> m_fs;
    
    public ForeignSourceWrapper() {
    }

    public ForeignSourceWrapper(Collection<ForeignSource> foreignSources) {
        m_fs = foreignSources;
    }
}

