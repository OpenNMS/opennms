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

@Entity
@Table(name = "snmp_collection_resource_types")
public class SnmpCollectionResourceType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "snmp_collection_resource_types_seq")
    @SequenceGenerator(
            name = "snmp_collection_resource_types_seq",
            sequenceName = "snmp_collection_resource_types_id_seq",
            allocationSize = 1
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_source_id", nullable = false)
    private SnmpCollectionSource collectionSource;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 256)
    private String label;

    @Column(name = "resource_label", length = 512)
    private String resourceLabel;

    @Column(name = "persistence_selector_strategy", length = 256)
    private String persistenceSelectorStrategy;

    @Column(name = "persistence_selector_params", columnDefinition = "text")
    private String persistenceSelectorParams;

    @Column(name = "storage_strategy", length = 256)
    private String storageStrategy;

    @Column(name = "storage_strategy_params", columnDefinition = "text")
    private String storageStrategyParams;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPersistenceSelectorParams() {
        return persistenceSelectorParams;
    }

    public void setPersistenceSelectorParams(String persistenceSelectorParams) {
        this.persistenceSelectorParams = persistenceSelectorParams;
    }

    public String getPersistenceSelectorStrategy() {
        return persistenceSelectorStrategy;
    }

    public void setPersistenceSelectorStrategy(String persistenceSelectorStrategy) {
        this.persistenceSelectorStrategy = persistenceSelectorStrategy;
    }

    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    public String getStorageStrategy() {
        return storageStrategy;
    }

    public void setStorageStrategy(String storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    public String getStorageStrategyParams() {
        return storageStrategyParams;
    }

    public void setStorageStrategyParams(String storageStrategyParams) {
        this.storageStrategyParams = storageStrategyParams;
    }

}
