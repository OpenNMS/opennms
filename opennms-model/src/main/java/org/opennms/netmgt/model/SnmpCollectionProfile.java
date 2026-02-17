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
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(name = "snmp_collection_profiles", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class SnmpCollectionProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_collection_profiles_seq")
    @SequenceGenerator(
            name = "snmp_collection_profiles_seq",
            sequenceName = "snmp_collection_profiles_id_seq",
            allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false, length = 256, unique = true)
    private String name;

    @Column(name = "rrd_step", nullable = false)
    private Integer rrdStep;

    @Column(name = "rrd_rras", columnDefinition = "text")
    private String rrdRras;

    @Column(name = "storage_flag", length = 50, nullable = false)
    private String storageFlag = "select";

    @Column(name = "source_names", columnDefinition = "text")
    private String sourceNames;

    @Column(name = "max_vars_per_pdu")
    private Integer maxVarsPerPdu;

    @Column(nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @Column(name = "created_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "last_modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
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

    public Integer getMaxVarsPerPdu() {
        return maxVarsPerPdu;
    }

    public void setMaxVarsPerPdu(Integer maxVarsPerPdu) {
        this.maxVarsPerPdu = maxVarsPerPdu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRrdRras() {
        return rrdRras;
    }

    public void setRrdRras(String rrdRras) {
        this.rrdRras = rrdRras;
    }

    public Integer getRrdStep() {
        return rrdStep;
    }

    public void setRrdStep(Integer rrdStep) {
        this.rrdStep = rrdStep;
    }

    public String getSourceNames() {
        return sourceNames;
    }

    public void setSourceNames(String sourceNames) {
        this.sourceNames = sourceNames;
    }

    public String getStorageFlag() {
        return storageFlag;
    }

    public void setStorageFlag(String storageFlag) {
        this.storageFlag = storageFlag;
    }

}
