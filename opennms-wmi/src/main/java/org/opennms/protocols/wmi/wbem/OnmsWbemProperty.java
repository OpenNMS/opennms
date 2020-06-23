/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.wmi.wbem;

import org.opennms.protocols.wmi.WmiException;

/**
 * TODO implement CIMTYPE, VALUE
 *
 * @author ranger
 * @version $Id: $
 */
public interface OnmsWbemProperty {

    /**
     * <p>getWmiName</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiName() throws WmiException;

    /**
     * <p>getWmiOrigin</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiOrigin() throws WmiException;

    /**
     * <p>getWmiIsArray</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsArray() throws WmiException;

    /**
     * <p>getWmiIsLocal</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsLocal() throws WmiException;

    /**
     * <p>getWmiValue</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Object getWmiValue() throws WmiException;

    /**
     * <p>getWmiCIMType</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Integer getWmiCIMType() throws WmiException;
}
