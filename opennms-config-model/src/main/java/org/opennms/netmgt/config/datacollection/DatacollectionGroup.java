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
import javax.xml.bind.annotation.XmlAttribute;
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
 * Top-level element for the datacollection group
 *  configuration file.
 */

@XmlRootElement(name="datacollection-group", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"name", "resourceType", "group", "systemDef"})
@ValidateUsing("datacollection-groups.xsd")
public class DatacollectionGroup implements Serializable {
    private static final long serialVersionUID = 659689462266282039L;

    private static final SystemDef[] EMPTY_SYSTEMDEF_ARRAY = new SystemDef[0];
    private static final ResourceType[] EMPTY_RESOURCETYPE_ARRAY = new ResourceType[0];
    private static final Group[] EMPTY_GROUP_ARRAY = new Group[0];

    /**
     * data collector group name
     */
    private String m_name;

    /**
     * Custom resource types
     */
    private List<ResourceType> m_resourceTypes = new ArrayList<ResourceType>();

    /**
     * a MIB object group
     */
    private List<Group> m_groups = new ArrayList<Group>();

    /**
     * list of system definitions
     */
    private List<SystemDef> m_systemDefs = new ArrayList<SystemDef>();


    public DatacollectionGroup() {
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
     * 
     * 
     * @param resourceType
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResourceType(final ResourceType resourceType) throws IndexOutOfBoundsException {
        m_resourceTypes.add(resourceType);
    }

    /**
     * 
     * 
     * @param index
     * @param resourceType
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addResourceType(final int index, final ResourceType resourceType) throws IndexOutOfBoundsException {
        m_resourceTypes.add(index, resourceType);
    }

    /**
     * 
     * 
     * @param systemDef
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSystemDef(final SystemDef systemDef) throws IndexOutOfBoundsException {
        m_systemDefs.add(systemDef);
    }

    /**
     * 
     * 
     * @param index
     * @param systemDef
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSystemDef(final int index, final SystemDef systemDef) throws IndexOutOfBoundsException {
        m_systemDefs.add(index, systemDef);
    }

    /**
     * Method enumerateGroup.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Group> enumerateGroup() {
        return Collections.enumeration(m_groups);
    }

    /**
     * Method enumerateResourceType.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<ResourceType> enumerateResourceType() {
        return Collections.enumeration(m_resourceTypes);
    }

    /**
     * Method enumerateSystemDef.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<SystemDef> enumerateSystemDef() {
        return Collections.enumeration(m_systemDefs);
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
        
        if (obj instanceof DatacollectionGroup) {
        
            final DatacollectionGroup temp = (DatacollectionGroup)obj;
            if (m_name != null) {
                if (temp.m_name == null) return false;
                else if (!(m_name.equals(temp.m_name))) 
                    return false;
            }
            else if (temp.m_name != null)
                return false;
            if (m_resourceTypes != null) {
                if (temp.m_resourceTypes == null) return false;
                else if (!(m_resourceTypes.equals(temp.m_resourceTypes))) 
                    return false;
            }
            else if (temp.m_resourceTypes != null)
                return false;
            if (m_groups != null) {
                if (temp.m_groups == null) return false;
                else if (!(m_groups.equals(temp.m_groups))) 
                    return false;
            }
            else if (temp.m_groups != null)
                return false;
            if (m_systemDefs != null) {
                if (temp.m_systemDefs == null) return false;
                else if (!(m_systemDefs.equals(temp.m_systemDefs))) 
                    return false;
            }
            else if (temp.m_systemDefs != null)
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
     * Group at the
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
     * Returns the value of field 'name'. The field 'name' has the
     * following description: data collector group name
     * 
     * @return the value of field 'Name'.
     */
    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    /**
     * Method getResourceType.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * ResourceType
     * at the given index
     */
    public ResourceType getResourceType(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_resourceTypes.size()) {
            throw new IndexOutOfBoundsException("getResourceType: Index value '" + index + "' not in range [0.." + (m_resourceTypes.size() - 1) + "]");
        }
        return m_resourceTypes.get(index);
    }

    /**
     * Method getResourceType.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="resourceType")
    public ResourceType[] getResourceType() {
        return m_resourceTypes.toArray(EMPTY_RESOURCETYPE_ARRAY);
    }

    /**
     * Method getResourceTypeCollection.Returns a reference to
     * '_resourceTypeList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<ResourceType> getResourceTypeCollection() {
        return m_resourceTypes;
    }

    /**
     * Method getResourceTypeCount.
     * 
     * @return the size of this collection
     */
    public int getResourceTypeCount() {
        return m_resourceTypes.size();
    }

