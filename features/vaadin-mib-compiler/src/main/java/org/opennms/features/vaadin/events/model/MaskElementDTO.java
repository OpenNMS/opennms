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
 * The Class MaskElementDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class MaskElementDTO implements Serializable {
    
    /** The mename. */
    private String mename;
    
    /** The mevalue list. */
    private List<String> mevalueList;
    
    /**
     * Instantiates a new mask element DTO.
     */
    public MaskElementDTO() {
        super();
        mevalueList = new ArrayList<String>();
    }
    
    /**
     * Gets the mename.
     *
     * @return the mename
     */
    public String getMename() {
        return mename;
    }
    
    /**
     * Sets the mename.
     *
     * @param mename the new mename
     */
    public void setMename(String mename) {
        this.mename = mename;
    }
    
    /**
     * Gets the mevalue collection.
     *
     * @return the mevalue collection
     */
    public List<String> getMevalueCollection() {
        return mevalueList;
    }
    
    /**
     * Sets the mevalue collection.
     *
     * @param mevalueList the new mevalue collection
     */
    public void setMevalueCollection(List<String> mevalueList) {
        this.mevalueList = mevalueList;
    }
}
