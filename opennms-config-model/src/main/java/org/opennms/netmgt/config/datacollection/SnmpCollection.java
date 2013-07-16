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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * a grouping of SNMP related RRD parms, MIB object groups
 *  and sysoid based system definitions.
 */

@XmlRootElement(name="snmp-collection", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"rrd", "includeCollection", "resourceType", "groups", "systems"})
@ValidateUsing("datacollection-config.xsd")
public class SnmpCollection implements Serializable {
    private static final long serialVersionUID = 2466351093869349285L;

    private static final ResourceType[] EMPTY_RESOURCETYPE_ARRAY = new ResourceType[0];
    private static final IncludeCollection[] EMPTY_INCLUDECOLLECTION_ARRAY = new IncludeCollection[0];

    /**
     * collector name
     */
    private String m_name;

    private Integer m_maxVarsPerPdu;

    /**
     * indicates if collected SNMP data is to be stored for
     *  "all" interfaces or only for the "primary"
     *  interface.
     */
    private String m_snmpStorageFlag;

    /**
     * RRD parms
     */
    private Rrd m_rrd;

    /**
     * Include Collection by specifying
     *  System Definition Name or Data Collection Group Name.
     */
    private List<IncludeCollection> m_includeCollections = new ArrayList<IncludeCollection>();

    /**
     * Custom resource types
     */
    private List<ResourceType> m_resourceTypes = new ArrayList<ResourceType>();

    /**
     * MIB object groups
     */
    private Groups m_groups;

    /**
     * sysOid-based sytems
     */
    private Systems m_systems;


    public SnmpCollection() {
        super();
    }


    /**
     * Returns the value of field 'name'. The field 'name' has the
     * following description: collector name
     * 
     * @return the value of field 'Name'.
     */
    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    /**
     * Returns the value of field 'maxVarsPerPdu'. The field
     * 'maxVarsPerPdu' has the following description: DEPRECATED
     * 
     * @return the value of field 'MaxVarsPerPdu'.
     */
    @XmlAttribute(name="maxVarsPerPdu")
    public Integer getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    /**
     * Returns the value of field 'snmpStorageFlag'. The field
     * 'snmpStorageFlag' has the following description: indicates
     * if collected SNMP data is to be stored for
     *  "all" interfaces or only for the "primary"
     *  interface.
     * 
     * @return the value of field 'SnmpStorageFlag'.
     */
    @XmlAttribute(name="snmpStorageFlag", required=true)
    public String getSnmpStorageFlag() {
        return m_snmpStorageFlag;
    }

    /**
     * Returns the value of field 'rrd'. The field 'rrd' has the
     * following description: RRD parms
     * 
     * @return the value of field 'Rrd'.
     */
    @XmlElement(name="rrd", required=true)
    public Rrd getRrd() {
        return m_rrd;
    }

    /**
     * Method getIncludeCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="include-collection")
    public IncludeCollection[] getIncludeCollection() {
        return m_includeCollections.toArray(EMPTY_INCLUDECOLLECTION_ARRAY);
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
     * Returns the value of field 'groups'. The field 'groups' has
     * the following description: MIB object groups
     * 
     * @return the value of field 'Groups'.
     */
    @XmlElement(name="groups")
    public Groups getGroups() {
        return m_groups;
    }

    /**
     * Returns the value of field 'systems'. The field 'systems'
     * has the following description: sysOid-based sytems
     * 
     * @return the value of field 'Systems'.
     */
    @XmlElement(name="systems")
    public Systems getSystems() {
        return m_systems;
    }

