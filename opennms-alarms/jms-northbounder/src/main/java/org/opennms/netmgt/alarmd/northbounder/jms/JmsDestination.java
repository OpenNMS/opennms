package org.opennms.netmgt.alarmd.northbounder.jms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.alarmd.api.Destination;

@XmlRootElement(name="jms-destination")
@XmlAccessorType(XmlAccessType.FIELD)
public class JmsDestination implements Destination{
    private static final long serialVersionUID = 1L;

    @XmlType(name="destination-type")
    @XmlEnum
    public enum DestinationType {
        QUEUE, TOPIC;
        public String value() {
            return name();
        }
        public static DestinationType fromValue(String v) {
            return valueOf(v);
        }
    }

    @XmlTransient
    private String m_destinationName;
    
    @XmlElement(name = "first-occurence-only", required = false, defaultValue = "false")
    private boolean m_firstOccurrenceOnly = false;
    
    @XmlElement(name = "send-as-object-message", required = false, defaultValue = "false")
    private boolean m_sendAsObjectMessageEnabled = false;
    
    @XmlElement(name = "destination-type", required = true, defaultValue = "QUEUE")
    private DestinationType m_destinationType = DestinationType.QUEUE;
    
    @XmlElement(name = "jms-destination", required = true)
    private String m_destination;

    @XmlElement(name = "message-format", required = false)
    private String m_messageFormat;

    public JmsDestination() {
        super();
    }
    
    public JmsDestination(DestinationType destinationType, String destination) {
        super();
        m_destinationType = destinationType;
        m_destination = destination;
        StringBuilder sb = new StringBuilder();
        sb.append(m_destinationType.toString()).append(":").append(m_destination);
        m_destinationName = sb.toString();
    }

    
    public JmsDestination(DestinationType destinationType,
            String destination, boolean firstOccurrenceOnly,
            boolean sendAsObjectMessage) {
        this(destinationType, destination);
        m_firstOccurrenceOnly = firstOccurrenceOnly;
        m_sendAsObjectMessageEnabled = sendAsObjectMessage;
    }

    public String getName() {
        return m_destinationName;
    }

    public void setName(String destinationName) {
        m_destinationName = destinationName;
    }

    @Override
    public boolean isFirstOccurrenceOnly() {
        return m_firstOccurrenceOnly;
    }

    public void setFirstOccurrenceOnly(boolean firstOccurrenceOnly) {
        m_firstOccurrenceOnly = firstOccurrenceOnly;
    }

    public String getJmsDestination() {
        return m_destination;
    }

    public boolean isSendAsObjectMessageEnabled() {
        return m_sendAsObjectMessageEnabled;
    }
    
    public DestinationType getDestinationType() {
        return m_destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        m_destinationType = destinationType;
    }

    public String getMessageFormat() {
        return m_messageFormat;
    }
    
}
