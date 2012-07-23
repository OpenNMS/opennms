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

import java.io.Serializable;

/**
 * The System Definition.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefDTO implements Serializable {

    /**
     * Field name.
     */
    private String m_name;

    /**
     * Field systemDefChoice.
     */
    private SystemDefChoiceDTO m_systemDefChoice;

    /**
     * List of IP address or IP address mask values to which this system definition applies.
     */
    private IpListDTO m_ipList;

    /** Container for list of MIB groups to be collected for the system. */
    private CollectDTO m_collect;


    /**
     * Instantiates a new System Definition DTO.
     */
    public SystemDefDTO() {
        super();
        m_systemDefChoice = new SystemDefChoiceDTO();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(final String name) {
        m_name = name == null ? null : name.intern();
    }

    /**
     * Gets the system definition choice.
     *
     * @return the system definition choice
     */
    public SystemDefChoiceDTO getSystemDefChoice() {
        return m_systemDefChoice;
    }

    /**
     * Sets the system definition choice.
     *
     * @param systemDefChoice the new system definition choice
     */
    public void setSystemDefChoice(final SystemDefChoiceDTO systemDefChoice) {
        m_systemDefChoice = systemDefChoice;
    }

    /**
     * Gets the sysoid.
     *
     * @return the sysoid
     */
    public String getSysoid() {
        return m_systemDefChoice == null ? null : m_systemDefChoice.getSysoid();
    }

    /**
     * Sets the sysoid.
     *
     * @param sysoid the new sysoid
     */
    public void setSysoid(final String sysoid) {
        if (m_systemDefChoice == null) m_systemDefChoice = new SystemDefChoiceDTO();
        m_systemDefChoice.setSysoid(sysoid);
        m_systemDefChoice.setSysoidMask(null);
    }

    /**
     * Gets the sysoid mask.
     *
     * @return the sysoid mask
     */
    public String getSysoidMask() {
        return m_systemDefChoice == null ? null : m_systemDefChoice.getSysoidMask();
    }

    /**
     * Sets the sysoid mask.
     *
     * @param sysoidMask the new sysoid mask
     */
    public void setSysoidMask(final String sysoidMask) {
        if (m_systemDefChoice == null) m_systemDefChoice = new SystemDefChoiceDTO();
        m_systemDefChoice.setSysoid(null);
        m_systemDefChoice.setSysoidMask(sysoidMask);
    }

    /**
     * Gets the IP list.
     *
     * @return the IP list
     */
    public IpListDTO getIpList() {
        return m_ipList;
    }

    /**
     * Sets the IP list.
     *
     * @param ipList the new IP list
     */
    public void setIpList(final IpListDTO ipList) {
        m_ipList = ipList;
    }

    /**
     * Gets the collect.
     *
     * @return the collect
     */
    public CollectDTO getCollect() {
        return m_collect;
    }

    /**
     * Sets the collect.
     *
     * @param collect the new collect
     */
    public void setCollect(final CollectDTO collect) {
        m_collect = collect;
    }

}
