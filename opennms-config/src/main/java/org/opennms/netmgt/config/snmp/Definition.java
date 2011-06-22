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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.snmp;

import java.io.IOException;
import java.io.Reader;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * Provides a mechanism for associating one or more specific
 *  IP addresses and/or IP address ranges with a set of SNMP parms
 * which
 *  will be used in place of the default values during SNMP data
 *  collection.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="definition")
@XmlAccessorType(XmlAccessType.FIELD)
public class Definition extends Configuration implements Serializable {
	private static final long serialVersionUID = 6077248084936410239L;

	private static final Range[] EMPTY_RANGE = new Range[0];
	private static final String[] EMPTY_IP_MATCH = new String[0];

	/**
     * IP address range to which this definition
     *  applies.
     */
	@XmlElement(name="range")
    private List<Range> _rangeList;

    /**
     * Specific IP address to which this definition
     *  applies.
     */
	@XmlElement(name="specific")
    private List<String> _specificList;

    /**
     * Match Octets (as in IPLIKE)
     */
	@XmlElement(name="ip-match")
    private List<String> _ipMatchList;

    public Definition() {
        super();
        this._rangeList = new ArrayList<Range>();
        this._specificList = new ArrayList<String>();
        this._ipMatchList = new ArrayList<String>();
    }

    /**
     * 
     * 
     * @param vIpMatch
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpMatch(final String vIpMatch) throws IndexOutOfBoundsException {
        this._ipMatchList.add(vIpMatch);
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpMatch(final int index, final String vIpMatch) throws IndexOutOfBoundsException {
        this._ipMatchList.add(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRange(final Range vRange) throws IndexOutOfBoundsException {
        this._rangeList.add(vRange);
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRange(final int index, final Range vRange) throws IndexOutOfBoundsException {
        this._rangeList.add(index, vRange);
    }

    /**
     * 
     * 
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(final String vSpecific) throws IndexOutOfBoundsException {
        this._specificList.add(vSpecific);
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSpecific(final int index, final String vSpecific) throws IndexOutOfBoundsException {
        this._specificList.add(index, vSpecific);
    }

    /**
     * Method enumerateIpMatch.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateIpMatch() {
        return Collections.enumeration(this._ipMatchList);
    }

    /**
     * Method enumerateRange.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Range> enumerateRange() {
        return Collections.enumeration(this._rangeList);
    }

    /**
     * Method enumerateSpecific.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateSpecific() {
        return Collections.enumeration(this._specificList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
		if (obj instanceof Configuration == false) return false;
		if (this == obj) return true;

		final Definition temp = (Definition)obj;
		
		return new EqualsBuilder()
			.appendSuper(super.equals(obj))
			.append(getRangeCollection(), temp.getRangeCollection())
			.append(getSpecificCollection(), temp.getSpecificCollection())
			.append(getIpMatchCollection(), temp.getIpMatchCollection())
			.isEquals();
    }

    /**
     * Method getIpMatch.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIpMatch(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("getIpMatch: Index value '" + index + "' not in range [0.." + (this._ipMatchList.size() - 1) + "]");
        }
        
        return _ipMatchList.get(index);
    }

    /**
     * Method getIpMatch.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getIpMatch() {
        return this._ipMatchList.toArray(EMPTY_IP_MATCH);
    }

    /**
     * Method getIpMatchCollection.Returns a reference to
     * '_ipMatchList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIpMatchCollection() {
        return this._ipMatchList;
    }

    /**
     * Method getIpMatchCount.
     * 
     * @return the size of this collection
     */
    public int getIpMatchCount() {
        return this._ipMatchList.size();
    }

    /**
     * Method getRange.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Range at the given index
     */
    public Range getRange(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("getRange: Index value '" + index + "' not in range [0.." + (this._rangeList.size() - 1) + "]");
        }
        
