/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller.outages;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * Class BasicSchedule.
 * 
 */

@XmlRootElement(name="basicSchedule", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class BasicSchedule implements java.io.Serializable {
    private static final long serialVersionUID = 4415207395024013808L;

    private static final Time[] EMPTY_TIME_LIST = new Time[0];

    /**
     * outage name
     */
    @XmlAttribute(name="name")
    private String m_name;

    /**
     * outage type
     */
    @XmlAttribute(name="type")
    private String m_type;

    /**
     * defines start/end time for the outage
     */
    @XmlElement(name="time")
    private List<Time> m_times = new ArrayList<Time>();


    public BasicSchedule() {
        super();
    }

    /**
     * 
     * 
     * @param time
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTime(final Time time) throws IndexOutOfBoundsException {
        m_times.add(time);
    }

    /**
     * 
     * 
     * @param index
     * @param time
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addTime(final int index, final Time time) throws IndexOutOfBoundsException {
        m_times.add(index, time);
    }

    /**
     * Method enumerateTime.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Time> enumerateTime() {
        return Collections.enumeration(m_times);
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: outage name
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Method getTime.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.poller.Time at the given index
     */
    public Time getTime(final int index) throws IndexOutOfBoundsException {
        return m_times.get(index);
    }

    /**
     * Method getTime.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Time[] getTime() {
        return m_times.toArray(EMPTY_TIME_LIST);
    }

    /**
     * Method getTimeCollection.Returns a reference to 'm_times'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Time> getTimeCollection() {
        return new ArrayList<Time>(m_times);
    }

    /**
     * Method getTimeCount.
     * 
     * @return the size of this collection
     */
    public int getTimeCount() {
        return m_times.size();
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the
     * following description: outage type
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return m_type;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    @Deprecated
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateTime.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Time> iterateTime() {
        return m_times.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    @Deprecated
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllTime() {
        m_times.clear();
    }

    /**
     * Method removeTime.
     * 
     * @param time
     * @return true if the object was removed from the collection.
     */
    public boolean removeTime(final Time time) {
        return m_times.remove(time);
    }

    /**
     * Method removeTimeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Time removeTimeAt(final int index) {
        return m_times.remove(index);
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: outage name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param time
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setTime(final int index, final Time time) throws IndexOutOfBoundsException {
        m_times.set(index, time);
    }

    /**
     * 
     * 
     * @param times
     */
    public void setTime(final Time[] times) {
        m_times.clear();
        for (final Time time : times) {
            m_times.add(time);
        }
    }

    /**
     * Sets the value of 'm_times' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param times the Vector to copy.
     */
    public void setTime(final List<Time> times) {
        if (times != m_times) {
            m_times.clear();
            m_times.addAll(times);
        }
    }

    /**
     * Sets the value of 'm_times' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param times the Vector to set.
     */
    public void setTimeCollection(final List<Time> times) {
        m_times = new ArrayList<Time>(times);
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the
     * following description: outage type
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        m_type = type;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.config.poller.BasicSchedule
     */
    @Deprecated
    public static BasicSchedule unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (BasicSchedule) Unmarshaller.unmarshal(BasicSchedule.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_times == null) ? 0 : m_times.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasicSchedule)) {
            return false;
        }
        final BasicSchedule other = (BasicSchedule) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_times == null) {
            if (other.m_times != null) {
                return false;
            }
        } else if (!m_times.equals(other.m_times)) {
            return false;
        }
        if (m_type == null) {
            if (other.m_type != null) {
                return false;
            }
        } else if (!m_type.equals(other.m_type)) {
            return false;
        }
        return true;
    }

}