    /**
     * 
     * 
     * @param includeCollection
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeCollection(final IncludeCollection includeCollection) throws IndexOutOfBoundsException {
        m_includeCollections.add(includeCollection);
    }

    /**
     * 
     * 
     * @param index
     * @param includeCollection
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIncludeCollection(final int index, final IncludeCollection includeCollection) throws IndexOutOfBoundsException {
        m_includeCollections.add(index, includeCollection);
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

    public void deleteMaxVarsPerPdu() {
        m_maxVarsPerPdu = null;
    }

    /**
     * Method enumerateIncludeCollection.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<IncludeCollection> enumerateIncludeCollection() {
        return Collections.enumeration(m_includeCollections);
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
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof SnmpCollection) {
            final SnmpCollection temp = (SnmpCollection)obj;

            return new EqualsBuilder()
                .append(m_name, temp.m_name)
                .append(m_maxVarsPerPdu, temp.m_maxVarsPerPdu)
                .append(m_snmpStorageFlag, temp.m_snmpStorageFlag)
                .append(m_rrd, temp.m_rrd)
                .append(m_includeCollections, temp.m_includeCollections)
                .append(m_resourceTypes, temp.m_resourceTypes)
                .append(m_groups, temp.m_groups)
                .append(m_systems, temp.m_systems)
                .isEquals();
        }
        return false;
    }

    /**
     * Method getIncludeCollection.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * IncludeCollection
     * at the given index
     */
    public IncludeCollection getIncludeCollection(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_includeCollections.size()) {
            throw new IndexOutOfBoundsException("getIncludeCollection: Index value '" + index + "' not in range [0.." + (m_includeCollections.size() - 1) + "]");
        }
        return m_includeCollections.get(index);
    }

    /**
     * Method getIncludeCollectionCollection.Returns a reference to
     * '_includeCollectionList'. No type checking is performed on
     * any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<IncludeCollection> getIncludeCollectionCollection() {
        return m_includeCollections;
    }

    /**
     * Method getIncludeCollectionCount.
     * 
     * @return the size of this collection
     */
    public int getIncludeCollectionCount() {
        return m_includeCollections.size();
    }

    /**
     * Method getResourceType.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * types.ResourceType
     * at the given index
     */
    public ResourceType getResourceType(final int index)  throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_resourceTypes.size()) {
            throw new IndexOutOfBoundsException("getResourceType: Index value '" + index + "' not in range [0.." + (m_resourceTypes.size() - 1) + "]");
        }
        return m_resourceTypes.get(index);
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
     * Method hasMaxVarsPerPdu.
     * 
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu() {
        return m_maxVarsPerPdu != null;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming
     * Language Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int result = 17;
        
        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_maxVarsPerPdu != null) {
            result = 37 * result + m_maxVarsPerPdu;
        }
        if (m_snmpStorageFlag != null) {
           result = 37 * result + m_snmpStorageFlag.hashCode();
        }
        if (m_rrd != null) {
           result = 37 * result + m_rrd.hashCode();
        }
        if (m_includeCollections != null) {
           result = 37 * result + m_includeCollections.hashCode();
        }
        if (m_resourceTypes != null) {
           result = 37 * result + m_resourceTypes.hashCode();
        }
        if (m_groups != null) {
           result = 37 * result + m_groups.hashCode();
        }
        if (m_systems != null) {
           result = 37 * result + m_systems.hashCode();
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
     * Method iterateIncludeCollection.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<IncludeCollection> iterateIncludeCollection() {
        return m_includeCollections.iterator();
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

    public void removeAllIncludeCollection() {
        m_includeCollections.clear();
    }

    public void removeAllResourceType() {
        m_resourceTypes.clear();
    }

    /**
     * Method removeIncludeCollection.
     * 
     * @param includeCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeIncludeCollection(final IncludeCollection includeCollection) {
        return m_includeCollections.remove(includeCollection);
    }

    /**
     * Method removeIncludeCollectionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public IncludeCollection removeIncludeCollectionAt(final int index) {
        return m_includeCollections.remove(index);
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
     * Sets the value of field 'groups'. The field 'groups' has the
     * following description: MIB object groups
     * 
     * @param groups the value of field 'groups'.
     */
    public void setGroups(final Groups groups) {
        m_groups = groups;
    }

    /**
     * @param index
     * @param includeCollection
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIncludeCollection(final int index, final IncludeCollection includeCollection) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_includeCollections.size()) {
            throw new IndexOutOfBoundsException("setIncludeCollection: Index value '" + index + "' not in range [0.." + (m_includeCollections.size() - 1) + "]");
        }
        m_includeCollections.set(index, includeCollection);
    }

    /**
     * @param includeCollections
     */
    public void setIncludeCollection(final IncludeCollection[] includeCollections) {
        m_includeCollections.clear();
        for (int i = 0; i < includeCollections.length; i++) {
                m_includeCollections.add(includeCollections[i]);
        }
    }

    /**
     * Sets the value of '_includeCollectionList' by copying the
     * given Vector. All elements will be checked for type safety.
     * 
     * @param includeCollections the Vector to copy.
     */
    public void setIncludeCollection(final List<IncludeCollection> includeCollections) {
        if (m_includeCollections == includeCollections) return;
        m_includeCollections.clear();
        m_includeCollections.addAll(includeCollections);
    }

    /**
     * Sets the value of '_includeCollectionList' by setting it to
     * the given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param includeCollections the Vector to set.
     */
    public void setIncludeCollectionCollection(final List<IncludeCollection> includeCollections) {
        m_includeCollections = includeCollections;
    }

    /**
     * Sets the value of field 'maxVarsPerPdu'. The field
     * 'maxVarsPerPdu' has the following description: DEPRECATED
     * 
     * @param maxVarsPerPdu the value of field 'maxVarsPerPdu'.
     */
    public void setMaxVarsPerPdu(final Integer maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the
     * following description: collector name
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name.intern();
    }

    /**
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
        if (m_resourceTypes == resourceTypes) return;
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
     * Sets the value of field 'rrd'. The field 'rrd' has the
     * following description: RRD parms
     * 
     * @param rrd the value of field 'rrd'.
     */
    public void setRrd(final Rrd rrd) {
        m_rrd = rrd;
    }

    /**
     * Sets the value of field 'snmpStorageFlag'. The field
     * 'snmpStorageFlag' has the following description: indicates
     * if collected SNMP data is to be stored for
     *  "all" interfaces or only for the "primary"
     *  interface.
     * 
     * @param snmpStorageFlag the value of field 'snmpStorageFlag'.
     */
    public void setSnmpStorageFlag(final String snmpStorageFlag) {
        m_snmpStorageFlag = snmpStorageFlag.intern();
    }

    /**
     * Sets the value of field 'systems'. The field 'systems' has
     * the following description: sysOid-based sytems
     * 
     * @param systems the value of field 'systems'.
     */
    public void setSystems(final Systems systems) {
        m_systems = systems;
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
     * SnmpCollection
     */
    @Deprecated
    public static SnmpCollection unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (SnmpCollection) Unmarshaller.unmarshal(SnmpCollection.class, reader);
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
