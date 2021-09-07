/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwEntity {

    private Integer id;
    private Integer parentId;
    private Integer nodeId;
    private Integer entPhysicalIndex;
    private Integer entPhysicalParentRelPos;
    private String entPhysicalName;
    private String entPhysicalDescr;
    private String entPhysicalAlias;
    private String entPhysicalVendorType;
    private String entPhysicalClass;
    private String entPhysicalMfgName;
    private String entPhysicalModelName;
    private String entPhysicalHardwareRev;
    private String entPhysicalFirmwareRev;
    private String entPhysicalSoftwareRev;
    private String entPhysicalSerialNum;
    private String entPhysicalAssetID;
    private Boolean entPhysicalIsFRU;
    private Date entPhysicalMfgDate;
    private String entPhysicalUris;
    private Set<HwEntity> children = new HashSet<>();
    private List<HwEntityAlias> hwEntityAliasList = new ArrayList<>();

    public HwEntity(Integer id, Integer parentId, Integer nodeId,
                    Integer entPhysicalIndex, Integer entPhysicalParentRelPos,
                    String entPhysicalName, String entPhysicalDescr,
                    String entPhysicalAlias, String entPhysicalVendorType,
                    String entPhysicalClass, String entPhysicalMfgName,
                    String entPhysicalModelName, String entPhysicalHardwareRev,
                    String entPhysicalFirmwareRev, String entPhysicalSoftwareRev,
                    String entPhysicalSerialNum, String entPhysicalAssetID,
                    Boolean entPhysicalIsFRU, Date entPhysicalMfgDate,
                    String entPhysicalUris) {
        this.id = id;
        this.parentId = parentId;
        this.nodeId = nodeId;
        this.entPhysicalIndex = entPhysicalIndex;
        this.entPhysicalParentRelPos = entPhysicalParentRelPos;
        this.entPhysicalName = entPhysicalName;
        this.entPhysicalDescr = entPhysicalDescr;
        this.entPhysicalAlias = entPhysicalAlias;
        this.entPhysicalVendorType = entPhysicalVendorType;
        this.entPhysicalClass = entPhysicalClass;
        this.entPhysicalMfgName = entPhysicalMfgName;
        this.entPhysicalModelName = entPhysicalModelName;
        this.entPhysicalHardwareRev = entPhysicalHardwareRev;
        this.entPhysicalFirmwareRev = entPhysicalFirmwareRev;
        this.entPhysicalSoftwareRev = entPhysicalSoftwareRev;
        this.entPhysicalSerialNum = entPhysicalSerialNum;
        this.entPhysicalAssetID = entPhysicalAssetID;
        this.entPhysicalIsFRU = entPhysicalIsFRU;
        this.entPhysicalMfgDate = entPhysicalMfgDate;
        this.entPhysicalUris = entPhysicalUris;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public Integer getEntPhysicalIndex() {
        return entPhysicalIndex;
    }

    public Integer getEntPhysicalParentRelPos() {
        return entPhysicalParentRelPos;
    }

    public String getEntPhysicalName() {
        return entPhysicalName;
    }

    public String getEntPhysicalDescr() {
        return entPhysicalDescr;
    }

    public String getEntPhysicalAlias() {
        return entPhysicalAlias;
    }

    public String getEntPhysicalVendorType() {
        return entPhysicalVendorType;
    }

    public String getEntPhysicalClass() {
        return entPhysicalClass;
    }

    public Boolean getEntPhysicalIsFRU() {
        return entPhysicalIsFRU;
    }

    public void addChild(HwEntity hwEntity) {
        children.add(hwEntity);
    }

    public Set<HwEntity> getChildren() {
        return children;
    }

    public List<HwEntityAlias> getHwEntityAliasList() {
        return hwEntityAliasList;
    }

    public void addHwEntityAliasList(List<HwEntityAlias> hwEntityAliasList) {
        this.hwEntityAliasList.addAll(hwEntityAliasList);
    }
}
