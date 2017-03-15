/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notifd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Queue.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "queue")
@XmlAccessorType(XmlAccessType.FIELD)
public class Queue implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "queue-id", required = true)
    private String queueId;

    @XmlElement(name = "interval", required = true)
    private String interval;

    @XmlElement(name = "handler-class", required = true)
    private HandlerClass handlerClass;

    public Queue() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Queue) {
            Queue temp = (Queue)obj;
            boolean equals = Objects.equals(temp.queueId, queueId)
                && Objects.equals(temp.interval, interval)
                && Objects.equals(temp.handlerClass, handlerClass);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'handlerClass'.
     * 
     * @return the value of field 'HandlerClass'.
     */
    public HandlerClass getHandlerClass() {
        return this.handlerClass;
    }

    /**
     * Returns the value of field 'interval'.
     * 
     * @return the value of field 'Interval'.
     */
    public String getInterval() {
        return this.interval;
    }

    /**
     * Returns the value of field 'queueId'.
     * 
     * @return the value of field 'QueueId'.
     */
    public String getQueueId() {
        return this.queueId;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            queueId, 
            interval, 
            handlerClass);
        return hash;
    }

    /**
     * Sets the value of field 'handlerClass'.
     * 
     * @param handlerClass the value of field 'handlerClass'.
     */
    public void setHandlerClass(final HandlerClass handlerClass) {
        if (handlerClass == null) {
            throw new IllegalArgumentException("HandlerClass is a required field!");
        }
        this.handlerClass = handlerClass;
    }

    /**
     * Sets the value of field 'interval'.
     * 
     * @param interval the value of field 'interval'.
     */
    public void setInterval(final String interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval is a required field!");
        }
        this.interval = interval;
    }

    /**
     * Sets the value of field 'queueId'.
     * 
     * @param queueId the value of field 'queueId'.
     */
    public void setQueueId(final String queueId) {
        if (queueId == null) {
            throw new IllegalArgumentException("Queue ID is a required field!");
        }
        this.queueId = queueId;
    }

}
