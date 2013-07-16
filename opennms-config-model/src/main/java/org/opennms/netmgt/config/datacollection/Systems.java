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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.datacollection;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * systems
 */

@XmlRootElement(name="systems", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class Systems implements Serializable {
    private static final long serialVersionUID = 1757643773135824946L;

    private static final SystemDef[] EMPTY_SYSTEMDEF_ARRAY = new SystemDef[0];

    /**
     * list of system definitions
     */
    private List<SystemDef> m_systemDefs = new ArrayList<SystemDef>();


    public Systems() {
        super();
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
     * Method enumerateSystemDef.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<SystemDef> enumerateSystemDef() {
        return Collections.enumeration(m_systemDefs);
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
        
        if (obj instanceof Systems) {
        
            final Systems temp = (Systems)obj;
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
     * Method getSystemDef.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * types.SystemDef at
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
     * @throws IOException if an IOException occurs during
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
    public void removeAllSystemDef() {
        m_systemDefs.clear();
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
        if (m_systemDefs == systemDefs) return;
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
     * Systems
     */
    @Deprecated
    public static Systems unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Systems) Unmarshaller.unmarshal(Systems.class, reader);
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
