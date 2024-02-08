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
package org.opennms.features.topology.plugins.topo.asset.util;

import java.util.Objects;

import org.opennms.netmgt.model.OnmsAssetRecord;

public class AssetBuilder {
    private final OnmsAssetRecord assetRecord = new OnmsAssetRecord();

    private final NodeBuilder nodeBuilder;

    public AssetBuilder(NodeBuilder parent) {
        this.nodeBuilder = Objects.requireNonNull(parent);
    }

    public AssetBuilder withRegion(String region) {
        assetRecord.setRegion(region);
        return this;
    }

    public AssetBuilder withBuilding(String building) {
        assetRecord.setBuilding(building);
        return this;
    }

    public AssetBuilder withCategory(String category) {
        assetRecord.setCategory(category);
        return this;
    }

    public AssetBuilder withRack(String rack) {
        assetRecord.setRack(rack);
        return this;
    }

    public AssetBuilder withDescription(String description) {
        assetRecord.setDescription(description);
        return this;
    }

    public AssetBuilder withManufacturer(String manufacturer) {
        assetRecord.setManufacturer(manufacturer);
        return this;
    }

    public AssetBuilder withAddress1(String address1) {
        assetRecord.getGeolocation().setAddress1(address1);
        return this;
    }

    public AssetBuilder withCountry(String country) {
        assetRecord.getGeolocation().setCountry(country);
        return this;
    }

    public AssetBuilder withAddress2(String address2) {
        assetRecord.getGeolocation().setAddress2(address2);
        return this;
    }

    public AssetBuilder withCity(String city) {
        assetRecord.getGeolocation().setCity(city);
        return this;
    }

    public AssetBuilder withZip(String zip) {
        assetRecord.getGeolocation().setZip(zip);
        return this;
    }

    public AssetBuilder withState(String state) {
        assetRecord.getGeolocation().setState(state);
        return this;
    }

    public AssetBuilder withLatitude(String latitude) {
        assetRecord.getGeolocation().setLatitude(Double.parseDouble(latitude));
        return this;
    }

    public AssetBuilder withLongitude(String longitude) {
        assetRecord.getGeolocation().setLongitude(Double.parseDouble(longitude));
        return this;
    }

    public AssetBuilder withDivision(String division) {
        assetRecord.setDivision(division);
        return this;
    }

    public AssetBuilder withDepartment(String department) {
        assetRecord.setDepartment(department);
        return this;
    }

    public AssetBuilder withFloor(String floor) {
        assetRecord.setFloor(floor);
        return this;
    }

    public AssetBuilder withRoom(String room) {
        assetRecord.setRoom(room);
        return this;
    }

    public AssetBuilder withSlot(String slot) {
        assetRecord.setSlot(slot);
        return this;
    }

    public AssetBuilder withPort(String port) {
        assetRecord.setPort(port);
        return this;
    }

    public AssetBuilder withCircuitId(String circuitId) {
        assetRecord.setCircuitId(circuitId);
        return this;
    }

    public AssetBuilder withDisplayCategory(String displayCategory) {
        assetRecord.setDisplayCategory(displayCategory);
        return this;
    }

    public AssetBuilder withNotifyCategory(String notifyCategory) {
        assetRecord.setNotifyCategory(notifyCategory);
        return this;
    }

    public AssetBuilder withPollerCategory(String pollerCategory) {
        assetRecord.setPollerCategory(pollerCategory);
        return this;
    }

    public AssetBuilder withThresholdCategory(String thresholdCategory) {
        assetRecord.setThresholdCategory(thresholdCategory);
        return this;
    }

    public AssetBuilder withManagedObjectType(String managedObjectType) {
        assetRecord.setManagedObjectType(managedObjectType);
        return this;
    }

    public AssetBuilder withManagedObjectInstance(String managedObjectInstance) {
        assetRecord.setManagedObjectInstance(managedObjectInstance);
        return this;
    }

    public AssetBuilder withVendor(String vendor) {
        assetRecord.setVendor(vendor);
        return this;
    }

    public AssetBuilder withModelNumber(String modelNumber) {
        assetRecord.setModelNumber(modelNumber);
        return this;
    }

    public AssetBuilder withOperatingSystem(String operatingSystem) {
        assetRecord.setOperatingSystem(operatingSystem);
        return this;
    }

    public NodeBuilder done() {
        return nodeBuilder;
    }

    public OnmsAssetRecord getAssetRecord() {
        return assetRecord;
    }
}
