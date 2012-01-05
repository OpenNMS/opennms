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
 * Top-level element for the datacollection-config.xml
 *  configuration file.
 */

@XmlRootElement(name="datacollection-config", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class DatacollectionConfig implements Serializable {
    private static final long serialVersionUID = 9164334193148732102L;

    private static final SnmpCollection[] EMPTY_SNMPCOLLECTION_ARRAY = new SnmpCollection[0];

    /**
     * full path to the RRD repository for collected SNMP
     *  data
     */
    private String m_rrdRepository;

    /**
     * SNMP data collection element
     */
    private List<SnmpCollection> m_snmpCollections = new ArrayList<SnmpCollection>();


    public DatacollectionConfig() {
        super();
    }


    /**
     * 
     * 
     * @param snmpCollection
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSnmpCollection(final SnmpCollection snmpCollection) throws IndexOutOfBoundsException {
        m_snmpCollections.add(snmpCollection);
    }

    /**
     * 
     * 
     * @param index
     * @param snmpCollection
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addSnmpCollection(final int index, final SnmpCollection snmpCollection) throws IndexOutOfBoundsException {
        m_snmpCollections.add(index, snmpCollection);
    }

    /**
     * Method enumerateSnmpCollection.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<SnmpCollection> enumerateSnmpCollection() {
        return java.util.Collections.enumeration(m_snmpCollections);
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
        
        if (obj instanceof DatacollectionConfig) {
        
            final DatacollectionConfig temp = (DatacollectionConfig)obj;
            if (m_rrdRepository != null) {
                if (temp.m_rrdRepository == null) return false;
                else if (!(m_rrdRepository.equals(temp.m_rrdRepository))) 
                    return false;
            }
            else if (temp.m_rrdRepository != null)
                return false;
            if (m_snmpCollections != null) {
                if (temp.m_snmpCollections == null) return false;
                else if (!(m_snmpCollections.equals(temp.m_snmpCollections))) 
                    return false;
            }
            else if (temp.m_snmpCollections != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'rrdRepository'. The field
     * 'rrdRepository' has the following description: full path to
     * the RRD repository for collected SNMP
     *  data
     * 
     * @return the value of field 'RrdRepository'.
     */
    @XmlAttribute(name="rrdRepository")
    public String getRrdRepository() {
        return m_rrdRepository;
    }

    /**
     * Method getSnmpCollection.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * SnmpCollection at
     * the given index
     */
    public SnmpCollection getSnmpCollection(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_snmpCollections.size()) {
            throw new IndexOutOfBoundsException("getSnmpCollection: Index value '" + index + "' not in range [0.." + (m_snmpCollections.size() - 1) + "]");
        }
        return m_snmpCollections.get(index);
    }

    /**
     * Method getSnmpCollection.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public SnmpCollection[] getSnmpCollection() {
        return m_snmpCollections.toArray(EMPTY_SNMPCOLLECTION_ARRAY);
    }

    /**
     * Method getSnmpCollectionCollection.Returns a reference to
     * '_snmpCollectionList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    @XmlElement(name="snmp-collection")
    public List<SnmpCollection> getSnmpCollectionCollection() {
        return m_snmpCollections;
    }

    /**
     * Method getSnmpCollectionCount.
     * 
     * @return the size of this collection
     */
    public int getSnmpCollectionCount() {
        return m_snmpCollections.size();
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

        if (m_rrdRepository != null) {
           result = 37 * result + m_rrdRepository.hashCode();
        }
        if (m_snmpCollections != null) {
           result = 37 * result + m_snmpCollections.hashCode();
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
     * Method iterateSnmpCollection.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<SnmpCollection> iterateSnmpCollection() {
        return m_snmpCollections.iterator();
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

    public void removeAllSnmpCollection() {
        m_snmpCollections.clear();
    }

    /**
     * Method removeSnmpCollection.
     * 
     * @param snmpCollection
     * @return true if the object was removed from the collection.
     */
    public boolean removeSnmpCollection(final SnmpCollection snmpCollection) {
        return m_snmpCollections.remove(snmpCollection);
    }

    /**
     * Method removeSnmpCollectionAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public SnmpCollection removeSnmpCollectionAt(final int index) {
        return m_snmpCollections.remove(index);
    }

    /**
     * Sets the value of field 'rrdRepository'. The field
     * 'rrdRepository' has the following description: full path to
     * the RRD repository for collected SNMP
     *  data
     * 
     * @param rrdRepository the value of field 'rrdRepository'.
     */
    public void setRrdRepository(final String rrdRepository) {
        m_rrdRepository = rrdRepository.intern();
    }

    /**
     * 
     * 
     * @param index
     * @param snmpCollection
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setSnmpCollection(final int index, final SnmpCollection snmpCollection) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_snmpCollections.size()) {
            throw new IndexOutOfBoundsException("setSnmpCollection: Index value '" + index + "' not in range [0.." + (m_snmpCollections.size() - 1) + "]");
        }
        m_snmpCollections.set(index, snmpCollection);
    }

    /**
     * 
     * 
     * @param snmpCollections
     */
    public void setSnmpCollection(final SnmpCollection[] snmpCollections) {
        m_snmpCollections.clear();
        for (int i = 0; i < snmpCollections.length; i++) {
                m_snmpCollections.add(snmpCollections[i]);
        }
    }

    /**
     * Sets the value of '_snmpCollectionList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param snmpCollections the Vector to copy.
     */
    public void setSnmpCollection(final List<SnmpCollection> snmpCollections) {
        m_snmpCollections.clear();
        m_snmpCollections.addAll(snmpCollections);
    }

    /**
     * Sets the value of '_snmpCollectionList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param snmpCollections the Vector to set.
     */
    public void setSnmpCollectionCollection(final List<SnmpCollection> snmpCollections) {
        m_snmpCollections = snmpCollections;
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
     * DatacollectionConfig
     */
    @Deprecated
    public static DatacollectionConfig unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (DatacollectionConfig) Unmarshaller.unmarshal(DatacollectionConfig.class, reader);
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
