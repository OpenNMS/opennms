//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model.inventory;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;


import java.util.Date;

@XmlRootElement(name = "inventoryAssetProperty")
@Entity
@Table(name = "inventoryassetproperty")
public class OnmsInventoryAssetProperty {
    @Id
    @Column(name="id")
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    @XmlTransient
    private int id;

    @ManyToOne(fetch=FetchType.EAGER,optional=false)
    @JoinColumn(name="inventoryAsset")
    private OnmsInventoryAsset inventoryAsset;

    @Column(name = "assetKey")
    private String assetKey;

    @Column(name = "assetValue")
    private String assetValue;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effdt")
    private Date effectiveDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scandt")
    private Date scanDate;

    @Column(name = "eff_status")
    private Boolean effStatus;

    public OnmsInventoryAssetProperty() {
        // do nothing.
    }
    
    public OnmsInventoryAssetProperty(OnmsInventoryAssetProperty assetProp) {
    	this.assetKey = assetProp.assetKey;
    	this.assetValue = assetProp.assetValue;
    	this.inventoryAsset = assetProp.inventoryAsset;
    	this.effectiveDate = assetProp.effectiveDate;
    	this.effStatus = assetProp.effStatus;
    	this.scanDate = assetProp.scanDate;
    }

    public OnmsInventoryAssetProperty(String assetKey,
                                      String assetValue) {
        this.inventoryAsset = null;
        this.assetKey = assetKey;
        this.assetValue = assetValue;
        this.effectiveDate = new Date();
        this.scanDate = new Date();
        this.effStatus = true;
    }

    public OnmsInventoryAssetProperty(OnmsInventoryAsset inventoryAsset,
                                      String assetKey,
                                      String assetValue) {
        this.inventoryAsset = inventoryAsset;
        this.assetKey = assetKey;
        this.assetValue = assetValue;
        this.effectiveDate = new Date();
        this.scanDate = new Date();
        this.effStatus = true;
    }

    public OnmsInventoryAssetProperty(OnmsInventoryAsset inventoryAsset,
                                      String assetKey,
                                      String assetValue,
                                      Date effdt) {
        this.inventoryAsset = inventoryAsset;
        this.assetKey = assetKey;
        this.assetValue = assetValue;
        this.effectiveDate = effdt;
        this.scanDate = effdt;
        this.effStatus = true;
    }
    
    public OnmsInventoryAssetProperty(OnmsInventoryAsset inventoryAsset,
            String assetKey,
            String assetValue,
            Date effdt,
            Date dateUpdated) {
    	this.inventoryAsset = inventoryAsset;
    	this.assetKey = assetKey;
    	this.assetValue = assetValue;
    	this.effectiveDate = effdt;
    	this.scanDate = dateUpdated;
    	this.effStatus = true;
    }

    public OnmsInventoryAssetProperty(OnmsInventoryAsset inventoryAsset,
                                      String assetKey,
                                      String assetValue,
                                      Date effdt,
                                      Boolean effStatus) {
        this.inventoryAsset = inventoryAsset;
        this.assetKey = assetKey;
        this.assetValue = assetValue;
        this.effectiveDate = effdt;
        this.scanDate = effdt;
        this.effStatus = effStatus;
    }
    
    public OnmsInventoryAssetProperty(OnmsInventoryAsset inventoryAsset,
            String assetKey,
            String assetValue,
            Date effdt,
            Date dateUpdated,
            Boolean effStatus) {
    	this.inventoryAsset = inventoryAsset;
		this.assetKey = assetKey;
		this.assetValue = assetValue;
		this.effectiveDate = effdt;
		this.scanDate = dateUpdated;
		this.effStatus = effStatus;
    }
    
    public int getId() {
        return id;
    }

    @XmlID
    @Transient
    public String getAssetPropertyId() {
        return Integer.toString(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public OnmsInventoryAsset getInventoryAsset() {
        return inventoryAsset;
    }

    public void setInventoryAsset(OnmsInventoryAsset inventoryAsset) {
        this.inventoryAsset = inventoryAsset;
    }

    public String getAssetKey() {
        return assetKey;
    }

    public void setAssetKey(String assetKey) {
        this.assetKey = assetKey;
    }

    public String getAssetValue() {
        return assetValue;
    }

    public void setAssetValue(String assetValue) {
        this.assetValue = assetValue;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effdt) {
        this.effectiveDate = effdt;
    }

    public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scandt) {
		this.scanDate = scandt;
	}

	public Boolean getEffStatus() {
        return effStatus;
    }

    public void setEffStatus(Boolean effStatus) {
        this.effStatus = effStatus;
    }
}
