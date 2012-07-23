/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection.model;

/**
 * The Class MibObjDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class MibObjDTO implements java.io.Serializable {

    /** SNMP Object Identifier. */
    private String m_oid;

    /**
     * Instance identifier.
     */
    private String m_instance;

    /**
     * A human readable name for the object.
     */
    private String m_alias;

    /**
     * SNMP data type.
     */
    private String m_type;

    /**
     * Maximum Value.
     */
    private String m_maxval;

    /**
     * Minimum Value.
     */
    private String m_minval;


    /**
     * Instantiates a new MIB Object DTO.
     */
    public MibObjDTO() {
        super();
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return m_alias;
    }

    /**
     * Gets the single instance of MibObjDTO.
     *
     * @return single instance of MibObjDTO
     */
    public String getInstance() {
        return m_instance;
    }

    /**
     * Gets the maximum value.
     *
     * @return the maximum value
     */
    public String getMaxval() {
        return m_maxval;
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    public String getMinval() {
        return m_minval;
    }

    /**
     * Gets the OID.
     *
     * @return the OID
     */
    public String getOid() {
        return m_oid;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return m_type;
    }

    /**
     * Sets the alias.
     *
     * @param alias the new alias
     */
    public void setAlias(final String alias) {
        m_alias = alias == null ? null : alias.intern();
    }

    /**
     * Sets the instance.
     *
     * @param instance the new instance
     */
    public void setInstance(final String instance) {
        m_instance = instance == null ? null : instance.intern();
    }

    /**
     * Sets the maximum value.
     *
     * @param maxval the new maximum value
     */
    public void setMaxval(final String maxval) {
        m_maxval = maxval == null ? null : maxval.intern();
    }

    /**
     * Sets the minimum value.
     *
     * @param minval the new minimum value
     */
    public void setMinval(final String minval) {
        m_minval = minval == null ? null : minval.intern();
    }

    /**
     * Sets the OID.
     *
     * @param oid the new OID
     */
    public void setOid(final String oid) {
        m_oid = oid == null ? null : oid.intern();
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(final String type) {
        m_type = type == null ? null : type.intern();
    }
}
