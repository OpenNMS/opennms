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

package org.opennms.features.apilayer.model;

import java.util.Objects;

import org.opennms.integration.api.v1.model.Geolocation;
import org.opennms.integration.api.v1.model.NodeAssetRecord;
import org.opennms.netmgt.model.OnmsAssetRecord;

public class NodeAssetRecordBean implements NodeAssetRecord {
    private final OnmsAssetRecord assetRecord;
    private final Geolocation geolocation;

    public NodeAssetRecordBean(OnmsAssetRecord assetRecord) {
        this.assetRecord = Objects.requireNonNull(assetRecord);
        this.geolocation = assetRecord.getGeolocation() != null ? new GeolocationBean(assetRecord.getGeolocation()) : null;
    }

    @Override
    public String getVendor() {
        return assetRecord.getVendor();
    }

    @Override
    public String getModelNumber() {
        return assetRecord.getModelNumber();
    }

    @Override
    public String getDescription() {
        return assetRecord.getDescription();
    }

    @Override
    public String getAssetNumber() {
        return assetRecord.getAssetNumber();
    }

    @Override
    public String getOperatingSystem() {
        return assetRecord.getOperatingSystem();
    }

    @Override
    public String getRegion() {
        return assetRecord.getRegion();
    }

    @Override
    public String getDivision() {
        return assetRecord.getDivision();
    }

    @Override
    public String getDepartment() {
        return assetRecord.getDepartment();
    }

    @Override
    public String getBuilding() {
        return assetRecord.getBuilding();
    }

    @Override
    public String getFloor() {
        return assetRecord.getFloor();
    }

    @Override
    public Geolocation getGeolocation() {
        return geolocation;
    }
}
