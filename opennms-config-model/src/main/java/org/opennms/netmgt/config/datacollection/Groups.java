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

package org.opennms.netmgt.config.datacollection;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * MIB object groups
 */

@XmlRootElement(name="groups", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"group"})
@ValidateUsing("datacollection-config.xsd")
public class Groups implements Serializable {
    private static final long serialVersionUID = 4574413441179220427L;

    private static final Group[] EMPTY_GROUP_ARRAY = new Group[0];
    /**
     * a MIB object group
     */
    private List<Group> m_groups = new ArrayList<Group>();


    public Groups() {
        super();
    }


    /**
     * 
     * 
     * @param group
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addGroup(final Group group) throws IndexOutOfBoundsException {
        m_groups.add(group);
    }

    /**
     * 
     * 
     * @param index
     * @param group
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addGroup(final int index, final Group group) throws IndexOutOfBoundsException {
        m_groups.add(index, group);
    }

    /**
     * Method enumerateGroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Group> enumerateGroup() {
        return java.util.Collections.enumeration(m_groups);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof Groups) {
        
            final Groups temp = (Groups)obj;
            if (m_groups != null) {
                if (temp.m_groups == null) return false;
                else if (!(m_groups.equals(temp.m_groups))) 
                    return false;
            }
            else if (temp.m_groups != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getGroup.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * types.Group at the
     * given index
     */
    public Group getGroup(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_groups.size()) {
            throw new IndexOutOfBoundsException("getGroup: Index value '" + index + "' not in range [0.." + (m_groups.size() - 1) + "]");
        }
        return m_groups.get(index);
    }

    /**
     * Method getGroup.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="group")
    public Group[] getGroup() {
        return m_groups.toArray(EMPTY_GROUP_ARRAY);
    }

    /**
     * Method getGroupCollection.Returns a reference to
     * '_groupList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Group> getGroupCollection() {
        return m_groups;
    }

    /**
     * Method getGroupCount.
     * 
     * @return the size of this collection
     */
    public int getGroupCount() {
        return m_groups.size();
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
        
        if (m_groups != null) {
           result = 37 * result + m_groups.hashCode();
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
     * Method iterateGroup.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Group> iterateGroup() {
        return m_groups.iterator();
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
    @Deprecated
    public void marshal(final java.io.Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    @Deprecated
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllGroup() {
        m_groups.clear();
    }

    /**
     * Method removeGroup.
     * 
     * @param group
     * @return true if the object was removed from the collection.
     */
    public boolean removeGroup(final Group group) {
        return m_groups.remove(group);
    }

    /**
     * Method removeGroupAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Group removeGroupAt(final int index) {
        return m_groups.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param group
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setGroup(final int index, final Group group) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_groups.size()) {
            throw new IndexOutOfBoundsException("setGroup: Index value '" + index + "' not in range [0.." + (m_groups.size() - 1) + "]");
        }
        m_groups.set(index, group);
    }

    /**
     * 
     * 
     * @param groups
     */
    public void setGroup(final Group[] groups) {
        m_groups.clear();
        for (int i = 0; i < groups.length; i++) {
                m_groups.add(groups[i]);
        }
    }

    /**
     * Sets the value of '_groupList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param groups the Vector to copy.
     */
    public void setGroup(final List<Group> groups) {
        if (m_groups == groups) return;
        m_groups.clear();
        m_groups.addAll(groups);
    }

    /**
     * Sets the value of '_groupList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param groups the Vector to set.
     */
    public void setGroupCollection(final List<Group> groups) {
        m_groups = groups;
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
     * Groups
     */
    @Deprecated
    public static Groups unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Groups) Unmarshaller.unmarshal(Groups.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

}
