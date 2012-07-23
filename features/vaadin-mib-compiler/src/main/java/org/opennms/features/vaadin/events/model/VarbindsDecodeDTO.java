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
 * The Class VarbindsDecodeDTO.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class VarbindsDecodeDTO implements Serializable {
    
    /** The parmid. */
    private String parmid;
    
    /** The decode list. */
    private List<DecodeDTO> decodeList;
    
    /**
     * Instantiates a new varbinds decode DTO.
     */
    public VarbindsDecodeDTO() {
        super();
        decodeList = new ArrayList<DecodeDTO>();
    }
    
    /**
     * Gets the parmid.
     *
     * @return the parmid
     */
    public String getParmid() {
        return parmid;
    }
    
    /**
     * Sets the parmid.
     *
     * @param parmid the new parmid
     */
    public void setParmid(String parmid) {
        this.parmid = parmid;
    }
    
    /**
     * Gets the decode collection.
     *
     * @return the decode collection
     */
    public List<DecodeDTO> getDecodeCollection() {
        return decodeList;
    }
    
    /**
     * Sets the decode collection.
     *
     * @param decodeList the new decode collection
     */
    public void setDecodeCollection(List<DecodeDTO> decodeList) {
        this.decodeList = decodeList;
    }
}
