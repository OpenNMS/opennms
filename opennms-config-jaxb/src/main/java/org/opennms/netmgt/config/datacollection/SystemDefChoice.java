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

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class SystemDefChoice.
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class SystemDefChoice implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * system object identifier (sysoid) which uniquely
     *  identifies the system.
     */
    private java.lang.String _sysoid;

    /**
     * Sysoid mask which can be used to match multiple
     *  systems if their sysoid begins with the mask
     */
    private java.lang.String _sysoidMask;


      //----------------/
     //- Constructors -/
    //----------------/

    public SystemDefChoice() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

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
        
        if (obj instanceof SystemDefChoice) {
        
            SystemDefChoice temp = (SystemDefChoice)obj;
            if (this._sysoid != null) {
                if (temp._sysoid == null) return false;
                else if (!(this._sysoid.equals(temp._sysoid))) 
                    return false;
            }
            else if (temp._sysoid != null)
                return false;
            if (this._sysoidMask != null) {
                if (temp._sysoidMask == null) return false;
                else if (!(this._sysoidMask.equals(temp._sysoidMask))) 
                    return false;
            }
            else if (temp._sysoidMask != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'sysoid'. The field 'sysoid' has
     * the following description: system object identifier (sysoid)
     * which uniquely
     *  identifies the system.
     * 
     * @return the value of field 'Sysoid'.
     */
    public java.lang.String getSysoid(
    ) {
        return this._sysoid;
    }

    /**
     * Returns the value of field 'sysoidMask'. The field
     * 'sysoidMask' has the following description: Sysoid mask
     * which can be used to match multiple
     *  systems if their sysoid begins with the mask
     * 
     * @return the value of field 'SysoidMask'.
     */
    public java.lang.String getSysoidMask(
    ) {
        return this._sysoidMask;
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
        if (_sysoid != null) {
           result = 37 * result + _sysoid.hashCode();
        }
        if (_sysoidMask != null) {
           result = 37 * result + _sysoidMask.hashCode();
        }
        
        return result;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
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
    public void marshal(
            final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'sysoid'. The field 'sysoid' has the
     * following description: system object identifier (sysoid)
     * which uniquely
     *  identifies the system.
     * 
     * @param sysoid the value of field 'sysoid'.
     */
    public void setSysoid(
            final java.lang.String sysoid) {
        this._sysoid = sysoid == null? null : sysoid.intern();
    }

    /**
     * Sets the value of field 'sysoidMask'. The field 'sysoidMask'
     * has the following description: Sysoid mask which can be used
     * to match multiple
     *  systems if their sysoid begins with the mask
     * 
     * @param sysoidMask the value of field 'sysoidMask'.
     */
    public void setSysoidMask(
            final java.lang.String sysoidMask) {
        this._sysoidMask = sysoidMask == null? null : sysoidMask.intern();
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
     * org.opennms.netmgt.config.datacollection.types.SystemDefChoic
     */
    public static org.opennms.netmgt.config.datacollection.SystemDefChoice unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.datacollection.SystemDefChoice) Unmarshaller.unmarshal(org.opennms.netmgt.config.datacollection.SystemDefChoice.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate(
    )
    throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
