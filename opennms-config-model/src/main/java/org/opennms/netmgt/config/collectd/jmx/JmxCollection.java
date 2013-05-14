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

package org.opennms.netmgt.config.collectd.jmx;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class JmxCollection.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="jmx-collection")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all") public class JmxCollection implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name.
     */
    @XmlAttribute(name="name", required=true)
    private java.lang.String _name;

    /**
     * DEPRECATED
     */
    @XmlTransient
    private int _maxVarsPerPdu = 0;

    /**
     * Field _rrd.
     */
    @XmlElement(name="rrd", required=true)
    private Rrd _rrd;

    /**
     * Field _mbeans.
     */
    @XmlElement(name="mbeans")
    private Mbeans _mbeans;

    /** Import Groups List. */
    @XmlElement(name="import-mbeans", required=false)
    private List<String> m_importMbeansList = new ArrayList<String>();


      //----------------/
     //- Constructors -/
    //----------------/

    public JmxCollection() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Gets the import MBeans list.
     *
     * @return the import MBeans list
     */
    @XmlTransient
    public List<String> getImportGroupsList() {
        return m_importMbeansList;
    }

    /**
     * Sets the import MBeans list.
     *
     * @param importMbeansList the new import MBeans list
     */
    public void setImportGroupsList(List<String> importMbeansList) {
        this.m_importMbeansList = importMbeansList;
    }

    /**
     * Checks for import MBeans.
     *
     * @return true, if successful
     */
    public boolean hasImportMbeans() {
        return m_importMbeansList != null && !m_importMbeansList.isEmpty();
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(
            final java.lang.Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof JmxCollection) {
        
            JmxCollection temp = (JmxCollection)obj;
            if (this._name != null) {
                if (temp._name == null) return false;
                else if (!(this._name.equals(temp._name))) 
                    return false;
            }
            else if (temp._name != null)
                return false;
            if (this._maxVarsPerPdu != temp._maxVarsPerPdu)
                return false;
            if (this._rrd != null) {
                if (temp._rrd == null) return false;
                else if (!(this._rrd.equals(temp._rrd))) 
                    return false;
            }
            else if (temp._rrd != null)
                return false;
            if (this._mbeans != null) {
                if (temp._mbeans == null) return false;
                else if (!(this._mbeans.equals(temp._mbeans))) 
                    return false;
            }
            else if (temp._mbeans != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'maxVarsPerPdu'. The field
     * 'maxVarsPerPdu' has the following description: DEPRECATED
     * 
     * @return the value of field 'MaxVarsPerPdu'.
     */
    public int getMaxVarsPerPdu(
    ) {
        return this._maxVarsPerPdu;
    }

    /**
     * Returns the value of field 'mbeans'.
     * 
     * @return the value of field 'Mbeans'.
     */
    public Mbeans getMbeans(
    ) {
        return this._mbeans;
    }

    /**
     * Returns the value of field 'name'.
     * 
     * @return the value of field 'Name'.
     */
    public java.lang.String getName(
    ) {
        return this._name;
    }

    /**
     * Returns the value of field 'rrd'.
     * 
     * @return the value of field 'Rrd'.
     */
    public Rrd getRrd(
    ) {
        return this._rrd;
    }

    /**
     * Method hasMaxVarsPerPdu.
     * 
     * @return true if at least one MaxVarsPerPdu has been added
     */
    public boolean hasMaxVarsPerPdu(
    ) {
        return this._maxVarsPerPdu != 0;
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
    public int hashCode(
    ) {
        int result = 17;
        
        long tmp;
        if (_name != null) {
           result = 37 * result + _name.hashCode();
        }
        result = 37 * result + _maxVarsPerPdu;
        if (_rrd != null) {
           result = 37 * result + _rrd.hashCode();
        }
        if (_mbeans != null) {
           result = 37 * result + _mbeans.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    @Deprecated
    public boolean isValid(
    ) {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
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
    public void marshal(
            final java.io.Writer out)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
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
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'maxVarsPerPdu'. The field
     * 'maxVarsPerPdu' has the following description: DEPRECATED
     * 
     * @param maxVarsPerPdu the value of field 'maxVarsPerPdu'.
     */
    public void setMaxVarsPerPdu(
            final int maxVarsPerPdu) {
        this._maxVarsPerPdu = maxVarsPerPdu;
    }

    /**
     * Sets the value of field 'mbeans'.
     * 
     * @param mbeans the value of field 'mbeans'.
     */
    public void setMbeans(
            final Mbeans mbeans) {
        this._mbeans = mbeans;
    }

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(
            final java.lang.String name) {
        this._name = name;
    }

    /**
     * Sets the value of field 'rrd'.
     * 
     * @param rrd the value of field 'rrd'.
     */
    public void setRrd(
            final Rrd rrd) {
        this._rrd = rrd;
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
     * org.opennms.netmgt.config.collectd.jmx.JmxCollection
     */
    @Deprecated
    public static JmxCollection unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (JmxCollection) Unmarshaller.unmarshal(JmxCollection.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
