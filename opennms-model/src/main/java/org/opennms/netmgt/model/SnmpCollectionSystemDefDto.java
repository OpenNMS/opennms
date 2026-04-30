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

import java.util.List;

/**
 * Wire-format DTO for {@link SnmpCollectionSystemDef}.
 *
 * <p>{@code ipAddresses} and {@code ipAddressMasks} are exposed as
 * {@link List List&lt;String&gt;} on the wire — the entity column itself stores
 * a JSON-encoded {@code IpList} object, but the JSON ↔ list conversion is
 * done in the REST persistence layer (see
 * {@code DataCollectionConfPersistenceService.toSystemDefDto} /
 * {@code applySystemDefDtoToEntity}). That keeps this module free of a
 * Jackson dependency and ensures every write path produces the canonical
 * JSON shape the runtime loader expects, regardless of whether the row was
 * created via XML migration, multipart upload, or direct REST CRUD.
 */
public class SnmpCollectionSystemDefDto {

    private Integer id;
    private String name;
    private String sysoid;
    private String sysoidMask;
    private List<String> ipAddresses;
    private List<String> ipAddressMasks;
    private String mibGroupNames;
    private Boolean enabled;

    // Flattened fields from SnmpCollectionSource
    private Integer collectionSourceId;
    private String collectionSourceName;

    public SnmpCollectionSystemDefDto() {
    }

    public SnmpCollectionSystemDefDto(
            Integer id,
            String name,
            String sysoid,
            String sysoidMask,
            List<String> ipAddresses,
            List<String> ipAddressMasks,
            String mibGroupNames,
            Boolean enabled,
            Integer collectionSourceId,
            String collectionSourceName) {

        this.id = id;
        this.name = name;
        this.sysoid = sysoid;
        this.sysoidMask = sysoidMask;
        this.ipAddresses = ipAddresses;
        this.ipAddressMasks = ipAddressMasks;
        this.mibGroupNames = mibGroupNames;
        this.enabled = enabled;
        this.collectionSourceId = collectionSourceId;
        this.collectionSourceName = collectionSourceName;
    }

    /* ===================== Getters & Setters ===================== */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public List<String> getIpAddressMasks() {
        return ipAddressMasks;
    }

    public void setIpAddressMasks(List<String> ipAddressMasks) {
        this.ipAddressMasks = ipAddressMasks;
    }

    public String getMibGroupNames() {
        return mibGroupNames;
    }

    public void setMibGroupNames(String mibGroupNames) {
        this.mibGroupNames = mibGroupNames;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getCollectionSourceId() {
        return collectionSourceId;
    }

    public void setCollectionSourceId(Integer collectionSourceId) {
        this.collectionSourceId = collectionSourceId;
    }

    public String getCollectionSourceName() {
        return collectionSourceName;
    }

    public void setCollectionSourceName(String collectionSourceName) {
        this.collectionSourceName = collectionSourceName;
    }
}
