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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.core.xml.JaxbUtils;

/**
 * Service to be launched by the manager.
 * 
 * @version $Revision$ $Date$
 */
public class Service implements Serializable {
    private static final long serialVersionUID = -2554947387909986065L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * Field _name.
     */
    @XmlElement(name = "name")
    private String _name;

    /**
     * Field _className.
     */
    @XmlElement(name = "class-name")
    private String _className;

    /**
     * Field _attributeList.
     */
    @XmlElement(name = "attribute")
    private List<Attribute> _attributeList = new ArrayList<Attribute>(0);;

    /**
     * Field _invokeList.
     */
    @XmlElement(name = "invoke")
    private List<Invoke> _invokeList = new ArrayList<Invoke>(0);

    // ----------------/
    // - Constructors -/
    // ----------------/

    public Service() {
        super();
    }

    public Service(final String name, final String className,
            final List<Attribute> attributes, final List<Invoke> invokes) {
        super();
        setName(name);
        setClassName(className);
        setAttribute(attributes);
        setInvoke(invokes);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     * 
     * 
     * @param vAttribute
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAttribute(final Attribute vAttribute)
            throws IndexOutOfBoundsException {
        this._attributeList.add(vAttribute);
    }

    /**
     * 
     * 
     * @param index
     * @param vAttribute
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addAttribute(final int index, final Attribute vAttribute)
            throws IndexOutOfBoundsException {
        this._attributeList.add(index, vAttribute);
    }

    /**
     * 
     * 
     * @param vInvoke
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addInvoke(final Invoke vInvoke)
            throws IndexOutOfBoundsException {
        this._invokeList.add(vInvoke);
    }

    /**
     * 
     * 
     * @param index
     * @param vInvoke
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addInvoke(final int index, final Invoke vInvoke)
            throws java.lang.IndexOutOfBoundsException {
        this._invokeList.add(index, vInvoke);
    }

    /**
     * Method enumerateAttribute.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Attribute> enumerateAttribute() {
        return Collections.enumeration(this._attributeList);
    }

    /**
     * Method enumerateInvoke.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Invoke> enumerateInvoke() {
        return Collections.enumeration(this._invokeList);
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

        if (obj instanceof Service) {

            Service temp = (Service) obj;
            if (this._name != null) {
                if (temp._name == null)
                    return false;
                else if (!(this._name.equals(temp._name)))
                    return false;
            } else if (temp._name != null)
                return false;
            if (this._className != null) {
                if (temp._className == null)
                    return false;
                else if (!(this._className.equals(temp._className)))
                    return false;
            } else if (temp._className != null)
                return false;
            if (this._attributeList != null) {
                if (temp._attributeList == null)
                    return false;
                else if (!(this._attributeList.equals(temp._attributeList)))
                    return false;
            } else if (temp._attributeList != null)
                return false;
            if (this._invokeList != null) {
                if (temp._invokeList == null)
                    return false;
                else if (!(this._invokeList.equals(temp._invokeList)))
                    return false;
            } else if (temp._invokeList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getAttribute.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.service.Attribute at
     *         the given inde
     */
    public Attribute getAttribute(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attributeList.size()) {
            throw new IndexOutOfBoundsException("getAttribute: Index value '"
                    + index + "' not in range [0.."
                    + (this._attributeList.size() - 1) + "]");
        }

        return (Attribute) _attributeList.get(index);
    }

    /**
     * Method getAttribute.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    @XmlTransient
    public Attribute[] getAttribute() {
        Attribute[] array = new Attribute[0];
        return (Attribute[]) this._attributeList.toArray(array);
    }

    /**
     * Method getAttributeCollection.Returns a reference to '_attributeList'.
     * No type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    @XmlTransient
    public List<Attribute> getAttributeCollection() {
        return this._attributeList;
    }

    /**
     * Method getAttributeCount.
     * 
     * @return the size of this collection
     */
    public int getAttributeCount() {
        return this._attributeList.size();
    }

    /**
     * Returns the value of field 'className'.
     * 
     * @return the value of field 'ClassName'.
     */
    @XmlTransient
    public String getClassName() {
        return this._className;
    }

    /**
     * Method getInvoke.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.service.Invoke at
     *         the given index
     */
    public Invoke getInvoke(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._invokeList.size()) {
            throw new IndexOutOfBoundsException("getInvoke: Index value '"
                    + index + "' not in range [0.."
                    + (this._invokeList.size() - 1) + "]");
        }

