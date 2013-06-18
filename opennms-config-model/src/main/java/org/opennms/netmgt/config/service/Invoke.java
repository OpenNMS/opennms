/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.config.service;

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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.netmgt.config.service.types.InvokeAtTypeAdapter;

/**
 * Class Invoke.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "invoke")
@XmlAccessorType(XmlAccessType.FIELD)
public class Invoke implements Serializable {
    private static final long serialVersionUID = 1295387509696778585L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _method.
     */
    @XmlAttribute(name = "method")
    private String _method;

    /**
     * Field _pass.
     */
    @XmlAttribute(name = "pass")
    private Integer _pass = 0;

    /**
     * Field _at.
     */
    @XmlAttribute(name = "at")
    @XmlJavaTypeAdapter(InvokeAtTypeAdapter.class)
    private InvokeAtType _at;

    /**
     * Field _argumentList.
     */
    @XmlElement(name = "argument")
    private List<Argument> _argumentList = new ArrayList<Argument>(0);;

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Invoke() {
        super();
    }

    public Invoke(final InvokeAtType type, final Integer pass,
            final String method, final List<Argument> arguments) {
        super();
        setAt(type);
        setPass(pass);
        setMethod(method);
        setArgument(arguments);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vArgument
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addArgument(final Argument vArgument)
            throws IndexOutOfBoundsException {
        this._argumentList.add(vArgument);
    }

    /**
     * 
     * 
     * @param index
     * @param vArgument
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addArgument(final int index, final Argument vArgument)
            throws IndexOutOfBoundsException {
        this._argumentList.add(index, vArgument);
    }

    /**
     */
    public void deletePass() {
        this._pass = null;
    }

    /**
     * Method enumerateArgument.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Argument> enumerateArgument() {
        return Collections.enumeration(this._argumentList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final java.lang.Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Invoke) {

            Invoke temp = (Invoke) obj;
            if (this._method != null) {
                if (temp._method == null)
                    return false;
                else if (!(this._method.equals(temp._method)))
                    return false;
            } else if (temp._method != null)
                return false;
            if (this._pass != temp._pass)
                return false;
            if (this._at != null) {
                if (temp._at == null)
                    return false;
                else if (!(this._at.equals(temp._at)))
                    return false;
            } else if (temp._at != null)
                return false;
            if (this._argumentList != null) {
                if (temp._argumentList == null)
                    return false;
                else if (!(this._argumentList.equals(temp._argumentList)))
                    return false;
            } else if (temp._argumentList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getArgument.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.service.Argument at
     *         the given index
     */
    public Argument getArgument(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._argumentList.size()) {
            throw new IndexOutOfBoundsException("getArgument: Index value '"
                    + index + "' not in range [0.."
                    + (this._argumentList.size() - 1) + "]");
        }

        return (Argument) _argumentList.get(index);
    }

    /**
     * Method getArgument.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public Argument[] getArgument() {
        Argument[] array = new Argument[0];
        return (Argument[]) this._argumentList.toArray(array);
    }

    /**
     * Method getArgumentCollection.Returns a reference to '_argumentList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Argument> getArgumentCollection() {
        return this._argumentList;
    }

    /**
     * Method getArgumentCount.
     * 
     * @return the size of this collection
     */
    public int getArgumentCount() {
        return this._argumentList.size();
    }

    /**
     * Returns the value of field 'at'.
     * 
     * @return the value of field 'At'.
     */
    public InvokeAtType getAt() {
        return this._at;
    }

    /**
     * Returns the value of field 'method'.
     * 
     * @return the value of field 'Method'.
     */
    public String getMethod() {
        return this._method;
    }

    /**
     * Returns the value of field 'pass'.
     * 
     * @return the value of field 'Pass'.
     */
    public int getPass() {
        return this._pass;
    }

    /**
     * Method hasPass.
     * 
     * @return true if at least one Pass has been added
     */
    public boolean hasPass() {
        return this._pass != null ? true : false;
    }

    /**
     * Overrides the java.lang.Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     * 
     * @return a hash code value for the object.
     */
    public int hashCode() {
        int result = 17;

        if (_method != null) {
            result = 37 * result + _method.hashCode();
        }
        result = 37 * result + _pass;
        if (_at != null) {
            result = 37 * result + _at.hashCode();
        }
        if (_argumentList != null) {
            result = 37 * result + _argumentList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateArgument.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Argument> iterateArgument() {
        return this._argumentList.iterator();
    }

    /**
     */
    public void removeAllArgument() {
        this._argumentList.clear();
    }

    /**
     * Method removeArgument.
     * 
     * @param vArgument
     * @return true if the object was removed from the collection.
     */
    public boolean removeArgument(final Argument vArgument) {
        boolean removed = _argumentList.remove(vArgument);
        return removed;
    }

    /**
     * Method removeArgumentAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Argument removeArgumentAt(final int index) {
        Object obj = this._argumentList.remove(index);
        return (Argument) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vArgument
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setArgument(final int index, final Argument vArgument)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._argumentList.size()) {
            throw new IndexOutOfBoundsException("setArgument: Index value '"
                    + index + "' not in range [0.."
                    + (this._argumentList.size() - 1) + "]");
        }

        this._argumentList.set(index, vArgument);
    }

    /**
     * 
     * 
     * @param vArgumentArray
     */
    public void setArgument(final Argument[] vArgumentArray) {
        // -- copy array
        _argumentList.clear();

        for (int i = 0; i < vArgumentArray.length; i++) {
            this._argumentList.add(vArgumentArray[i]);
        }
    }

    /**
     * Sets the value of '_argumentList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vArgumentList
     *            the Vector to copy.
     */
    public void setArgument(final List<Argument> vArgumentList) {
        // copy vector
        this._argumentList.clear();

        this._argumentList.addAll(vArgumentList);
    }

    /**
     * Sets the value of field 'at'.
     * 
     * @param at
     *            the value of field 'at'.
     */
    public void setAt(final InvokeAtType at) {
        this._at = at;
    }

    /**
     * Sets the value of field 'method'.
     * 
     * @param method
     *            the value of field 'method'.
     */
    public void setMethod(final String method) {
        this._method = method;
    }

    /**
     * Sets the value of field 'pass'.
     * 
     * @param pass
     *            the value of field 'pass'.
     */
    public void setPass(final int pass) {
        this._pass = pass;
    }
}
