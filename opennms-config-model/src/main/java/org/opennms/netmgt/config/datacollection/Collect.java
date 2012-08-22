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

package org.opennms.netmgt.config.datacollection;

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
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * container for list of MIB groups to be collected for the system
 */

@XmlRootElement(name="collect", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"includeGroup"})
@ValidateUsing("datacollection-config.xsd")
public class Collect implements Serializable {
    private static final long serialVersionUID = -7340423679797254573L;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    /**
     * Field _includeGroupList.
     */
    private List<String> m_includeGroups = new ArrayList<String>();

    /**
     * 
     * 
     * @param includeGroup
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeGroup(final String includeGroup) throws IndexOutOfBoundsException {
        m_includeGroups.add(includeGroup);
    }

    /**
     * 
     * 
     * @param index
     * @param vIncludeGroup
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeGroup(final int index, final String vIncludeGroup) throws IndexOutOfBoundsException {
        m_includeGroups.add(index, vIncludeGroup);
    }

    /**
     * Method enumerateIncludeGroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateIncludeGroup() {
        return Collections.enumeration(m_includeGroups);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Collect) {
        
            final Collect temp = (Collect)obj;
            if (m_includeGroups != null) {
                if (temp.m_includeGroups == null) return false;
                else if (!(m_includeGroups.equals(temp.m_includeGroups))) 
                    return false;
            }
            else if (temp.m_includeGroups != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getIncludeGroup.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIncludeGroup(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_includeGroups.size()) {
            throw new IndexOutOfBoundsException("getIncludeGroup: Index value '" + index + "' not in range [0.." + (m_includeGroups.size() - 1) + "]");
        }
        return m_includeGroups.get(index);
    }

    /**
     * Method getIncludeGroup.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="includeGroup")
    public String[] getIncludeGroup() {
        return m_includeGroups.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Method getIncludeGroupCollection.Returns a reference to
     * '_includeGroupList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIncludeGroupCollection() {
        return m_includeGroups;
    }

    /**
     * Method getIncludeGroupCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeGroupCount() {
        return m_includeGroups.size();
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;
        
        if (m_includeGroups != null) {
           result = 37 * result + m_includeGroups.hashCode();
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
     * Method iterateIncludeGroup.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateIncludeGroup() {
        return m_includeGroups.iterator();
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
    public void removeAllIncludeGroup() {
        m_includeGroups.clear();
    }

    /**
     * Method removeIncludeGroup.
     * 
     * @param includeGroup
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeGroup(final String includeGroup) {
        return m_includeGroups.remove(includeGroup);
    }

    /**
     * Method removeIncludeGroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIncludeGroupAt(final int index) {
        return m_includeGroups.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param includeGroup
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeGroup(final int index, final String includeGroup) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_includeGroups.size()) {
            throw new IndexOutOfBoundsException("setIncludeGroup: Index value '" + index + "' not in range [0.." + (m_includeGroups.size() - 1) + "]");
        }
        m_includeGroups.set(index, includeGroup.intern());
    }

    /**
     * 
     * 
     * @param includeGroups
     */
    public void setIncludeGroup(final String[] includeGroups) {
        m_includeGroups.clear();
        for (int i = 0; i < includeGroups.length; i++) {
                m_includeGroups.add(includeGroups[i].intern());
        }
    }

    /**
     * Sets the value of '_includeGroupList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param includeGroups the Vector to copy.
     */
    public void setIncludeGroup(final List<String> includeGroups) {
        m_includeGroups.clear();
        for (final String includeGroup : includeGroups) {
            m_includeGroups.add(includeGroup.intern());
        }
    }

    /**
     * Sets the value of '_includeGroupList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeGroups the Vector to set.
     */
    public void setIncludeGroupCollection(final List<String> includeGroups) {
        m_includeGroups = new ArrayList<String>();
        for (final String includeGroup : includeGroups) {
            m_includeGroups.add(includeGroup.intern());
        }
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
     * org.opennms.netmgt.config.datacollection.Collect
     */
    @Deprecated
    public static Collect unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Collect)Unmarshaller.unmarshal(Collect.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate() throws ValidationException {
        new org.exolab.castor.xml.Validator().validate(this);
    }

}
