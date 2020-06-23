/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
 * @version $Id: $
 */
@Entity
@Table(name="resourceReference")
public class ResourceReference implements Serializable {
    private static final long serialVersionUID = -8681671877772073153L;

    private Integer m_id;
    private String m_resourceId;
    
    /**
     * <p>Constructor for ResourceReference.</p>
     */
    public ResourceReference() {
    }
    
    /**
     * Unique identifier for resource reference.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(name="id")
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getResourceId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="resourceId")
    public String getResourceId() {
        return m_resourceId;
    }
    
    /**
     * <p>setResourceId</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     */
    public void setResourceId(String resourceId) {
        m_resourceId = resourceId;
    }

}
