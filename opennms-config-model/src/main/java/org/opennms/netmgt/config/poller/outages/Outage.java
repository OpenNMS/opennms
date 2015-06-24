/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.poller.outages;

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
    private static final long serialVersionUID = -2187904465632591493L;

    private static final Node[] EMPTY_NODE_LIST = new Node[0];
    private static final Interface[] EMPTY_INTERFACE_LIST = new Interface[0];

    /**
     * List of interfaces to which the outage
     *  applies.
     */
    @XmlElement(name="interface")
    private List<Interface> m_interfaces = new ArrayList<Interface>();

    /**
     * List of nodes to which the outage
     *  applies.
     */
    @XmlElement(name="node")
    private List<Node> m_nodes = new ArrayList<Node>();

    public Outage() {
        super();
    }

    /**
     * 
     * 
     * @param iface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInterface(final Interface iface) throws IndexOutOfBoundsException {
        m_interfaces.add(iface);
    }

    /**
     * 
     * 
     * @param index
     * @param iface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addInterface(final int index, final Interface iface) throws IndexOutOfBoundsException {
        m_interfaces.add(index, iface);
    }

    /**
     * 
     * 
     * @param node
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNode(final Node node) throws IndexOutOfBoundsException {
        m_nodes.add(node);
    }

    /**
     * 
     * 
     * @param index
     * @param node
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addNode(final int index, final Node node) throws IndexOutOfBoundsException {
        m_nodes.add(index, node);
    }

    /**
     * Method enumerateInterface.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Interface> enumerateInterface() {
        return Collections.enumeration(m_interfaces);
    }

    /**
     * Method enumerateNode.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<Node> enumerateNode() {
        return Collections.enumeration(m_nodes);
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
        return m_interfaces.get(index);
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
        return m_interfaces.toArray(EMPTY_INTERFACE_LIST);
    }

    /**
     * Method getInterfaceCollection.Returns a reference to
     * 'm_interfaces'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Interface> getInterfaceCollection() {
        return new ArrayList<Interface>(m_interfaces);
    }

    /**
     * Method getInterfaceCount.
     * 
     * @return the size of this collection
     */
    public int getInterfaceCount() {
        return m_interfaces.size();
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
        return m_nodes.get(index);
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
        return m_nodes.toArray(EMPTY_NODE_LIST);
    }

    /**
     * Method getNodeCollection.Returns a reference to 'm_nodes'.
     * No type checking is performed on any modifications to the
     * Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<Node> getNodeCollection() {
        return new ArrayList<Node>(m_nodes);
    }

    /**
     * Method getNodeCount.
     * 
     * @return the size of this collection
     */
    public int getNodeCount() {
        return m_nodes.size();
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
        } catch (final ValidationException vex) {
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
        return m_interfaces.iterator();
    }

    /**
     * Method iterateNode.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<Node> iterateNode() {
        return m_nodes.iterator();
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
        m_interfaces.clear();
    }

    /**
     */
    public void removeAllNode() {
        m_nodes.clear();
    }

    /**
     * Method removeInterface.
     * 
     * @param iface
     * @return true if the object was removed from the collection.
     */
    public boolean removeInterface(final Interface iface) {
        return m_interfaces.remove(iface);
    }

    /**
     * Method removeInterfaceAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Interface removeInterfaceAt(final int index) {
        return m_interfaces.remove(index);
    }

    /**
     * Method removeNode.
     * 
     * @param node
     * @return true if the object was removed from the collection.
     */
    public boolean removeNode(final Node node) {
        return m_nodes.remove(node);
    }

    /**
     * Method removeNodeAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public Node removeNodeAt(final int index) {
        return m_nodes.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param iface
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setInterface(final int index, final Interface iface) throws IndexOutOfBoundsException {
        m_interfaces.set(index, iface);
    }

    /**
     * 
     * 
     * @param interfaces
     */
    public void setInterface(final Interface[] interfaces) {
        m_interfaces.clear();
        for (final Interface iface : interfaces) {
            m_interfaces.add(iface);
        }
    }

    /**
     * Sets the value of 'm_interfaces' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param interfaces the Vector to copy.
     */
    public void setInterface(final List<Interface> interfaces) {
        if (interfaces != m_interfaces) {
            m_interfaces.clear();
            m_interfaces.addAll(interfaces);
        }
    }

    /**
     * Sets the value of 'm_interfaces' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param m_interfaces the Vector to set.
     */
    public void setInterfaceCollection(final List<Interface> interfaces) {
        m_interfaces = new ArrayList<Interface>(interfaces);
    }

    /**
     * 
     * 
     * @param index
     * @param nodes
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setNode(final int index, final Node nodes) throws IndexOutOfBoundsException {
        m_nodes.set(index, nodes);
    }

    /**
     * 
     * 
     * @param nodes
     */
    public void setNode(final Node[] nodes) {
        m_nodes.clear();
        for (final Node node : nodes) {
            m_nodes.add(node);
        }
    }

    /**
     * Sets the value of 'm_nodes' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param nodes the Vector to copy.
     */
    public void setNode(final List<Node> nodes) {
        if (nodes != m_nodes) {
            m_nodes.clear();
            m_nodes.addAll(nodes);
        }
    }

    /**
     * Sets the value of 'm_nodes' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param nodes the Vector to set.
     */
    public void setNodeCollection(final List<Node> nodes) {
        m_nodes = new ArrayList<Node>(nodes);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_interfaces == null) ? 0 : m_interfaces.hashCode());
        result = prime * result + ((m_nodes == null) ? 0 : m_nodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Outage)) {
            return false;
        }
        final Outage other = (Outage) obj;
        if (m_interfaces == null) {
            if (other.m_interfaces != null) {
                return false;
            }
        } else if (!m_interfaces.equals(other.m_interfaces)) {
            return false;
        }
        if (m_nodes == null) {
            if (other.m_nodes != null) {
                return false;
            }
        } else if (!m_nodes.equals(other.m_nodes)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Outage[name=" + getName() +
                ",type=" + getType() +
                ",times=" + getTime() +
                ",interfaces=" + m_interfaces +
                ",nodes=" + m_nodes + "]";
    }
}
