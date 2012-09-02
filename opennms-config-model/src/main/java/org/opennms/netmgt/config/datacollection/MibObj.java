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

import java.io.Reader;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Validator;
import org.opennms.core.xml.ValidateUsing;

/**
 * a MIB object
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="mibObj", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder={"oid", "instance", "alias", "type", "maxval", "minval"})
@ValidateUsing("datacollection-config.xsd")
public class MibObj implements java.io.Serializable {
    private static final long serialVersionUID = -7831201614734695268L;

    /**
     * object identifier
     */
    private String m_oid;

    /**
     * instance identifier. Only valid instance identifier
     *  values are a positive integer value or the keyword
     * "ifIndex" which
     *  indicates that the ifIndex of the interface is to be
     * substituted for
     *  the instance value for each interface the oid is retrieved
     *  for.
     */
    private String m_instance;

    /**
     * a human readable name for the object (such as
     *  "ifOctetsIn"). NOTE: This value is used as the RRD file
     * name and
     *  data source name. RRD only supports data source names up to
     * 19 chars
     *  in length. If the SNMP data collector encounters an alias
     * which
     *  exceeds 19 characters it will be truncated.
     */
    private String m_alias;

    /**
     * SNMP data type SNMP supported types: counter, gauge,
     *  timeticks, integer, octetstring, string. The SNMP type is
     * mapped to
     *  one of two RRD supported data types COUNTER or GAUGE, or
     * the
     *  string.properties file. The mapping is as follows: SNMP
     * counter
     *  -> RRD COUNTER; SNMP gauge, timeticks, integer, octetstring
     * ->
     *  RRD GAUGE; SNMP string -> String properties file
     */
    private String m_type;

    /**
     * Maximum Value. In order to correctly manage counter
     *  wraps, it is possible to add a maximum value for a
     * collection. For
     *  example, a 32-bit counter would have a max value of
     *  4294967295.
     */
    private String m_maxval;

    /**
     * Minimum Value. For completeness, adding the ability
     *  to use a minimum value.
     */
    private String m_minval;


    public MibObj() {
        super();
    }
    
    public MibObj(final String oid, final String instance, final String alias, final String type) {
        super();
        m_oid = oid;
        m_instance = instance;
        m_alias = alias;
        m_type = type;
    }

    /**
     * Overrides the java.lang.Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override()
    public boolean equals(final java.lang.Object obj) {
        if ( this == obj )
            return true;
        
        if (obj instanceof MibObj) {
            final MibObj temp = (MibObj)obj;
            if (m_oid != null) {
                if (temp.m_oid == null) return false;
                else if (!(m_oid.equals(temp.m_oid))) 
                    return false;
            }
            else if (temp.m_oid != null)
                return false;
            if (m_instance != null) {
                if (temp.m_instance == null) return false;
                else if (!(m_instance.equals(temp.m_instance))) 
                    return false;
            }
            else if (temp.m_instance != null)
                return false;
            if (m_alias != null) {
                if (temp.m_alias == null) return false;
                else if (!(m_alias.equals(temp.m_alias))) 
                    return false;
            }
            else if (temp.m_alias != null)
                return false;
            if (m_type != null) {
                if (temp.m_type == null) return false;
                else if (!(m_type.equals(temp.m_type))) 
                    return false;
            }
            else if (temp.m_type != null)
                return false;
            if (m_maxval != null) {
                if (temp.m_maxval == null) return false;
                else if (!(m_maxval.equals(temp.m_maxval))) 
                    return false;
            }
            else if (temp.m_maxval != null)
                return false;
            if (m_minval != null) {
                if (temp.m_minval == null) return false;
                else if (!(m_minval.equals(temp.m_minval))) 
                    return false;
            }
            else if (temp.m_minval != null)
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
     *  in length. If the SNMP data collector encounters an alias
     * which
     *  exceeds 19 characters it will be truncated.
     * 
     * @return the value of field 'Alias'.
     */
    @XmlAttribute(name="alias", required=true)
    public String getAlias() {
        return m_alias;
    }

    /**
     * Returns the value of field 'instance'. The field 'instance'
     * has the following description: instance identifier. Only
     * valid instance identifier
     *  values are a positive integer value or the keyword
     * "ifIndex" which
     *  indicates that the ifIndex of the interface is to be
     * substituted for
     *  the instance value for each interface the oid is retrieved
     *  for.
     * 
     * @return the value of field 'Instance'.
     */
    @XmlAttribute(name="instance", required=true)
    public String getInstance() {
        return m_instance;
    }

    /**
     * Returns the value of field 'maxval'. The field 'maxval' has
     * the following description: Maximum Value. In order to
     * correctly manage counter
     *  wraps, it is possible to add a maximum value for a
     * collection. For
     *  example, a 32-bit counter would have a max value of
     *  4294967295.
     * 
     * @return the value of field 'Maxval'.
     */
    @XmlAttribute(name="maxval", required=false)
    public String getMaxval() {
        return m_maxval;
    }

    /**
     * Returns the value of field 'minval'. The field 'minval' has
     * the following description: Minimum Value. For completeness,
     * adding the ability
     *  to use a minimum value.
     * 
     * @return the value of field 'Minval'.
     */
    @XmlAttribute(name="minval", required=false)
    public String getMinval() {
        return m_minval;
    }

    /**
     * Returns the value of field 'oid'. The field 'oid' has the
     * following description: object identifier
     * 
     * @return the value of field 'Oid'.
     */
    @XmlAttribute(name="oid", required=true)
    public String getOid() {
        return m_oid;
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the
     * following description: SNMP data type SNMP supported types:
     * counter, gauge,
     *  timeticks, integer, octetstring, string. The SNMP type is
     * mapped to
     *  one of two RRD supported data types COUNTER or GAUGE, or
     * the
     *  string.properties file. The mapping is as follows: SNMP
     * counter
     *  -> RRD COUNTER; SNMP gauge, timeticks, integer, octetstring
     * ->
     *  RRD GAUGE; SNMP string -> String properties file
     * 
     * @return the value of field 'Type'.
     */
    @XmlAttribute(name="type", required=true)
    public String getType() {
        return m_type;
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
        
        if (m_oid != null) {
           result = 37 * result + m_oid.hashCode();
        }
        if (m_instance != null) {
           result = 37 * result + m_instance.hashCode();
        }
        if (m_alias != null) {
           result = 37 * result + m_alias.hashCode();
        }
        if (m_type != null) {
           result = 37 * result + m_type.hashCode();
        }
        if (m_maxval != null) {
           result = 37 * result + m_maxval.hashCode();
        }
        if (m_minval != null) {
           result = 37 * result + m_minval.hashCode();
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
     * 
     * 
     * @param out
     * @throws MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    @Deprecated
    public void marshal(final java.io.Writer out)
    throws MarshalException, ValidationException {
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
    public void marshal(final org.xml.sax.ContentHandler handler)
    throws java.io.IOException, MarshalException, ValidationException {
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
     *  in length. If the SNMP data collector encounters an alias
     * which
     *  exceeds 19 characters it will be truncated.
     * 
     * @param alias the value of field 'alias'.
     */
    public void setAlias(final String alias) {
        m_alias = alias.intern();
    }

    /**
     * Sets the value of field 'instance'. The field 'instance' has
     * the following description: instance identifier. Only valid
     * instance identifier
     *  values are a positive integer value or the keyword
     * "ifIndex" which
     *  indicates that the ifIndex of the interface is to be
     * substituted for
     *  the instance value for each interface the oid is retrieved
     *  for.
     * 
     * @param instance the value of field 'instance'.
     */
    public void setInstance(final String instance) {
        m_instance = instance.intern();
    }

    /**
     * Sets the value of field 'maxval'. The field 'maxval' has the
     * following description: Maximum Value. In order to correctly
     * manage counter
     *  wraps, it is possible to add a maximum value for a
     * collection. For
     *  example, a 32-bit counter would have a max value of
     *  4294967295.
     * 
     * @param maxval the value of field 'maxval'.
     */
    public void setMaxval(final String maxval) {
        m_maxval = maxval.intern();
    }

    /**
     * Sets the value of field 'minval'. The field 'minval' has the
     * following description: Minimum Value. For completeness,
     * adding the ability
     *  to use a minimum value.
     * 
     * @param minval the value of field 'minval'.
     */
    public void setMinval(final String minval) {
        m_minval = minval.intern();
    }

    /**
     * Sets the value of field 'oid'. The field 'oid' has the
     * following description: object identifier
     * 
     * @param oid the value of field 'oid'.
     */
    public void setOid(final String oid) {
        m_oid = oid.intern();
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the
     * following description: SNMP data type SNMP supported types:
     * counter, gauge,
     *  timeticks, integer, octetstring, string. The SNMP type is
     * mapped to
     *  one of two RRD supported data types COUNTER or GAUGE, or
     * the
     *  string.properties file. The mapping is as follows: SNMP
     * counter
     *  -> RRD COUNTER; SNMP gauge, timeticks, integer, octetstring
     * ->
     *  RRD GAUGE; SNMP string -> String properties file
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        m_type = type.intern();
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
     * MibObj
     */
    @Deprecated
    public static MibObj unmarshal(final Reader reader) throws MarshalException, ValidationException {
        return (MibObj)Unmarshaller.unmarshal(MibObj.class, reader);
    }

    /**
     * 
     * 
     * @throws ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws ValidationException {
        Validator validator = new Validator();
        validator.validate(this);
    }

}
