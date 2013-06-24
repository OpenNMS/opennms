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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;


/**
 * Selects a StorageStrategy that decides where data is stored.
 */

@XmlRootElement(name="storageStrategy", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
public class StorageStrategy implements Serializable {
    private static final long serialVersionUID = -2689731640740067448L;

    private static final Parameter[] EMPTY_PARAMETER_ARRAY = new Parameter[0];

    /**
     * Java class name of the class that implements the
     * StorageStrategy.
     */
    private String m_clazz;

    /**
     * list of parameters to pass to the strategy
     *  for strategy-specific configuration information
     */
    private List<Parameter> m_parameters = new ArrayList<Parameter>();

    public StorageStrategy() {
        super();
    }

    public StorageStrategy(final String clazz) {
        if (clazz != null)
            m_clazz = clazz.intern();
    }

    /**
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    /**
     * 
     * 
     * @param index
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addParameter(final int index, final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(index, parameter);
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Parameter> enumerateParameter() {
        return Collections.enumeration(m_parameters);
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
        
        if (obj instanceof StorageStrategy) {
        
            final StorageStrategy temp = (StorageStrategy)obj;
            if (m_clazz != null) {
                if (temp.m_clazz == null) return false;
                else if (!(m_clazz.equals(temp.m_clazz))) 
                    return false;
            }
            else if (temp.m_clazz != null)
                return false;
            if (m_parameters != null) {
                if (temp.m_parameters == null) return false;
                else if (!(m_parameters.equals(temp.m_parameters))) 
                    return false;
            }
            else if (temp.m_parameters != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'clazz'. The field 'clazz' has
     * the following description: Java class name of the class that
     * implements the StorageStrategy.
     * 
     * @return the value of field 'Clazz'.
     */
    @XmlAttribute(name="class", required=true)
    public String getClazz() {
        return m_clazz;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * Parameter at
     * the given index
     */
    public Parameter getParameter(final int index)
    throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_parameters.size()) {
            throw new IndexOutOfBoundsException("getParameter: Index value '" + index + "' not in range [0.." + (m_parameters.size() - 1) + "]");
        }
        return m_parameters.get(index);
    }

    /**
     * Method getParameter.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="parameter")
    public Parameter[] getParameter() {
        return m_parameters.toArray(EMPTY_PARAMETER_ARRAY);
    }

    /**
     * Method getParameterCollection.Returns a reference to
     * '_parameterList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Parameter> getParameterCollection() {
        return m_parameters;
    }

    /**
     * Method getParameterCount.
     * 
     * @return the size of this collection
     */
    public int getParameterCount() {
        return m_parameters.size();
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
        
        if (m_clazz != null) {
           result = 37 * result + m_clazz.hashCode();
        }
        if (m_parameters != null) {
           result = 37 * result + m_parameters.hashCode();
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
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Parameter> iterateParameter() {
        return m_parameters.iterator();
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
    public void removeAllParameter() {
        m_parameters.clear();
    }

    /**
     * Method removeParameter.
     * 
     * @param parameter
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Parameter removeParameterAt(final int index) {
        return m_parameters.remove(index);
    }

    /**
     * Sets the value of field 'clazz'. The field 'clazz' has the
     * following description: Java class name of the class that
     * implements the StorageStrategy.
     * 
     * @param clazz the value of field 'clazz'.
     */
    public void setClazz(final String clazz) {
        m_clazz = clazz.intern();
    }

    /**
     * 
     * 
     * @param index
     * @param parameter
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setParameter(final int index, final Parameter parameter) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_parameters.size()) {
            throw new IndexOutOfBoundsException("setParameter: Index value '" + index + "' not in range [0.." + (m_parameters.size() - 1) + "]");
        }
        m_parameters.set(index, parameter);
    }

    /**
     * 
     * 
     * @param parameters
     */
    public void setParameter(final Parameter[] parameters) {
        m_parameters.clear();
        for (int i = 0; i < parameters.length; i++) {
                m_parameters.add(parameters[i]);
        }
    }

    /**
     * Sets the value of '_parameterList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param parameters the Vector to copy.
     */
    public void setParameter(final List<Parameter> parameters) {
        if (m_parameters == parameters) return;
        m_parameters.clear();
        m_parameters.addAll(parameters);
    }

    /**
     * Sets the value of '_parameterList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param parameters the Vector to set.
     */
    public void setParameterCollection(final List<Parameter> parameters) {
        m_parameters = parameters;
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
     * StorageStrateg
     */
    @Deprecated
    public static StorageStrategy unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (StorageStrategy) Unmarshaller.unmarshal(StorageStrategy.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate()
    throws ValidationException {
        new Validator().validate(this);
    }
}
