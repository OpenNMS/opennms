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
package org.opennms.features.vaadin.mibcompiler.model;

import java.io.Serializable;

/**
 * The Class LogMsgDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class LogMsgDTO implements Serializable {
    
    /** The content. */
    private String content;
    
    /** The dest. */
    private String dest;
    
    /** The notify. */
    private Boolean notify;
    
    /**
     * Instantiates a new log msg DTO.
     */
    public LogMsgDTO() {
        super();
    }
    
    /**
     * Gets the content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the content.
     *
     * @param content the new content
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Gets the dest.
     *
     * @return the dest
     */
    public String getDest() {
        return dest;
    }
    
    /**
     * Sets the dest.
     *
     * @param dest the new dest
     */
    public void setDest(String dest) {
        this.dest = dest;
    }
    
    /**
     * Gets the notify.
     *
     * @return the notify
     */
    public Boolean getNotify() {
        return notify == null ? Boolean.TRUE : notify;
    }
    
    /**
     * Sets the notify.
     *
     * @param notify the new notify
     */
    public void setNotify(Boolean notify) {
        this.notify = notify;
    }
}
