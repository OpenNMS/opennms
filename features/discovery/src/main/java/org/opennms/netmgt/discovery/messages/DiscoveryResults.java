package org.opennms.netmgt.discovery.messages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;

import com.google.common.base.Preconditions;

public class DiscoveryResults implements Serializable {
    private static final long serialVersionUID = -3416001085353754747L;

    private final Map<InetAddress, Long> m_responses;
    private final String m_foreignSource;
    private final String m_location;

    public DiscoveryResults(Map<InetAddress, Long> responses, String foreignSource, String location) {
        m_responses = Preconditions.checkNotNull(responses, "ranges argument");
        m_foreignSource = Preconditions.checkNotNull(foreignSource, "foreignSource argument");
        m_location = Preconditions.checkNotNull(location, "location argument");
    }

    public Map<InetAddress, Long> getResponses() {
        return m_responses;
    }

    public String getForeignSource() {
        return m_foreignSource;
    }

    public String getLocation() {
        return m_location;
    }

    @Override  
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final DiscoveryResults other = (DiscoveryResults) obj;

       return com.google.common.base.Objects.equal(this.m_responses, other.m_responses)  &&
               com.google.common.base.Objects.equal(this.m_foreignSource, other.m_foreignSource) &&
               com.google.common.base.Objects.equal(this.m_location, other.m_location);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(m_responses,
               m_foreignSource,
               m_location);
    }

    @Override
    public String toString() {
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("responses", m_responses)
                 .add("foreignSource", m_foreignSource)
                 .add("location", m_location)
                 .toString();
    }
}

