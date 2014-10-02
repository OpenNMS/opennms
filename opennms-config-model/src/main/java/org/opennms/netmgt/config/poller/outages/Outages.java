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
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("poll-outages.xsd")
public class Outages implements Serializable {
    private static final Outage[] EMPTY_OUTAGE_LIST = new Outage[0];

    private static final long serialVersionUID = 2135204624761990598L;

    /**
     * A scheduled outage
     */
    private Map<String, Outage> _outageMap;
    
    public Outages() {
        super();
        this._outageMap = new LinkedHashMap<String, Outage>();
    }

    /**
     * Adds a new Scheduled Outage
     * 
     * @param vOutage
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addOutage(final Outage vOutage) throws IndexOutOfBoundsException {
        this._outageMap.put(vOutage.getName(), vOutage);
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
        List<Outage> outageList = new ArrayList<Outage>(this._outageMap.values());
        outageList.add(index, vOutage);
        setOutage(outageList);
        
    }

    /**
     * Method enumerateOutage.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Outage> enumerateOutage() {
        return Collections.enumeration(this._outageMap.values());
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
            if (this._outageMap != null) {
                if (temp._outageMap == null) return false;
                else if (!(this._outageMap.equals(temp._outageMap))) 
                    return false;
            }
            else if (temp._outageMap != null)
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
        
        if (index < 0 || index >= this._outageMap.size()) {
            throw new IndexOutOfBoundsException("getOutage: Index value '" + index + "' not in range [0.." + (this._outageMap.size() - 1) + "]");
        }
        
        int count = 0;
        for(Outage o : this._outageMap.values()) {
            if (count == index) {
                return o;
            }
            count++;
        }
        
        return null;
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
    @XmlElement(name="outage")
    public Outage[] getOutage() {
        return this._outageMap.values().toArray(EMPTY_OUTAGE_LIST);
    }

    /**
     * Method getOutageCollection.Returns a reference to
     * '_outageList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Outage> getOutageCollection() {
        return new ArrayList<Outage>(this._outageMap.values());
    }

    /**
     * Method getOutageCount.
     * 
     * @return the size of this collection
     */
    public int getOutageCount() {
        return this._outageMap.size();
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

        if (_outageMap != null) {
            result = 37 * result + _outageMap.hashCode();
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
        return this._outageMap.values().iterator();
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
        this._outageMap.clear();
    }

    /**
     * Method removeOutage.
     * 
     * @param vOutage
     * @return true if the object was removed from the collection.
     */
    public boolean removeOutage(final Outage vOutage) {
        Outage removed = _outageMap.remove(vOutage.getName());
        return removed != null;
    }

    /**
     * Method removeOutageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Outage removeOutageAt(final int index) {
        List<Outage> outageList = new ArrayList<Outage>(this._outageMap.values());
        Outage o = outageList.remove(index);
        setOutage(outageList);
        return o;
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
        if (index < 0 || index >= this._outageMap.size()) {
            throw new IndexOutOfBoundsException("setOutage: Index value '" + index + "' not in range [0.." + (this._outageMap.size() - 1) + "]");
        }

        List<Outage> outageList = new ArrayList<Outage>(this._outageMap.values());
        outageList.set(index, vOutage);
        setOutage(outageList);
    }

    /**
     * 
     * 
     * @param vOutageArray
     */
    public void setOutage(final Outage[] vOutageArray) {
        setOutage(Arrays.asList(vOutageArray));
    }

    /**
     * Sets the value of '_outageList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vOutageList the Vector to copy.
     */
    public void setOutage(final List<Outage> vOutageList) {
        Map<String, Outage> m = new LinkedHashMap<String, Outage>();
        for(Outage o : vOutageList) {
            m.put(o.getName(), o);
        }
        this._outageMap = m;
    }

    /**
     * Sets the value of '_outageList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param outageList the Vector to set.
     */
    public void setOutageCollection(final List<Outage> outageList) {
        setOutage(outageList);
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

    public Outage getOutage(String name) {
        return this._outageMap.get(name);
    }

    public void removeOutage(String outageName) {
        this._outageMap.remove(outageName);
    }

}
