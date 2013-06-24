/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.ackd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
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

/**
 * Class Reader.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "reader")
@XmlAccessorType(XmlAccessType.FIELD)
public class Reader implements Serializable {
    private static final long serialVersionUID = -7392047026243024472L;

    public static final boolean DEFAULT_ENABLED_FLAG = true;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * The reader name is the value returned by the getName() method required
     * by the AckReader interface. Readers are currently wired in using
     * Spring.
     * 
     */
    @XmlAttribute(name = "reader-name")
    private String _readerName;

    /**
     * Field _enabled.
     */
    @XmlAttribute(name = "enabled")
    private Boolean _enabled;

    /**
     * A very basic configuration for defining simple input to a schedule
     * (java.lang.concurrent)
     * 
     */
    @XmlElement(name = "reader-schedule")
    private ReaderSchedule _readerSchedule;

    /**
     * Parameters to be used for collecting this service. Parameters are
     * specfic to the service monitor.
     */
    @XmlElement(name = "parameter")
    private List<Parameter> _parameterList = new ArrayList<Parameter>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Reader() {
        super();
    }

    public Reader(final String name, final boolean enabled,
            final ReaderSchedule schedule, final List<Parameter> parameters) {
        super();
        setReaderName(name);
        setEnabled(enabled);
        setReaderSchedule(schedule);
        setParameter(parameters);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vParameter
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addParameter(final Parameter vParameter)
            throws IndexOutOfBoundsException {
        this._parameterList.add(vParameter);
    }

    /**
     * 
     * 
     * @param index
     * @param vParameter
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addParameter(final int index, final Parameter vParameter)
            throws IndexOutOfBoundsException {
        this._parameterList.add(index, vParameter);
    }

    /**
     * Method enumerateParameter.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Parameter> enumerateParameter() {
        return Collections.enumeration(this._parameterList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reader other = (Reader) obj;
        if (_enabled == null) {
            if (other._enabled != null)
                return false;
        } else if (!_enabled.equals(other._enabled))
            return false;
        if (_parameterList == null) {
            if (other._parameterList != null)
                return false;
        } else if (!_parameterList.equals(other._parameterList))
            return false;
        if (_readerName == null) {
            if (other._readerName != null)
                return false;
        } else if (!_readerName.equals(other._readerName))
            return false;
        if (_readerSchedule == null) {
            if (other._readerSchedule != null)
                return false;
        } else if (!_readerSchedule.equals(other._readerSchedule))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'enabled'.
     * 
     * @return the value of field 'Enabled'.
     */
    public boolean getEnabled() {
        return _enabled == null ? DEFAULT_ENABLED_FLAG : _enabled;
    }

    /**
     * Method getParameter.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.ackd.Parameter at
     *         the given index
     */
    public Parameter getParameter(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._parameterList.size()) {
            throw new IndexOutOfBoundsException("getParameter: Index value '"
                    + index + "' not in range [0.."
                    + (this._parameterList.size() - 1) + "]");
        }

        return (Parameter) _parameterList.get(index);
    }

    /**
     * Method getParameter.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public Parameter[] getParameter() {
        Parameter[] array = new Parameter[0];
        return (Parameter[]) this._parameterList.toArray(array);
    }

    /**
     * Method getParameterCollection.Returns a reference to '_parameterList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Parameter> getParameterCollection() {
        return this._parameterList;
    }

    /**
     * Method getParameterCount.
     * 
     * @return the size of this collection
     */
    public int getParameterCount() {
        return this._parameterList.size();
    }

    /**
     * Returns the value of field 'readerName'. The field 'readerName' has the
     * following description: The reader name is the value returned by the
     * getName() method required by the AckReader interface. Readers are
     * currently wired in using Spring.
     * 
     * 
     * @return the value of field 'ReaderName'.
     */
    public String getReaderName() {
        return this._readerName;
    }

    /**
     * Returns the value of field 'readerSchedule'. The field 'readerSchedule'
     * has the following description: A very basic configuration for defining
     * simple input to a schedule (java.lang.concurrent)
     * 
     * 
     * @return the value of field 'ReaderSchedule'.
     */
    public ReaderSchedule getReaderSchedule() {
        return this._readerSchedule;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_enabled == null) ? 0 : _enabled.hashCode());
        result = prime * result
                + ((_parameterList == null) ? 0 : _parameterList.hashCode());
        result = prime * result
                + ((_readerName == null) ? 0 : _readerName.hashCode());
        result = prime
                * result
                + ((_readerSchedule == null) ? 0 : _readerSchedule.hashCode());
        return result;
    }

    /**
     * Returns the value of field 'enabled'.
     * 
     * @return the value of field 'Enabled'.
     */
    public boolean isEnabled() {
        return _enabled == null ? DEFAULT_ENABLED_FLAG : _enabled;
    }

    /**
     * Method iterateParameter.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Parameter> iterateParameter() {
        return this._parameterList.iterator();
    }

    /**
     */
    public void removeAllParameter() {
        this._parameterList.clear();
    }

    /**
     * Method removeParameter.
     * 
     * @param vParameter
     * @return true if the object was removed from the collection.
     */
    public boolean removeParameter(final Parameter vParameter) {
        return _parameterList.remove(vParameter);
    }

    /**
     * Method removeParameterAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Parameter removeParameterAt(final int index) {
        return this._parameterList.remove(index);
    }

    /**
     * Sets the value of field 'enabled'.
     * 
     * @param enabled
     *            the value of field 'enabled'.
     */
    public void setEnabled(final boolean enabled) {
        this._enabled = enabled;
    }

    /**
     * 
     * 
     * @param index
     * @param vParameter
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setParameter(final int index, final Parameter vParameter)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._parameterList.size()) {
            throw new IndexOutOfBoundsException("setParameter: Index value '"
                    + index + "' not in range [0.."
                    + (this._parameterList.size() - 1) + "]");
        }

        this._parameterList.set(index, vParameter);
    }

    /**
     * 
     * 
     * @param vParameterArray
     */
    public void setParameter(final Parameter[] vParameterArray) {
        // -- copy array
        _parameterList.clear();

        for (int i = 0; i < vParameterArray.length; i++) {
            this._parameterList.add(vParameterArray[i]);
        }
    }

    /**
     * Sets the value of '_parameterList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vParameterList
     *            the Vector to copy.
     */
    public void setParameter(final List<Parameter> vParameterList) {
        // copy vector
        this._parameterList.clear();

        this._parameterList.addAll(vParameterList);
    }

    /**
     * Sets the value of field 'readerName'. The field 'readerName' has the
     * following description: The reader name is the value returned by the
     * getName() method required by the AckReader interface. Readers are
     * currently wired in using Spring.
     * 
     * 
     * @param readerName
     *            the value of field 'readerName'.
     */
    public void setReaderName(final String readerName) {
        this._readerName = readerName;
    }

    /**
     * Sets the value of field 'readerSchedule'. The field 'readerSchedule'
     * has the following description: A very basic configuration for defining
     * simple input to a schedule (java.lang.concurrent)
     * 
     * 
     * @param readerSchedule
     *            the value of field 'readerSchedule'.
     */
    public void setReaderSchedule(final ReaderSchedule readerSchedule) {
        this._readerSchedule = readerSchedule;
    }
}
