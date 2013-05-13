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

package org.opennms.netmgt.config.poller;

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
 * A scheduled outage
 * 
 */

@XmlRootElement(name="outage", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class Outage extends BasicSchedule implements Serializable {
    private static final long serialVersionUID = 8039170922077782745L;

    /**
     * List of interfaces to which the outage
     *  applies.
     */
    @XmlElement(name="interface")
    private List<Interface> _interfaceList;

    /**
     * List of nodes to which the outage
     *  applies.
     */
    @XmlElement(name="node")
    private List<Node> _nodeList;

    public Outage() {
        super();
        this._interfaceList = new ArrayList<Interface>();
        this._nodeList = new ArrayList<Node>();
    }

    /**
     * 
     * 
     * @param vInterface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInterface(final Interface vInterface) throws IndexOutOfBoundsException {
        this._interfaceList.add(vInterface);
    }

    /**
     * 
     * 
     * @param index
     * @param vInterface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInterface(final int index, final Interface vInterface) throws IndexOutOfBoundsException {
        this._interfaceList.add(index, vInterface);
    }

    /**
     * 
     * 
     * @param vNode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNode(final Node vNode) throws IndexOutOfBoundsException {
        this._nodeList.add(vNode);
    }

    /**
     * 
     * 
     * @param index
     * @param vNode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNode(final int index, final Node vNode) throws IndexOutOfBoundsException {
        this._nodeList.add(index, vNode);
    }

    /**
     * Method enumerateInterface.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Interface> enumerateInterface() {
        return Collections.enumeration(this._interfaceList);
    }

    /**
     * Method enumerateNode.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Node> enumerateNode() {
        return Collections.enumeration(this._nodeList);
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (super.equals(obj)==false)
            return false;

        if (obj instanceof Outage) {

            Outage temp = (Outage)obj;
            if (this._interfaceList != null) {
                if (temp._interfaceList == null) return false;
                else if (!(this._interfaceList.equals(temp._interfaceList))) 
                    return false;
            }
            else if (temp._interfaceList != null)
                return false;
            if (this._nodeList != null) {
                if (temp._nodeList == null) return false;
                else if (!(this._nodeList.equals(temp._nodeList))) 
                    return false;
            }
            else if (temp._nodeList != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getInterface.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.poller.Interface at the given index
     */
    public Interface getInterface(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._interfaceList.size()) {
            throw new IndexOutOfBoundsException("getInterface: Index value '" + index + "' not in range [0.." + (this._interfaceList.size() - 1) + "]");
        }

        return _interfaceList.get(index);
    }

    /**
     * Method getInterface.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Interface[] getInterface() {
        Interface[] array = new Interface[0];
        return this._interfaceList.toArray(array);
    }

    /**
     * Method getInterfaceCollection.Returns a reference to
     * '_interfaceList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Interface> getInterfaceCollection() {
        return this._interfaceList;
    }

    /**
     * Method getInterfaceCount.
     * 
     * @return the size of this collection
     */
    public int getInterfaceCount() {
        return this._interfaceList.size();
    }

    /**
     * Method getNode.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the
     * org.opennms.netmgt.config.poller.Node at the given index
     */
    public Node getNode(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._nodeList.size()) {
            throw new IndexOutOfBoundsException("getNode: Index value '" + index + "' not in range [0.." + (this._nodeList.size() - 1) + "]");
        }

        return _nodeList.get(index);
    }

    /**
     * Method getNode.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are
     * changing in another thread, we pass a 0-length Array of the
     * correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    public Node[] getNode() {
        Node[] array = new Node[0];
        return this._nodeList.toArray(array);
    }

    /**
     * Method getNodeCollection.Returns a reference to '_nodeList'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Node> getNodeCollection() {
        return this._nodeList;
    }

    /**
     * Method getNodeCount.
     * 
     * @return the size of this collection
     */
    public int getNodeCount() {
        return this._nodeList.size();
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

        if (_interfaceList != null) {
            result = 37 * result + _interfaceList.hashCode();
        }
        if (_nodeList != null) {
            result = 37 * result + _nodeList.hashCode();
        }

        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    @Deprecated
    @Override
    public boolean isValid() {
        try {
            validate();
        } catch (ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateInterface.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Interface> iterateInterface() {
        return this._interfaceList.iterator();
    }

    /**
     * Method iterateNode.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Node> iterateNode() {
        return this._nodeList.iterator();
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    @Override
    public void marshal(final Writer out) throws MarshalException, ValidationException {
        Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    @Deprecated
    @Override
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     */
    public void removeAllInterface() {
        this._interfaceList.clear();
    }

    /**
     */
    public void removeAllNode() {
        this._nodeList.clear();
    }

    /**
     * Method removeInterface.
     * 
     * @param vInterface
     * @return true if the object was removed from the collection.
     */
    public boolean removeInterface(final Interface vInterface) {
        return _interfaceList.remove(vInterface);
    }

    /**
     * Method removeInterfaceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Interface removeInterfaceAt(final int index) {
        return this._interfaceList.remove(index);
    }

    /**
     * Method removeNode.
     * 
     * @param vNode
     * @return true if the object was removed from the collection.
     */
    public boolean removeNode(final Node vNode) {
        return _nodeList.remove(vNode);
    }

    /**
     * Method removeNodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Node removeNodeAt(final int index) {
        return this._nodeList.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param vInterface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setInterface(final int index, final Interface vInterface) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._interfaceList.size()) {
            throw new IndexOutOfBoundsException("setInterface: Index value '" + index + "' not in range [0.." + (this._interfaceList.size() - 1) + "]");
        }

        this._interfaceList.set(index, vInterface);
    }

    /**
     * 
     * 
     * @param vInterfaceArray
     */
    public void setInterface(final Interface[] vInterfaceArray) {
        //-- copy array
        _interfaceList.clear();

        for (int i = 0; i < vInterfaceArray.length; i++) {
            this._interfaceList.add(vInterfaceArray[i]);
        }
    }

    /**
     * Sets the value of '_interfaceList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param vInterfaceList the Vector to copy.
     */
    public void setInterface(final List<Interface> vInterfaceList) {
        // copy vector
        this._interfaceList.clear();

        this._interfaceList.addAll(vInterfaceList);
    }

    /**
     * Sets the value of '_interfaceList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param _interfaceList the Vector to set.
     */
    public void setInterfaceCollection(final List<Interface> _interfaceList) {
        this._interfaceList = _interfaceList;
    }

    /**
     * 
     * 
     * @param index
     * @param vNode
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setNode(final int index, final Node vNode) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._nodeList.size()) {
            throw new IndexOutOfBoundsException("setNode: Index value '" + index + "' not in range [0.." + (this._nodeList.size() - 1) + "]");
        }

        this._nodeList.set(index, vNode);
    }

    /**
     * 
     * 
     * @param vNodeArray
     */
    public void setNode(final Node[] vNodeArray) {
        //-- copy array
        _nodeList.clear();

        for (int i = 0; i < vNodeArray.length; i++) {
            this._nodeList.add(vNodeArray[i]);
        }
    }

    /**
     * Sets the value of '_nodeList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param vNodeList the Vector to copy.
     */
    public void setNode(final List<Node> vNodeList) {
        // copy vector
        this._nodeList.clear();

        this._nodeList.addAll(vNodeList);
    }

    /**
     * Sets the value of '_nodeList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param nodeList the Vector to set.
     */
    public void setNodeCollection(final List<Node> nodeList) {
        this._nodeList = nodeList;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.config.poller.BasicSchedule
     */
    @Deprecated
    public static BasicSchedule unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (BasicSchedule) Unmarshaller.unmarshal(Outage.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    @Override
    public void validate() throws ValidationException {
        new Validator().validate(this);
    }

}
