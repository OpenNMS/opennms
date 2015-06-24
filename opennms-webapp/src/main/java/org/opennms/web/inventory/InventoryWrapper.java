/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.inventory;

import java.util.Date;

/**
 * <p>InventoryWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class InventoryWrapper {
    
    private String version;
    private Date date;
    private String group;
    private String urlViewVC;
    
    
    /**
     * <p>Constructor for InventoryWrapper.</p>
     *
     * @param version a {@link java.lang.String} object.
     * @param date a java$util$Date object.
     * @param group a {@link java.lang.String} object.
     * @param urlViewVC a {@link java.lang.String} object.
     */
    public InventoryWrapper(String version, Date date, String group, String urlViewVC){
        this.version = version;
        this.date = date;
        this.group = group;
        this.urlViewVC = urlViewVC;
    }

    /**
     * <p>Getter for the field <code>version</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVersion(){
        return version;
    }
    /**
     * <p>Getter for the field <code>date</code>.</p>
     *
     * @return a java$util$Date object.
     */
    public Date getDate() {
        return date;
    }
    /**
     * <p>Getter for the field <code>group</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroup(){
        return group;
    }
    /**
     * <p>Getter for the field <code>urlViewVC</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUrlViewVC(){
        return urlViewVC;
    }
}
