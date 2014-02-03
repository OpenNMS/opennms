/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 * Top-level element for the poll-outages.xml configuration file.
 * 
 */

@XmlRootElement(name="outages", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class Outages implements Serializable {
    private static final Outage[] EMPTY_OUTAGE_LIST = new Outage[0];

    private static final long serialVersionUID = 2135204624761990598L;

    /**
     * A scheduled outage
     */
    @XmlElement(name="outage")
    private List<Outage> m_outages = new ArrayList<Outage>();


    public Outages() {
        super();
    }

    /**
     * Adds a new Scheduled Outage
     * 
     * @param outage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutage(final Outage outage) throws IndexOutOfBoundsException {
        m_outages.add(outage);
    }

    /**
     * Adds a new Scheduled Outage
     * 
     * @param index
     * @param outage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutage( final int index, final Outage outage) throws IndexOutOfBoundsException {
        m_outages.add(index, outage);
    }

    /**
     * Method enumerateOutage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Outage> enumerateOutage() {
        return Collections.enumeration(m_outages);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof Outages) {

            Outages temp = (Outages)obj;
            if (m_outages != null) {
                if (temp.m_outages == null) return false;
                else if (!(m_outages.equals(temp.m_outages))) 
                    return false;
            }
            else if (temp.m_outages != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getOutage.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.poller.Outage at the given index
     */
    public Outage getOutage(final int index) throws IndexOutOfBoundsException {
        return m_outages.get(index);
    }

    /**
     * Method getOutage.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Outage[] getOutage() {
        return m_outages.toArray(EMPTY_OUTAGE_LIST);
    }

    /**
     * Method getOutageCollection.Returns a reference to
     * 'm_outages'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Outage> getOutageCollection() {
        return new ArrayList<Outage>(m_outages);
    }

    /**
     * Method getOutageCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCount() {
        return m_outages.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int result = 17;

        if (m_outages != null) {
            result = 37 * result + m_outages.hashCode();
        }

        return result;
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
     * Method iterateOutage.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Outage> iterateOutage() {
        return m_outages.iterator();
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
    public void removeAllOutage() {
        m_outages.clear();
    }

    /**
     * Method removeOutage.
     * 
     * @param outage
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutage(final Outage outage) {
        return m_outages.remove(outage);
    }

    /**
     * Method removeOutageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Outage removeOutageAt(final int index) {
        return m_outages.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param outage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOutage(final int index, final Outage outage) throws IndexOutOfBoundsException {
        m_outages.set(index, outage);
    }

    /**
     * 
     * 
     * @param outages
     */
    public void setOutage(final Outage[] outages) {
        m_outages.clear();
        for (final Outage outage : outages) {
            m_outages.add(outage);
        }
    }

    /**
     * Sets the value of 'm_outages' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param outages the Vector to copy.
     */
    public void setOutage(final List<Outage> outages) {
        if (outages != m_outages) {
            m_outages.clear();
            m_outages.addAll(outages);
        }
    }

    /**
     * Sets the value of 'm_outages' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param outages the Vector to set.
     */
    public void setOutageCollection(final List<Outage> outages) {
        m_outages = new ArrayList<Outage>(outages);
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
     * org.opennms.netmgt.config.poller.Outages
     */
    @Deprecated
    public static Outages unmarshal(final java.io.Reader reader) throws MarshalException, ValidationException {
        return (Outages) Unmarshaller.unmarshal(Outages.class, reader);
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
    public String toString() {
        return "Outages[outages=" + m_outages + "]";
    }
}
