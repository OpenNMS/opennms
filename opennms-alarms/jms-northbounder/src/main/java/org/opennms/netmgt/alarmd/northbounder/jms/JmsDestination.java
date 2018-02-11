/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd.northbounder.jms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.alarmd.api.Destination;

/**
 * The Class JmsDestination.
 */
@XmlRootElement(name = "jms-destination")
@XmlAccessorType(XmlAccessType.FIELD)
public class JmsDestination implements Destination {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Enumeration DestinationType.
     */
    @XmlType(name = "destination-type")
    @XmlEnum
    public enum DestinationType {

        /** The queue. */
        QUEUE, 
        /** The topic. */
        TOPIC;

        /**
         * Value.
         *
         * @return the string
         */
        public String value() {
            return name();
        }

        /**
         * From value.
         *
         * @param v the type as string
         * @return the destination type
         */
        public static DestinationType fromValue(String v) {
            return valueOf(v);
        }
    }

    /** The destination name. */
    @XmlTransient
    private String m_destinationName;

    /** The first occurrence only. */
    @XmlElement(name = "first-occurence-only", required = false, defaultValue = "false")
    private Boolean m_firstOccurrenceOnly;

    /** The send as object message enabled. */
    @XmlElement(name = "send-as-object-message", required = false, defaultValue = "false")
    private Boolean m_sendAsObjectMessageEnabled;

    /** The destination type. */
    @XmlElement(name = "destination-type", required = true, defaultValue = "QUEUE")
    private DestinationType m_destinationType;

    /** The destination. */
    @XmlElement(name = "jms-destination", required = true)
    private String m_destination;

    /** The message format. */
    @XmlElement(name = "message-format", required = false)
    private String m_messageFormat;

    /**
     * Instantiates a new JMS destination.
     */
    public JmsDestination() {
        super();
    }

    /**
     * Instantiates a new JMS destination.
     *
     * @param destinationType the destination type
     * @param destination the destination
     */
    public JmsDestination(DestinationType destinationType, String destination) {
        super();
        m_destinationType = destinationType;
        m_destination = destination;
        final StringBuilder sb = new StringBuilder();
        sb.append(m_destinationType.toString()).append(":").append(m_destination);
        m_destinationName = sb.toString();
    }

    /**
     * Instantiates a new JMS destination.
     *
     * @param destinationType the destination type
     * @param destination the destination
     * @param firstOccurrenceOnly the first occurrence only
     * @param sendAsObjectMessage the send as object message
     */
    public JmsDestination(DestinationType destinationType, String destination, boolean firstOccurrenceOnly, boolean sendAsObjectMessage) {
        this(destinationType, destination);
        m_firstOccurrenceOnly = firstOccurrenceOnly;
        m_sendAsObjectMessageEnabled = sendAsObjectMessage;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#getName()
     */
    public String getName() {
        return m_destinationName;
    }

    /**
     * Sets the name.
     *
     * @param destinationName the new name
     */
    public void setName(String destinationName) {
        m_destinationName = destinationName;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#isFirstOccurrenceOnly()
     */
    @Override
    public boolean isFirstOccurrenceOnly() {
        return m_firstOccurrenceOnly == null ? false : m_firstOccurrenceOnly;
    }

    /**
     * Sets the first occurrence only.
     *
     * @param firstOccurrenceOnly the new first occurrence only
     */
    public void setFirstOccurrenceOnly(Boolean firstOccurrenceOnly) {
        m_firstOccurrenceOnly = firstOccurrenceOnly;
    }

    /**
     * Gets the JMS destination.
     *
     * @return the JMS destination
     */
    public String getJmsDestination() {
        return m_destination;
    }

    /**
     * Checks if is send as object message enabled.
     *
     * @return true, if is send as object message enabled
     */
    public boolean isSendAsObjectMessageEnabled() {
        return m_sendAsObjectMessageEnabled == null ? false : m_sendAsObjectMessageEnabled;
    }

    /**
     * Gets the destination type.
     *
     * @return the destination type
     */
    public DestinationType getDestinationType() {
        return m_destinationType == null ? DestinationType.QUEUE : m_destinationType;
    }

    /**
     * Sets the destination type.
     *
     * @param destinationType the new destination type
     */
    public void setDestinationType(DestinationType destinationType) {
        m_destinationType = destinationType;
    }

    /**
     * Gets the message format.
     *
     * @return the message format
     */
    public String getMessageFormat() {
        return m_messageFormat;
    }

}
