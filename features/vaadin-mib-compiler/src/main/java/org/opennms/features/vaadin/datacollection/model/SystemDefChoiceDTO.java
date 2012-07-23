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
 * The System Definition Choice DTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class SystemDefChoiceDTO implements Serializable {

    /**
     * system object identifier (sysoid) which uniquely identifies the system.
     */
    private String _sysoid;

    /** Sysoid mask which can be used to match multiple systems if their sysoid begins with the mask. */
    private String _sysoidMask;

    /**
     * Instantiates a new System Definition Choice DTO.
     */
    public SystemDefChoiceDTO() {
        super();
    }

    /**
     * Gets the sysoid.
     *
     * @return the sysoid
     */
    public String getSysoid() {
        return this._sysoid;
    }


    /**
     * Gets the sysoid mask.
     *
     * @return the sysoid mask
     */
    public String getSysoidMask() {
        return this._sysoidMask;
    }

    /**
     * Sets the sysoid.
     *
     * @param sysoid the new sysoid
     */
    public void setSysoid(final String sysoid) {
        this._sysoid = sysoid == null ? null : sysoid.intern();
    }

    /**
     * Sets the sysoid mask.
     *
     * @param sysoidMask the new sysoid mask
     */
    public void setSysoidMask(final String sysoidMask) {
        this._sysoidMask = sysoidMask == null ? null : sysoidMask.intern();
    }

}
