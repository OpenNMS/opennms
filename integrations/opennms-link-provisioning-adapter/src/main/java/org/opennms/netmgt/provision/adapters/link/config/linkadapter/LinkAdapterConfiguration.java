package org.opennms.netmgt.provision.adapters.link.config.linkadapter;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <p>LinkAdapterConfiguration class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="link-adapter-configuration")
public class LinkAdapterConfiguration {
    Set<LinkPattern> m_patterns = new HashSet<LinkPattern>();

    /**
     * <p>addPattern</p>
     *
     * @param linkPattern a {@link org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern} object.
     */
    public void addPattern(LinkPattern linkPattern) {
        m_patterns.add(linkPattern);
    }

    /**
     * <p>getPatterns</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @XmlElement(name="for")
    public Set<LinkPattern> getPatterns() {
        return m_patterns;
    }
    
    /**
     * <p>setPatterns</p>
     *
     * @param patterns a {@link java.util.Set} object.
     */
    public void setPatterns(Set<LinkPattern> patterns) {
        synchronized(m_patterns) {
            m_patterns.clear();
            m_patterns.addAll(patterns);
        }
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append("patterns", m_patterns)
            .toString();
    }
}
