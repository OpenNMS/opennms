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
 * list of IP address or IP address mask values to which
 *  this system definition applies.
 */

@XmlRootElement(name="ipList", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("datacollection-config.xsd")
public class IpList implements Serializable {
    private static final long serialVersionUID = -5925958500985291547L;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * List of IP addresses
     */
    private List<String> m_ipAddresses = new ArrayList<String>();

    /**
     * List of IP address masks
     */
    private List<String> m_ipAddressMasks = new ArrayList<String>();


    /**
     * 
     * 
     * @param ipAddr
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpAddr(final String ipAddr) throws IndexOutOfBoundsException {
        m_ipAddresses.add(ipAddr.intern());
    }

    /**
     * 
     * 
     * @param index
     * @param ipAddr
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpAddr(final int index, final String ipAddr) throws IndexOutOfBoundsException {
        m_ipAddresses.add(index, ipAddr.intern());
    }

    /**
     * 
     * 
     * @param ipAddrMask
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpAddrMask(final String ipAddrMask) throws IndexOutOfBoundsException {
        m_ipAddressMasks.add(ipAddrMask.intern());
    }

    /**
     * 
     * 
     * @param index
     * @param ipAddrMask
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void addIpAddrMask(final int index, final String ipAddrMask) throws IndexOutOfBoundsException {
        m_ipAddressMasks.add(index, ipAddrMask.intern());
    }

    /**
     * Method enumerateIpAddr.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateIpAddr() {
        return Collections.enumeration(m_ipAddresses);
    }

    /**
     * Method enumerateIpAddrMask.
     * 
     * @return an Enumeration over all possible elements of this
     * collection
     */
    public Enumeration<String> enumerateIpAddrMask() {
        return Collections.enumeration(m_ipAddressMasks);
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
        
        if (obj instanceof IpList) {
        
            final IpList temp = (IpList)obj;
            if (m_ipAddresses != null) {
                if (temp.m_ipAddresses == null) return false;
                else if (!(m_ipAddresses.equals(temp.m_ipAddresses))) 
                    return false;
            }
            else if (temp.m_ipAddresses != null)
                return false;
            if (m_ipAddressMasks != null) {
                if (temp.m_ipAddressMasks == null) return false;
                else if (!(m_ipAddressMasks.equals(temp.m_ipAddressMasks))) 
                    return false;
            }
            else if (temp.m_ipAddressMasks != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Method getIpAddr.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIpAddr(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_ipAddresses.size()) {
            throw new IndexOutOfBoundsException("getIpAddr: Index value '" + index + "' not in range [0.." + (m_ipAddresses.size() - 1) + "]");
        }
        return m_ipAddresses.get(index);
    }

    /**
     * Method getIpAddr.Returns the contents of the collection in
     * an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="ipAddr")
    public String[] getIpAddr() {
        return m_ipAddresses.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Method getIpAddrCollection.Returns a reference to
     * '_ipAddrList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIpAddrCollection() {
        return m_ipAddresses;
    }

    /**
     * Method getIpAddrCount.
     * 
     * @return the size of this collection
     */
    public int getIpAddrCount() {
        return m_ipAddresses.size();
    }

    /**
     * Method getIpAddrMask.
     * 
     * @param index
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     * @return the value of the String at the given index
     */
    public String getIpAddrMask(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_ipAddressMasks.size()) {
            throw new IndexOutOfBoundsException("getIpAddrMask: Index value '" + index + "' not in range [0.." + (m_ipAddressMasks.size() - 1) + "]");
        }
        return m_ipAddressMasks.get(index);
    }

    /**
     * Method getIpAddrMask.Returns the contents of the collection
     * in an Array.  <p>Note:  Just in case the collection contents
     * are changing in another thread, we pass a 0-length Array of
     * the correct type into the API call.  This way we <i>know</i>
     * that the Array returned is of exactly the correct length.
     * 
     * @return this collection as an Array
     */
    @XmlElement(name="ipAddrMask")
    public String[] getIpAddrMask() {
        return m_ipAddressMasks.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Method getIpAddrMaskCollection.Returns a reference to
     * '_ipAddrMaskList'. No type checking is performed on any
     * modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<String> getIpAddrMaskCollection() {
        return m_ipAddressMasks;
    }

    /**
     * Method getIpAddrMaskCount.
     * 
     * @return the size of this collection
     */
    public int getIpAddrMaskCount() {
        return m_ipAddressMasks.size();
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
        
        if (m_ipAddresses != null) {
           result = 37 * result + m_ipAddresses.hashCode();
        }
        if (m_ipAddressMasks != null) {
           result = 37 * result + m_ipAddressMasks.hashCode();
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
        } catch (ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * Method iterateIpAddr.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateIpAddr() {
        return m_ipAddresses.iterator();
    }

    /**
     * Method iterateIpAddrMask.
     * 
     * @return an Iterator over all possible elements in this
     * collection
     */
    public Iterator<String> iterateIpAddrMask() {
        return m_ipAddressMasks.iterator();
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
    public void marshal(final ContentHandler handler) throws IOException, MarshalException, ValidationException {
        Marshaller.marshal(this, handler);
    }

    public void removeAllIpAddr() {
        m_ipAddresses.clear();
    }

    public void removeAllIpAddrMask() {
        m_ipAddressMasks.clear();
    }

    /**
     * Method removeIpAddr.
     * 
     * @param ipAddr
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpAddr(final String ipAddr) {
        return m_ipAddresses.remove(ipAddr);
    }

    /**
     * Method removeIpAddrAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIpAddrAt(final int index) {
        return m_ipAddresses.remove(index);
    }

    /**
     * Method removeIpAddrMask.
     * 
     * @param ipAddrMask
     * @return true if the object was removed from the collection.
     */
    public boolean removeIpAddrMask(final String ipAddrMask) {
        return m_ipAddressMasks.remove(ipAddrMask);
    }

    /**
     * Method removeIpAddrMaskAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public String removeIpAddrMaskAt(final int index) {
        return m_ipAddressMasks.remove(index);
    }

    /**
     * 
     * 
     * @param index
     * @param ipAddr
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIpAddr(final int index, final String ipAddr) throws IndexOutOfBoundsException {
        if (index < 0 || index >= m_ipAddresses.size()) {
            throw new IndexOutOfBoundsException("setIpAddr: Index value '" + index + "' not in range [0.." + (m_ipAddresses.size() - 1) + "]");
        }
        m_ipAddresses.set(index, ipAddr.intern());
    }

    /**
     * 
     * 
     * @param ipAddrs
     */
    public void setIpAddr(final String[] ipAddrs) {
        m_ipAddresses.clear();
        
        for (int i = 0; i < ipAddrs.length; i++) {
                m_ipAddresses.add(ipAddrs[i].intern());
        }
    }

    /**
     * Sets the value of '_ipAddrList' by copying the given Vector.
     * All elements will be checked for type safety.
     * 
     * @param ipAddrs the Vector to copy.
     */
    public void setIpAddr(final List<String> ipAddrs) {
        m_ipAddresses.clear();
        for (final String ipAddr : ipAddrs) {
            m_ipAddresses.add(ipAddr.intern());
        }
    }

    /**
     * Sets the value of '_ipAddrList' by setting it to the given
     * Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ipAddrs the Vector to set.
     */
    public void setIpAddrCollection(final List<String> ipAddrs) {
        m_ipAddresses = new ArrayList<String>();
        for (final String ipAddr : ipAddrs) {
            m_ipAddresses.add(ipAddr.intern());
        }
    }

    /**
     * @param index
     * @param ipAddrMask
     * @throws java.lang.IndexOutOfBoundsException if the index
     * given is outside the bounds of the collection
     */
    public void setIpAddrMask(final int index, final String ipAddrMask) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= m_ipAddressMasks.size()) {
            throw new IndexOutOfBoundsException("setIpAddrMask: Index value '" + index + "' not in range [0.." + (m_ipAddressMasks.size() - 1) + "]");
        }
        
        m_ipAddressMasks.set(index, ipAddrMask.intern());
    }

    /**
     * 
     * 
     * @param ipAddrMasks
     */
    public void setIpAddrMask(final String[] ipAddrMasks) {
        m_ipAddressMasks.clear();
        for (int i = 0; i < ipAddrMasks.length; i++) {
                m_ipAddressMasks.add(ipAddrMasks[i].intern());
        }
    }

    /**
     * Sets the value of '_ipAddrMaskList' by copying the given
     * Vector. All elements will be checked for type safety.
     * 
     * @param ipAddrMasks the Vector to copy.
     */
    public void setIpAddrMask(final List<String> ipAddrMasks) {
        m_ipAddressMasks.clear();
        for (final String ipAddrMask : ipAddrMasks) {
            m_ipAddressMasks.add(ipAddrMask.intern());
        }
    }

    /**
     * Sets the value of '_ipAddrMaskList' by setting it to the
     * given Vector. No type checking is performed.
     * @deprecated
     * 
     * @param ipAddrMasks the Vector to set.
     */
    public void setIpAddrMaskCollection(final List<String> ipAddrMasks) {
        m_ipAddressMasks = new ArrayList<String>();
        for (final String ipAddrMask : ipAddrMasks) {
            m_ipAddressMasks.add(ipAddrMask.intern());
        }
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
     * org.opennms.netmgt.config.datacollection.types.IpList
     */
    @Deprecated
    public static IpList unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (IpList)Unmarshaller.unmarshal(IpList.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate() throws ValidationException {
        final Validator validator = new Validator();
        validator.validate(this);
    }

}