        return (Invoke) _invokeList.get(index);
    }

    /**
     * Method getInvoke.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    @XmlTransient
    public Invoke[] getInvoke() {
        Invoke[] array = new Invoke[0];
        return (Invoke[]) this._invokeList.toArray(array);
    }

    /**
     * Method getInvokeCollection.Returns a reference to '_invokeList'. No
     * type checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    @XmlTransient
    public java.util.List<Invoke> getInvokeCollection() {
        return this._invokeList;
    }

    /**
     * Method getInvokeCount.
     * 
     * @return the size of this collection
     */
    public int getInvokeCount() {
        return this._invokeList.size();
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    @XmlTransient
    public String getName() {
        return this._name;
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

        if (_name != null) {
            result = 37 * result + _name.hashCode();
        }
        if (_className != null) {
            result = 37 * result + _className.hashCode();
        }
        if (_attributeList != null) {
            result = 37 * result + _attributeList.hashCode();
        }
        if (_invokeList != null) {
            result = 37 * result + _invokeList.hashCode();
        }

        return result;
    }

    /**
     * Method iterateAttribute.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Attribute> iterateAttribute() {
        return this._attributeList.iterator();
    }

    /**
     * Method iterateInvoke.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Invoke> iterateInvoke() {
        return this._invokeList.iterator();
    }

    /**
     * 
     * 
     * @param out
     */
    public void marshal(final java.io.Writer out) {
        JaxbUtils.marshal(this, out);
    }

    /**
     */
    public void removeAllAttribute() {
        this._attributeList.clear();
    }

    /**
     */
    public void removeAllInvoke() {
        this._invokeList.clear();
    }

    /**
     * Method removeAttribute.
     * 
     * @param vAttribute
     * @return true if the object was removed from the collection.
     */
    public boolean removeAttribute(final Attribute vAttribute) {
        boolean removed = _attributeList.remove(vAttribute);
        return removed;
    }

    /**
     * Method removeAttributeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Attribute removeAttributeAt(final int index) {
        Object obj = this._attributeList.remove(index);
        return (Attribute) obj;
    }

    /**
     * Method removeInvoke.
     * 
     * @param vInvoke
     * @return true if the object was removed from the collection.
     */
    public boolean removeInvoke(final Invoke vInvoke) {
        boolean removed = _invokeList.remove(vInvoke);
        return removed;
    }

    /**
     * Method removeInvokeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Invoke removeInvokeAt(final int index) {
        Object obj = this._invokeList.remove(index);
        return (Invoke) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vAttribute
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setAttribute(final int index, final Attribute vAttribute)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._attributeList.size()) {
            throw new IndexOutOfBoundsException("setAttribute: Index value '"
                    + index + "' not in range [0.."
                    + (this._attributeList.size() - 1) + "]");
        }

        this._attributeList.set(index, vAttribute);
    }

    /**
     * 
     * 
     * @param vAttributeArray
     */
    public void setAttribute(final Attribute[] vAttributeArray) {
        // -- copy array
        _attributeList.clear();

        for (int i = 0; i < vAttributeArray.length; i++) {
            this._attributeList.add(vAttributeArray[i]);
        }
    }

    /**
     * Sets the value of '_attributeList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vAttributeList
     *            the Vector to copy.
     */
    public void setAttribute(final List<Attribute> vAttributeList) {
        // copy vector
        this._attributeList.clear();

        this._attributeList.addAll(vAttributeList);
    }

    /**
     * Sets the value of field 'className'.
     * 
     * @param className
     *            the value of field 'className'.
     */
    public void setClassName(final String className) {
        this._className = className;
    }

    /**
     * 
     * 
     * @param index
     * @param vInvoke
     * @throws java.lang.IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setInvoke(final int index, final Invoke vInvoke)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._invokeList.size()) {
            throw new IndexOutOfBoundsException("setInvoke: Index value '"
                    + index + "' not in range [0.."
                    + (this._invokeList.size() - 1) + "]");
        }

        this._invokeList.set(index, vInvoke);
    }

    /**
     * 
     * 
     * @param vInvokeArray
     */
    public void setInvoke(final Invoke[] vInvokeArray) {
        // -- copy array
        _invokeList.clear();

        for (int i = 0; i < vInvokeArray.length; i++) {
            this._invokeList.add(vInvokeArray[i]);
        }
    }

    /**
     * Sets the value of '_invokeList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vInvokeList
     *            the Vector to copy.
     */
    public void setInvoke(final List<Invoke> vInvokeList) {
        // copy vector
        this._invokeList.clear();

        this._invokeList.addAll(vInvokeList);
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name
     *            the value of field 'name'.
     */
    public void setName(final java.lang.String name) {
        this._name = name;
    }
}
