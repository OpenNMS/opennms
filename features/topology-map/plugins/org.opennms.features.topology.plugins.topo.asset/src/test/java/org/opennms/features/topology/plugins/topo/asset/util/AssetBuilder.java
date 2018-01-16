/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