    /**
     * Method getSystemDef.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * SystemDef at
     * the given index
     */
    public SystemDef getSystemDef(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_systemDefs.size()) {
            throw new IndexOutOfBoundsException("getSystemDef: Index value '" + index + "' not in range [0.." + (m_systemDefs.size() - 1) + "]");
        }
        return m_systemDefs.get(index);
    }

    /**
     * Method getSystemDef.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="systemDef")
    public SystemDef[] getSystemDef() {
        return m_systemDefs.toArray(EMPTY_SYSTEMDEF_ARRAY);
    }

    /**
     * Method getSystemDefCollection.Returns a reference to
     * '_systemDefList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<SystemDef> getSystemDefCollection() {
        return m_systemDefs;
    }

    /**
     * Method getSystemDefCount.
     * 
     * @return the size of this collection
     */
    public int getSystemDefCount() {
        return m_systemDefs.size();
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
        
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_resourceTypes != null) {
           result = 37 * result + m_resourceTypes.hashCode();
        }
        if (m_groups != null) {
           result = 37 * result + m_groups.hashCode();
        }
        if (m_systemDefs != null) {
           result = 37 * result + m_systemDefs.hashCode();
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
     * Method iterateResourceType.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<ResourceType> iterateResourceType() {
        return m_resourceTypes.iterator();
    }

    /**
     * Method iterateSystemDef.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<SystemDef> iterateSystemDef() {
        return m_systemDefs.iterator();
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
    public void marshal(final Writer out) throws MarshalException, ValidationException {
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

    /**
     */
    public void removeAllGroup() {
        m_groups.clear();
    }

    /**
     */
    public void removeAllResourceType() {
        m_resourceTypes.clear();
    }

    /**
     */
    public void removeAllSystemDef() {
        m_systemDefs.clear();
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
     * Method removeResourceType.
     * 
     * @param resourceType
     * @return true if the object was removed from the collection.
     */
    public boolean removeResourceType(final ResourceType resourceType) {
        return m_resourceTypes.remove(resourceType);
    }

    /**
     * Method removeResourceTypeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public ResourceType removeResourceTypeAt(final int index) {
        return m_resourceTypes.remove(index);
    }

    /**
     * Method removeSystemDef.
     * 
     * @param systemDef
     * @return true if the object was removed from the collection.
     */
    public boolean removeSystemDef(final SystemDef systemDef) {
        return m_systemDefs.remove(systemDef);
    }

    /**
     * Method removeSystemDefAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public SystemDef removeSystemDefAt(final int index) {
        return m_systemDefs.remove(index);
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
     * Sets the value of field 'name'. The field 'name' has the
     * following description: data collector group name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name.intern();
    }

    /**
     * 
     * 
     * @param index
     * @param resourceType
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setResourceType(final int index, final ResourceType resourceType) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_resourceTypes.size()) {
            throw new IndexOutOfBoundsException("setResourceType: Index value '" + index + "' not in range [0.." + (m_resourceTypes.size() - 1) + "]");
        }
        m_resourceTypes.set(index, resourceType);
    }

    /**
     * 
     * 
     * @param resourceTypes
     */
    public void setResourceType(final ResourceType[] resourceTypes) {
        m_resourceTypes.clear();
        for (int i = 0; i < resourceTypes.length; i++) {
                m_resourceTypes.add(resourceTypes[i]);
        }
    }

    /**
     * Sets the value of '_resourceTypeList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param resourceTypes the Vector to copy.
     */
    public void setResourceType(final List<ResourceType> resourceTypes) {
        m_resourceTypes.clear();
        m_resourceTypes.addAll(resourceTypes);
    }

    /**
     * Sets the value of '_resourceTypeList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param resourceTypes the Vector to set.
     */
    public void setResourceTypeCollection(final List<ResourceType> resourceTypes) {
        m_resourceTypes = resourceTypes;
    }

    /**
     * 
     * 
     * @param index
     * @param systemDef
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSystemDef(final int index, final SystemDef systemDef) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_systemDefs.size()) {
            throw new IndexOutOfBoundsException("setSystemDef: Index value '" + index + "' not in range [0.." + (m_systemDefs.size() - 1) + "]");
        }
        m_systemDefs.set(index, systemDef);
    }

    /**
     * 
     * 
     * @param systemDefs
     */
    public void setSystemDef(final SystemDef[] systemDefs) {
        m_systemDefs.clear();
        for (int i = 0; i < systemDefs.length; i++) {
                m_systemDefs.add(systemDefs[i]);
        }
    }

    /**
     * Sets the value of '_systemDefList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param systemDefs the Vector to copy.
     */
    public void setSystemDef(final List<SystemDef> systemDefs) {
        m_systemDefs.clear();
        m_systemDefs.addAll(systemDefs);
    }

    /**
     * Sets the value of '_systemDefList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param systemDefs the Vector to set.
     */
    public void setSystemDefCollection(final List<SystemDef> systemDefs) {
        m_systemDefs = systemDefs;
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
     * DatacollectionGroup
     */
    @Deprecated
    public static DatacollectionGroup unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (DatacollectionGroup) Unmarshaller.unmarshal(DatacollectionGroup.class, reader);
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
