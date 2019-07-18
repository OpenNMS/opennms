/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.google.common.base.MoreObjects;


/**
 * EffectiveConfiguration is a JSON document persisted in the relational DB.
 * It is intended to be the merge of any configuration on the file system or made available by plugins.
 * It is controlled by Opennms and available to Sentinels.
 * By convention, the document key is the filename of where the configuration was initially stored.
 * Client can poll the lastUpdated value to determin if configuration has change. 
 *
 */
@Entity
@Table(name = "effective_configuration")
@TypeDef(name = "jsonb", typeClass = OnmsJsonbType.class)
public class EffectiveConfiguration implements Serializable {
    
    private static final long serialVersionUID = -2278390290606190530L;

    private Integer id;

    private String key;
    
    private int hashCode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String configuration;
    
    private Date lastUpdated;
    
    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return id;
    }
        
    public void setId(Integer id) {
        this.id = id;
    }

    @Type(type = "text")
    @Column(name = "key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Type(type = "jsonb")
    @Column(name = "document", columnDefinition = "jsonb")
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Column(name = "hash_code", nullable = false)
    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", getId())
            .add("key", key)
            .add("configuration", configuration)
            .add("lastUpdated",lastUpdated)
            .toString();
    }
 
}
