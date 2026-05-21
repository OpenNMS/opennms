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
import java.io.Serializable;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = "snmp_collection_systemdefs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"collection_source_id", "name"})
)
public class SnmpCollectionSystemDef  implements  Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_collection_systemdefs_seq")
    @SequenceGenerator(
            name = "snmp_collection_systemdefs_seq",
            sequenceName = "snmp_collection_systemdefs_id_seq",
            allocationSize = 1
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_source_id", nullable = false)
    private SnmpCollectionSource collectionSource;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(length = 256)
    private String sysoid;

    @Column(name = "sysoid_mask", length = 256)
    private String sysoidMask;

    @Column(name = "ip_addresses", columnDefinition = "text")
    private String ipAddresses;

    @Column(name = "ip_address_masks", columnDefinition = "text")
    private String ipAddressMasks;

    @Column(name = "mib_group_names", columnDefinition = "text", nullable = false)
    private String mibGroupNames;

    @Column(nullable = false)
    private Boolean enabled = Boolean.TRUE;

    public SnmpCollectionSource getCollectionSource() {
        return collectionSource;
    }

    public void setCollectionSource(SnmpCollectionSource collectionSource) {
        this.collectionSource = collectionSource;
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

    public String getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(String ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getIpAddressMasks() {
        return ipAddressMasks;
    }

    public void setIpAddressMasks(String ipAddressMasks) {
        this.ipAddressMasks = ipAddressMasks;
    }

    public String getMibGroupNames() {
        return mibGroupNames;
    }

    public void setMibGroupNames(String mibGroupNames) {
        this.mibGroupNames = mibGroupNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSysoid() {
        return sysoid;
    }

    public void setSysoid(String sysoid) {
        this.sysoid = sysoid;
    }

    public String getSysoidMask() {
        return sysoidMask;
    }

    public void setSysoidMask(String sysoidMask) {
        this.sysoidMask = sysoidMask;
    }
}
