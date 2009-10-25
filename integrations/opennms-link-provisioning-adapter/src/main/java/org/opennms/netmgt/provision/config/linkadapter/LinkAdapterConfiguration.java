package org.opennms.netmgt.provision.config.linkadapter;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="link-adapter-configuration")
public class LinkAdapterConfiguration {
    Set<LinkPattern> m_patterns = new HashSet<LinkPattern>();

    public void addPattern(LinkPattern linkPattern) {
        m_patterns.add(linkPattern);
    }

    @XmlElement(name="for")
    public Set<LinkPattern> getPatterns() {
        return m_patterns;
    }
    
    public synchronized void setPatterns(Set<LinkPattern> patterns) {
        m_patterns.clear();
        m_patterns.addAll(patterns);
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("patterns", m_patterns)
            .toString();
    }
}
