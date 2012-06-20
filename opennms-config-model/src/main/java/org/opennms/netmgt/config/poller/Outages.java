/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.config.poller;

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
    private static final long serialVersionUID = 2135204624761990598L;

    /**
     * A scheduled outage
     */
    @XmlElement(name="outage")
    private List<Outage> _outageList;


    public Outages() {
        super();
        this._outageList = new ArrayList<Outage>();
    }

    /**
     * Adds a new Scheduled Outage
     * 
     * @param vOutage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutage(final Outage vOutage) throws IndexOutOfBoundsException {
        this._outageList.add(vOutage);
    }

    /**
     * Adds a new Scheduled Outage
     * 
     * @param index
     * @param vOutage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutage( final int index, final Outage vOutage) throws IndexOutOfBoundsException {
        this._outageList.add(index, vOutage);
    }

    /**
     * Method enumerateOutage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Outage> enumerateOutage() {
        return Collections.enumeration(this._outageList);
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
            if (this._outageList != null) {
                if (temp._outageList == null) return false;
                else if (!(this._outageList.equals(temp._outageList))) 
                    return false;
            }
            else if (temp._outageList != null)
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
        // check bounds for index
        if (index < 0 || index >= this._outageList.size()) {
            throw new IndexOutOfBoundsException("getOutage: Index value '" + index + "' not in range [0.." + (this._outageList.size() - 1) + "]");
        }

        return _outageList.get(index);
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
        Outage[] array = new Outage[0];
        return this._outageList.toArray(array);
    }

    /**
     * Method getOutageCollection.Returns a reference to
     * '_outageList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Outage> getOutageCollection() {
        return this._outageList;
    }

    /**
     * Method getOutageCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCount() {
        return this._outageList.size();
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_outageList != null) {
            result = 37 * result + _outageList.hashCode();
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
        } catch (ValidationException vex) {
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
        return this._outageList.iterator();
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
        this._outageList.clear();
    }

    /**
     * Method removeOutage.
     * 
     * @param vOutage
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutage(final Outage vOutage) {
        boolean removed = _outageList.remove(vOutage);
        return removed;
    }

    /**
     * Method removeOutageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Outage removeOutageAt(final int index) {
        return this._outageList.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param vOutage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setOutage(final int index, final Outage vOutage) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._outageList.size()) {
            throw new IndexOutOfBoundsException("setOutage: Index value '" + index + "' not in range [0.." + (this._outageList.size() - 1) + "]");
        }

        this._outageList.set(index, vOutage);
    }

    /**
     * 
     * 
     * @param vOutageArray
     */
    public void setOutage(final Outage[] vOutageArray) {
        //-- copy array
        _outageList.clear();

        for (int i = 0; i < vOutageArray.length; i++) {
            this._outageList.add(vOutageArray[i]);
        }
    }

    /**
     * Sets the value of '_outageList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vOutageList the Vector to copy.
     */
    public void setOutage(final List<Outage> vOutageList) {
        // copy vector
        this._outageList.clear();

        this._outageList.addAll(vOutageList);
    }

    /**
     * Sets the value of '_outageList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param outageList the Vector to set.
     */
    public void setOutageCollection(final List<Outage> outageList) {
        this._outageList = outageList;
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

}
