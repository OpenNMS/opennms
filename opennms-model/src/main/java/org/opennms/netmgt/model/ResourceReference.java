/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
