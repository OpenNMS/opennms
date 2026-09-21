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


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "snmp_collection_sources", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class SnmpCollectionSource implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_collection_sources_seq")
    @SequenceGenerator(
            name = "snmp_collection_sources_seq",
            sequenceName = "snmp_collection_sources_id_seq",
            allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false, length = 256, unique = true)
    private String name;

    @Column(length = 128)
    private String vendor;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "created_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "last_modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @Column(name = "uploaded_by", length = 256)
    private String uploadedBy;

    @OneToMany(mappedBy = "collectionSource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnmpCollectionResourceType> resourceTypes;

    @OneToMany(mappedBy = "collectionSource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnmpCollectionMibGroup> mibGroups;

    @OneToMany(mappedBy = "collectionSource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SnmpCollectionSystemDef> systemDefs;



    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public List<SnmpCollectionMibGroup> getMibGroups() {
        return mibGroups;
    }

    public void setMibGroups(List<SnmpCollectionMibGroup> mibGroups) {
        this.mibGroups = mibGroups;
    }

    public List<SnmpCollectionResourceType> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<SnmpCollectionResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public List<SnmpCollectionSystemDef> getSystemDefs() {
        return systemDefs;
    }

    public void setSystemDefs(List<SnmpCollectionSystemDef> systemDefs) {
        this.systemDefs = systemDefs;
    }

}
