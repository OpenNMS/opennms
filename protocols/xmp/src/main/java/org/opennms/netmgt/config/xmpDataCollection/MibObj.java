/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.xmpDataCollection;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * a MIB object
 * 
 * @version $Revision$ $Date$
 */

@SuppressWarnings("all") public class MibObj implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * MIB name
     */
    private java.lang.String _mib;

    /**
     * MIB table name or empty string if scalar
     */
    private java.lang.String _table;

    /**
     * Variable name
     */
    private java.lang.String _var;

    /**
     * Instance identifier or empty string. If not
     *  empty, this string will be used for table queries.
     */
    private java.lang.String _instance;

    /**
     * a human readable name for the object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file
     * name and
     *  data source name. RRD only supports data source names up to
     * 19 chars
     *  in length. If the XMP data collector encounters an alias
     * which
     *  exceeds 19 characters it will be truncated.
     */
    private java.lang.String _alias;


      //----------------/
     //- Constructors -/
    //----------------/

    public MibObj() {
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
        
        if (obj instanceof MibObj) {
        
            MibObj temp = (MibObj)obj;
            if (this._mib != null) {
                if (temp._mib == null) return false;
                else if (!(this._mib.equals(temp._mib))) 
                    return false;
            }
            else if (temp._mib != null)
                return false;
            if (this._table != null) {
                if (temp._table == null) return false;
                else if (!(this._table.equals(temp._table))) 
                    return false;
            }
            else if (temp._table != null)
                return false;
            if (this._var != null) {
                if (temp._var == null) return false;
                else if (!(this._var.equals(temp._var))) 
                    return false;
            }
            else if (temp._var != null)
                return false;
            if (this._instance != null) {
                if (temp._instance == null) return false;
                else if (!(this._instance.equals(temp._instance))) 
                    return false;
            }
            else if (temp._instance != null)
                return false;
            if (this._alias != null) {
                if (temp._alias == null) return false;
                else if (!(this._alias.equals(temp._alias))) 
                    return false;
            }
            else if (temp._alias != null)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of field 'alias'. The field 'alias' has
     * the following description: a human readable name for the
     * object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file
     * name and
     *  data source name. RRD only supports data source names up to
     * 19 chars
     *  in length. If the XMP data collector encounters an alias
     * which
     *  exceeds 19 characters it will be truncated.
     * 
     * @return the value of field 'Alias'.
     */
    public java.lang.String getAlias(
    ) {
        return this._alias;
    }

    /**
     * Returns the value of field 'instance'. The field 'instance'
     * has the following description: Instance identifier or empty
     * string. If not
     *  empty, this string will be used for table queries.
     * 
     * @return the value of field 'Instance'.
     */
    public java.lang.String getInstance(
    ) {
        return this._instance;
    }

    /**
     * Returns the value of field 'mib'. The field 'mib' has the
     * following description: MIB name
     * 
     * @return the value of field 'Mib'.
     */
    public java.lang.String getMib(
    ) {
        return this._mib;
    }

    /**
     * Returns the value of field 'table'. The field 'table' has
     * the following description: MIB table name or empty string if
     * scalar
     * 
     * @return the value of field 'Table'.
     */
    public java.lang.String getTable(
    ) {
        return this._table;
    }

    /**
     * Returns the value of field 'var'. The field 'var' has the
     * following description: Variable name
     * 
     * @return the value of field 'Var'.
     */
    public java.lang.String getVar(
    ) {
        return this._var;
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
        if (_mib != null) {
           result = 37 * result + _mib.hashCode();
        }
        if (_table != null) {
           result = 37 * result + _table.hashCode();
        }
        if (_var != null) {
           result = 37 * result + _var.hashCode();
        }
        if (_instance != null) {
           result = 37 * result + _instance.hashCode();
        }
        if (_alias != null) {
           result = 37 * result + _alias.hashCode();
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
     * Sets the value of field 'alias'. The field 'alias' has the
     * following description: a human readable name for the object
     * (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file
     * name and
     *  data source name. RRD only supports data source names up to
     * 19 chars
     *  in length. If the XMP data collector encounters an alias
     * which
     *  exceeds 19 characters it will be truncated.
     * 
     * @param alias the value of field 'alias'.
     */
    public void setAlias(
            final java.lang.String alias) {
        this._alias = alias;
    }

    /**
     * Sets the value of field 'instance'. The field 'instance' has
     * the following description: Instance identifier or empty
     * string. If not
     *  empty, this string will be used for table queries.
     * 
     * @param instance the value of field 'instance'.
     */
    public void setInstance(
            final java.lang.String instance) {
        this._instance = instance;
    }

    /**
     * Sets the value of field 'mib'. The field 'mib' has the
     * following description: MIB name
     * 
     * @param mib the value of field 'mib'.
     */
    public void setMib(
            final java.lang.String mib) {
        this._mib = mib;
    }

    /**
     * Sets the value of field 'table'. The field 'table' has the
     * following description: MIB table name or empty string if
     * scalar
     * 
     * @param table the value of field 'table'.
     */
    public void setTable(
            final java.lang.String table) {
        this._table = table;
    }

    /**
     * Sets the value of field 'var'. The field 'var' has the
     * following description: Variable name
     * 
     * @param var the value of field 'var'.
     */
    public void setVar(
            final java.lang.String var) {
        this._var = var;
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
     * org.opennms.netmgt.config.xmpDataCollection.MibObj
     */
    public static org.opennms.netmgt.config.xmpDataCollection.MibObj unmarshal(
            final java.io.Reader reader)
    throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.opennms.netmgt.config.xmpDataCollection.MibObj) Unmarshaller.unmarshal(org.opennms.netmgt.config.xmpDataCollection.MibObj.class, reader);
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
