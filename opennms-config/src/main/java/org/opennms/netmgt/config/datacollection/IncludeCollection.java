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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * Class IncludeCollection.
 */

@XmlRootElement(name="include-collection", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class IncludeCollection implements Serializable {
    private static final long serialVersionUID = -1484624151254635162L;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * System Definition Name
     */
    private String m_systemDef;

    /**
     * Data Collection Group Name
     */
    private String m_dataCollectionGroup;

    /**
     * Exclude filter uses regular expression syntax to avoid
     * certain system definitions
     */
    private List<String> m_excludeFilters = new ArrayList<String>();


    public IncludeCollection() {
        super();
    }


    /**
     * 
     * 
     * @param excludeFilter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addExcludeFilter(final String excludeFilter) throws IndexOutOfBoundsException {
        m_excludeFilters.add(excludeFilter);
    }

    /**
     * 
     * 
     * @param index
     * @param excludeFilter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addExcludeFilter(final int index, final String excludeFilter) throws IndexOutOfBoundsException {
        m_excludeFilters.add(index, excludeFilter);
    }

    /**
     * Method enumerateExcludeFilter.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateExcludeFilter() {
        return Collections.enumeration(m_excludeFilters);
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
        
        if (obj instanceof IncludeCollection) {
        
            final IncludeCollection temp = (IncludeCollection)obj;
            if (m_systemDef != null) {
                if (temp.m_systemDef == null) return false;
                else if (!(m_systemDef.equals(temp.m_systemDef))) 
                    return false;
            }
            else if (temp.m_systemDef != null)
                return false;
            if (m_dataCollectionGroup != null) {
                if (temp.m_dataCollectionGroup == null) return false;
                else if (!(m_dataCollectionGroup.equals(temp.m_dataCollectionGroup))) 
                    return false;
            }
            else if (temp.m_dataCollectionGroup != null)
                return false;
            if (m_excludeFilters != null) {
                if (temp.m_excludeFilters == null) return false;
                else if (!(m_excludeFilters.equals(temp.m_excludeFilters))) 
                    return false;
            }
            else if (temp.m_excludeFilters != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'dataCollectionGroup'. The field
     * 'dataCollectionGroup' has the following description: Data
     * Collection Group Name
     * 
     * @return the value of field 'DataCollectionGroup'.
     */
    @XmlAttribute(name="dataCollectionGroup")
    public String getDataCollectionGroup() {
        return m_dataCollectionGroup;
    }

    /**
     * Method getExcludeFilter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getExcludeFilter(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_excludeFilters.size()) {
            throw new IndexOutOfBoundsException("getExcludeFilter: Index value '" + index + "' not in range [0.." + (m_excludeFilters.size() - 1) + "]");
        }
        return m_excludeFilters.get(index);
    }

    /**
     * Method getExcludeFilter.Returns the contents of the
     * collection in an Array.  <p>Note:  Just in case the
     * collection contents are changing in another thread, we pass
     * a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of
     * exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="exclude-filter")
    public String[] getExcludeFilter() {
        return m_excludeFilters.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Method getExcludeFilterCollection.Returns a reference to
     * '_excludeFilterList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getExcludeFilterCollection() {
        return m_excludeFilters;
    }

    /**
     * Method getExcludeFilterCount.
     * 
     * @return the size of this collection
     */
    public int getExcludeFilterCount() {
        return m_excludeFilters.size();
    }

    /**
     * Returns the value of field 'systemDef'. The field
     * 'systemDef' has the following description: System Definition
     * Name
     * 
     * @return the value of field 'SystemDef'.
     */
    @XmlAttribute(name="systemDef")
    public String getSystemDef() {
        return m_systemDef;
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

        if (m_systemDef != null) {
           result = 37 * result + m_systemDef.hashCode();
        }
        if (m_dataCollectionGroup != null) {
           result = 37 * result + m_dataCollectionGroup.hashCode();
        }
        if (m_excludeFilters != null) {
           result = 37 * result + m_excludeFilters.hashCode();
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
     * Method iterateExcludeFilter.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateExcludeFilter() {
        return m_excludeFilters.iterator();
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
    public void removeAllExcludeFilter() {
        m_excludeFilters.clear();
    }

    /**
     * Method removeExcludeFilter.
     * 
     * @param excludeFilter
     * @return true if the object was removed from the collection.
     */
    public boolean removeExcludeFilter(final String excludeFilter) {
        return m_excludeFilters.remove(excludeFilter);
    }

    /**
     * Method removeExcludeFilterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeExcludeFilterAt(final int index) {
        return m_excludeFilters.remove(index);
    }

    /**
     * Sets the value of field 'dataCollectionGroup'. The field
     * 'dataCollectionGroup' has the following description: Data
     * Collection Group Name
     * 
     * @param dataCollectionGroup the value of field
     * 'dataCollectionGroup'.
     */
    public void setDataCollectionGroup(final String dataCollectionGroup) {
        m_dataCollectionGroup = dataCollectionGroup.intern();
    }

    /**
     * @param index
     * @param excludeFilter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setExcludeFilter(final int index, final String excludeFilter) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_excludeFilters.size()) {
            throw new IndexOutOfBoundsException("setExcludeFilter: Index value '" + index + "' not in range [0.." + (m_excludeFilters.size() - 1) + "]");
        }
        m_excludeFilters.set(index, excludeFilter.intern());
    }

    /**
     * 
     * 
     * @param excludeFilters
     */
    public void setExcludeFilter(final String[] excludeFilters) {
        m_excludeFilters.clear();
        for (int i = 0; i < excludeFilters.length; i++) {
                m_excludeFilters.add(excludeFilters[i].intern());
        }
    }

    /**
     * Sets the value of '_excludeFilterList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param excludeFilters the Vector to copy.
     */
    public void setExcludeFilter(final List<String> excludeFilters) {
        m_excludeFilters.clear();
        for (final String excludeFilter : excludeFilters) {
            m_excludeFilters.add(excludeFilter.intern());
        }
    }

    /**
     * Sets the value of '_excludeFilterList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param excludeFilters the Vector to set.
     */
    public void setExcludeFilterCollection(final List<String> excludeFilters) {
        m_excludeFilters = new ArrayList<String>();
        for (final String excludeFilter : excludeFilters) {
            m_excludeFilters.add(excludeFilter.intern());
        }
    }

    /**
     * Sets the value of field 'systemDef'. The field 'systemDef'
     * has the following description: System Definition Name
     * 
     * @param systemDef the value of field 'systemDef'.
     */
    public void setSystemDef(final String systemDef) {
        m_systemDef = systemDef.intern();
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
     * IncludeCollection
     */
    @Deprecated
    public static IncludeCollection unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (IncludeCollection) Unmarshaller.unmarshal(IncludeCollection.class, reader);
    }

    /**
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate()
    throws ValidationException {
        new Validator().validate(this);
    }

}
