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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;
import org.xml.sax.ContentHandler;

/**
 * RRD parms
 */

@XmlRootElement(name="rrd", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class Rrd implements Serializable {
    private static final long serialVersionUID = 8501536919916316365L;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * step size for the RRD
     */
    private Integer m_step;

    /**
     * Round Robin Archive definitions
     */
    private List<String> m_rras = new ArrayList<String>();


    public Rrd() {
        super();
    }


    /**
     * @param rra
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRra(final String rra) throws IndexOutOfBoundsException {
        m_rras.add(rra.intern());
    }

    /**
     * 
     * 
     * @param index
     * @param rra
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addRra(final int index, final String rra) throws IndexOutOfBoundsException {
        m_rras.add(index, rra.intern());
    }

    /**
     */
    public void deleteStep() {
        m_step = null;
    }

    /**
     * Method enumerateRra.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateRra() {
        return Collections.enumeration(m_rras);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final Object obj) {
        if ( this == obj ) return true;
        
        if (obj instanceof Rrd) {
            final Rrd temp = (Rrd)obj;

            return new EqualsBuilder()
                .append(m_rras, temp.m_rras)
                .append(m_step, temp.m_step)
                .isEquals();
        }
        return false;
    }

    /**
     * Method getRra.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getRra(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_rras.size()) {
            throw new IndexOutOfBoundsException("getRra: Index value '" + index + "' not in range [0.." + (m_rras.size() - 1) + "]");
        }
        return m_rras.get(index);
    }

    /**
     * Method getRra.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="rra", required=true)
    public String[] getRra() {
        return m_rras.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Method getRraCollection.Returns a reference to '_rraList'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getRraCollection() {
        return m_rras;
    }

    /**
     * Method getRraCount.
     * 
     * @return the size of this collection
     */
    public int getRraCount() {
        return m_rras.size();
    }

    /**
     * Returns the value of field 'step'. The field 'step' has the
     * following description: step size for the RRD
     * 
     * @return the value of field 'Step'.
     */
    @XmlAttribute(name="step", required=true)
    public Integer getStep() {
        return m_step == null? 0 : m_step;
    }

    /**
     * Method hasStep.
     * 
     * @return true if at least one Step has been added
     */
    public boolean hasStep() {
        return m_step != null;
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
        
        result = 37 * result + m_step;
        if (m_rras != null) {
           result = 37 * result + m_rras.hashCode();
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
     * Method iterateRra.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateRra() {
        return m_rras.iterator();
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

    public void removeAllRra() {
        m_rras.clear();
    }

    /**
     * Method removeRra.
     * 
     * @param rra
     * @return true if the object was removed from the collection.
     */
    public boolean removeRra(final String rra) {
        return m_rras.remove(rra);
    }

    /**
     * Method removeRraAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeRraAt(final int index) {
        return m_rras.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param rra
     * @throws IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setRra(final int index, final String rra) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_rras.size()) {
            throw new IndexOutOfBoundsException("setRra: Index value '" + index + "' not in range [0.." + (m_rras.size() - 1) + "]");
        }
        m_rras.set(index, rra.intern());
    }

    /**
     * 
     * 
     * @param rras
     */
    public void setRra(final String[] rras) {
        m_rras.clear();
        for (int i = 0; i < rras.length; i++) {
                m_rras.add(rras[i].intern());
        }
    }

    /**
     * Sets the value of '_rraList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param rras the Vector to copy.
     */
    public void setRra(final List<String> rras) {
        m_rras.clear();
        for (final String rra : rras) {
            m_rras.add(rra.intern());
        }
    }

    /**
     * Sets the value of '_rraList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param rras the Vector to set.
     */
    public void setRraCollection(final List<String> rras) {
        m_rras = new ArrayList<String>();
        for (final String rra : rras) {
            m_rras.add(rra.intern());
        }
    }

    /**
     * Sets the value of field 'step'. The field 'step' has the
     * following description: step size for the RRD
     * 
     * @param step the value of field 'step'.
     */
    public void setStep(final Integer step) {
        m_step = step;
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
     * Rrd
     */
    @Deprecated
    public static Rrd unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (Rrd) Unmarshaller.unmarshal(Rrd.class, reader);
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
