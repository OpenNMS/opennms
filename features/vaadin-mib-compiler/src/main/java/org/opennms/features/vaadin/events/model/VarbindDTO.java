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
package org.opennms.features.vaadin.events.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class VarbindDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class VarbindDTO implements Serializable {
    
    /** The vbnumber. */
    private Integer vbnumber;
    
    /** The vbvalue list. */
    private List<String> vbvalueList;
    
    /**
     * Instantiates a new varbind DTO.
     */
    public VarbindDTO() {
        super();
        vbvalueList = new ArrayList<String>();
    }
    
    /**
     * Gets the vbnumber.
     *
     * @return the vbnumber
     */
    public Integer getVbnumber() {
        return vbnumber;
    }
    
    /**
     * Sets the vbnumber.
     *
     * @param vbnumber the new vbnumber
     */
    public void setVbnumber(Integer vbnumber) {
        this.vbnumber = vbnumber;
    }
    
    /**
     * Gets the vbvalue collection.
     *
     * @return the vbvalue collection
     */
    public List<String> getVbvalueCollection() {
        return vbvalueList;
    }
    
    /**
     * Sets the vbvalue collection.
     *
     * @param vbvalueList the new vbvalue collection
     */
    public void setVbvalueCollection(List<String> vbvalueList) {
        this.vbvalueList = vbvalueList;
    }
}