        return _rangeList.get(index);
    }

    /**
     * Method getRange.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Range[] getRange() {
        return this._rangeList.toArray(EMPTY_RANGE);
    }

    /**
     * Method getRangeCollection.Returns a reference to
     * '_rangeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Range> getRangeCollection() {
        return this._rangeList;
    }

    /**
     * Method getRangeCount.
     * 
     * @return the size of this collection
     */
    public int getRangeCount() {
        return this._rangeList.size();
    }

    /**
     * Method getSpecific.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getSpecific(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("getSpecific: Index value '" + index + "' not in range [0.." + (this._specificList.size() - 1) + "]");
        }
        
        return _specificList.get(index);
    }

    /**
     * Method getSpecific.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public String[] getSpecific() {
        return this._specificList.toArray(EMPTY_IP_MATCH);
    }

    /**
     * Method getSpecificCollection.Returns a reference to
     * '_specificList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getSpecificCollection() {
        return this._specificList;
    }

    /**
     * Method getSpecificCount.
     * 
     * @return the size of this collection
     */
    public int getSpecificCount() {
        return this._specificList.size();
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode(
    ) {
        int result = 17;
        
        if (_rangeList != null) {
           result = 37 * result + _rangeList.hashCode();
        }
        if (_specificList != null) {
           result = 37 * result + _specificList.hashCode();
        }
        if (_ipMatchList != null) {
           result = 37 * result + _ipMatchList.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (final ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateIpMatch.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateIpMatch() {
        return this._ipMatchList.iterator();
    }

    /**
     * Method iterateRange.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Range> iterateRange() {
        return this._rangeList.iterator();
    }

    /**
     * Method iterateSpecific.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateSpecific() {
        return this._specificList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllIpMatch() {
        this._ipMatchList.clear();
    }

    public void removeAllRange() {
        this._rangeList.clear();
    }

    public void removeAllSpecific() {
        this._specificList.clear();
    }

    /**
     * Method removeIpMatch.
     * 
     * @param vIpMatch
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpMatch(final String vIpMatch) {
        return _ipMatchList.remove(vIpMatch);
    }

    /**
     * Method removeIpMatchAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIpMatchAt(final int index) {
        return this._ipMatchList.remove(index);
    }

    /**
     * Method removeRange.
     * 
     * @param vRange
     * @return true if the object was removed from the collection.
     */
    public boolean removeRange(final Range vRange) {
        return _rangeList.remove(vRange);
    }

    /**
     * Method removeRangeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Range removeRangeAt(final int index) {
        return this._rangeList.remove(index);
    }

    /**
     * Method removeSpecific.
     * 
     * @param vSpecific
     * @return true if the object was removed from the collection.
     */
    public boolean removeSpecific(final String vSpecific) {
        return _specificList.remove(vSpecific);
    }

    /**
     * Method removeSpecificAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeSpecificAt(final int index) {
        return this._specificList.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param vIpMatch
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIpMatch(final int index, final String vIpMatch) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._ipMatchList.size()) {
            throw new IndexOutOfBoundsException("setIpMatch: Index value '" + index + "' not in range [0.." + (this._ipMatchList.size() - 1) + "]");
        }
        
        this._ipMatchList.set(index, vIpMatch);
    }

    /**
     * 
     * 
     * @param vIpMatchArray
     */
    public void setIpMatch(final String[] vIpMatchArray) {
        //-- copy array
        _ipMatchList.clear();
        
        for (int i = 0; i < vIpMatchArray.length; i++) {
                this._ipMatchList.add(vIpMatchArray[i]);
        }
    }

    /**
     * Sets the value of '_ipMatchList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vIpMatchList the Vector to copy.
     */
    public void setIpMatch(final List<String> vIpMatchList) {
        // copy vector
        this._ipMatchList.clear();
        
        this._ipMatchList.addAll(vIpMatchList);
    }

    /**
     * Sets the value of '_ipMatchList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ipMatchList the Vector to set.
     */
    public void setIpMatchCollection(final List<String> ipMatchList) {
        this._ipMatchList = ipMatchList;
    }

    /**
     * 
     * 
     * @param index
     * @param vRange
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRange(final int index, final Range vRange) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._rangeList.size()) {
            throw new IndexOutOfBoundsException("setRange: Index value '" + index + "' not in range [0.." + (this._rangeList.size() - 1) + "]");
        }
        
        this._rangeList.set(index, vRange);
    }

    /**
     * 
     * 
     * @param vRangeArray
     */
    public void setRange(final Range[] vRangeArray) {
        //-- copy array
        _rangeList.clear();
        
        for (int i = 0; i < vRangeArray.length; i++) {
                this._rangeList.add(vRangeArray[i]);
        }
    }

    /**
     * Sets the value of '_rangeList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vRangeList the Vector to copy.
     */
    public void setRange(final List<Range> vRangeList) {
        // copy vector
        this._rangeList.clear();
        
        this._rangeList.addAll(vRangeList);
    }

    /**
     * Sets the value of '_rangeList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param rangeList the Vector to set.
     */
    public void setRangeCollection(final List<Range> rangeList) {
        this._rangeList = rangeList;
    }

    /**
     * 
     * 
     * @param index
     * @param vSpecific
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSpecific(final int index, final String vSpecific) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._specificList.size()) {
            throw new IndexOutOfBoundsException("setSpecific: Index value '" + index + "' not in range [0.." + (this._specificList.size() - 1) + "]");
        }
        
        this._specificList.set(index, vSpecific);
    }

    /**
     * 
     * 
     * @param vSpecificArray
     */
    public void setSpecific(final String[] vSpecificArray) {
        //-- copy array
        _specificList.clear();
        
        for (int i = 0; i < vSpecificArray.length; i++) {
                this._specificList.add(vSpecificArray[i]);
        }
    }

    /**
     * Sets the value of '_specificList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vSpecificList the Vector to copy.
     */
    public void setSpecific(final List<String> vSpecificList) {
        // copy vector
        this._specificList.clear();
        
        this._specificList.addAll(vSpecificList);
    }

    /**
     * Sets the value of '_specificList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param specificList the Vector to set.
     */
    public void setSpecificCollection(final List<String> specificList) {
        this._specificList = specificList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * Configuration
     */
    public static Configuration unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Configuration) Unmarshaller.unmarshal(Definition.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.appendSuper(super.toString())
    		.append("ranges", getRangeCollection())
    		.append("specifics", getSpecificCollection())
    		.append("ip-matches", getIpMatchCollection())
    		.toString();
    }
}
