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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.xml.sax.ContentHandler;

/**
 * system definition
 */

@XmlRootElement(name="systemDef", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"name", "sysoid", "sysoidMask", "ipList", "collect"})
public class SystemDef implements Serializable {
    private static final long serialVersionUID = 656006979873221835L;

    /**
     * Field _name.
     */
    private String m_name;

    /**
     * Field _systemDefChoice.
     */
    private SystemDefChoice m_systemDefChoice = new SystemDefChoice();

    /**
     * list of IP address or IP address mask values to
     *  which this system definition applies.
     */
    private IpList m_ipList;

    /**
     * container for list of MIB groups to be collected
     *  for the system
     */
    private Collect m_collect;


    public SystemDef() {
        super();
    }

    public SystemDef(final String name) {
        super();
        m_name = name;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    @XmlAttribute(name="name", required=true)
    public String getName() {
        return m_name;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        m_name = name.intern();
    }

    /**
     * Returns the value of field 'systemDefChoice'.
     * 
     * @return the value of field 'SystemDefChoice'.
     */
    public SystemDefChoice getSystemDefChoice() {
        return m_systemDefChoice;
    }

    /**
     * Sets the value of field 'systemDefChoice'.
     * 
     * @param systemDefChoice the value of field 'systemDefChoice'.
     */
    public void setSystemDefChoice(final SystemDefChoice systemDefChoice) {
        m_systemDefChoice = systemDefChoice;
    }

    /* Make compatible with JAXB by proxying SystemDefChoice */
    @XmlElement(name="sysoid")
    public String getSysoid() {
        return m_systemDefChoice == null? null : m_systemDefChoice.getSysoid();
    }
    public void setSysoid(final String sysoid) {
        if (m_systemDefChoice == null) m_systemDefChoice = new SystemDefChoice();
        m_systemDefChoice.setSysoid(sysoid);
        m_systemDefChoice.setSysoidMask(null);
    }
    @XmlElement(name="sysoidMask")
    public String getSysoidMask() {
        return m_systemDefChoice == null? null : m_systemDefChoice.getSysoidMask();
    }
    public void setSysoidMask(final String sysoidMask) {
        if (m_systemDefChoice == null) m_systemDefChoice = new SystemDefChoice();
        m_systemDefChoice.setSysoid(null);
        m_systemDefChoice.setSysoidMask(sysoidMask);
    }
    
    /**
     * Returns the value of field 'ipList'. The field 'ipList' has
     * the following description: list of IP address or IP address
     * mask values to
     *  which this system definition applies.
     * 
     * @return the value of field 'IpList'.
     */
    @XmlElement(name="ipList")
    public IpList getIpList() {
        return m_ipList;
    }

    /**
     * Sets the value of field 'ipList'. The field 'ipList' has the
     * following description: list of IP address or IP address mask
     * values to
     *  which this system definition applies.
     * 
     * @param ipList the value of field 'ipList'.
     */
    public void setIpList(final IpList ipList) {
        m_ipList = ipList;
    }

    /**
     * Returns the value of field 'collect'. The field 'collect'
     * has the following description: container for list of MIB
     * groups to be collected
     *  for the system
     * 
     * @return the value of field 'Collect'.
     */
    @XmlElement(name="collect")
    public Collect getCollect() {
        return m_collect;
    }

    /**
     * Sets the value of field 'collect'. The field 'collect' has
     * the following description: container for list of MIB groups
     * to be collected
     *  for the system
     * 
     * @param collect the value of field 'collect'.
     */
    public void setCollect(final Collect collect) {
        m_collect = collect;
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
        
        if (obj instanceof SystemDef) {
        
            final SystemDef temp = (SystemDef)obj;
            if (m_name != null) {
                if (temp.m_name == null) return false;
                else if (!(m_name.equals(temp.m_name))) 
                    return false;
            }
            else if (temp.m_name != null)
                return false;
            if (m_systemDefChoice != null) {
                if (temp.m_systemDefChoice == null) return false;
                else if (!(m_systemDefChoice.equals(temp.m_systemDefChoice))) 
                    return false;
            }
            else if (temp.m_systemDefChoice != null)
                return false;
            if (m_ipList != null) {
                if (temp.m_ipList == null) return false;
                else if (!(m_ipList.equals(temp.m_ipList))) 
                    return false;
            }
            else if (temp.m_ipList != null)
                return false;
            if (m_collect != null) {
                if (temp.m_collect == null) return false;
                else if (!(m_collect.equals(temp.m_collect))) 
                    return false;
            }
            else if (temp.m_collect != null)
                return false;
            return true;
        }
        return false;
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

        if (m_name != null) {
           result = 37 * result + m_name.hashCode();
        }
        if (m_systemDefChoice != null) {
           result = 37 * result + m_systemDefChoice.hashCode();
        }
        if (m_ipList != null) {
           result = 37 * result + m_ipList.hashCode();
        }
        if (m_collect != null) {
           result = 37 * result + m_collect.hashCode();
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
     * Method unmarshal.
     * 
     * @param reader
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * SystemDef
     */
    @Deprecated
    public static SystemDef unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (SystemDef) Unmarshaller.unmarshal(SystemDef.class, reader);
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
