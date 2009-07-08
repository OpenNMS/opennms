/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Model class for a resource reference.  This maps a unique
 * string resourceID to a unique integer to minimize costs of
 * storing repeated resourceID strings in the database.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see OnmsResource#getId()
 */
@Entity
@Table(name="resourceReference")
public class ResourceReference implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Integer m_id;
    private String m_resourceId;
    
    public ResourceReference() {
    }
    
    /**
     * Unique identifier for resource reference.
     * 
     */
    @Id
    @Column(name="id")
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    public void setId(Integer id) {
        m_id = id;
    }

    @Column(name="resourceId")
    public String getResourceId() {
        return m_resourceId;
    }
    
    public void setResourceId(String resourceId) {
        m_resourceId = resourceId;
    }

}
