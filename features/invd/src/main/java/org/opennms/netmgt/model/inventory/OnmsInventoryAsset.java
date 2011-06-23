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

import org.opennms.netmgt.model.OnmsNode;
import org.springframework.core.style.ToStringCreator;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlID;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.JoinColumn;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.Set;
import java.util.LinkedHashSet;

@XmlRootElement(name = "inventoryAsset")
@Entity
@Table(name = "inventoryasset")
public class OnmsInventoryAsset {

    @Id
    @Column(name="id")
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    @XmlTransient
    private int id;

    @XmlTransient
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="category")
    private OnmsInventoryCategory category;

    @Column(name = "assetName")
    private String assetName;

    @Column(name = "assetSource")
    private String assetSource;

    @XmlTransient
    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="ownerNode")
    private OnmsNode ownerNode;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "effdt")
    private Date effectiveDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scandt")
    private Date scanDate;
    
    @Column(name = "eff_status")
    private Boolean effStatus;

    @XmlTransient
    @OneToMany(mappedBy="inventoryAsset", fetch=FetchType.EAGER)
    @org.hibernate.annotations.Cascade( {
        org.hibernate.annotations.CascadeType.ALL,
        org.hibernate.annotations.CascadeType.DELETE })
    private Set<OnmsInventoryAssetProperty> properties = new LinkedHashSet<OnmsInventoryAssetProperty>();

    /**
     * Default constructor.
     */
    public OnmsInventoryAsset() {
        // do nothing.
    }

    /**
     * Constructor.
     *
     * This constructor defaults dateAdded to the current time and
     * assetSource to "Invd."
     *
     * @param category The inventory category this asset belongs to.
     * @param assetName The name of this asset.
     * @param ownerNode The node this asset belongs to.
     */
    public OnmsInventoryAsset(OnmsInventoryCategory category,
                              String assetName,
                              OnmsNode ownerNode) {
        this.assetName = assetName;
        this.category = category;
        this.ownerNode = ownerNode;
        this.effectiveDate = new Date();
        this.scanDate = new Date();
        this.assetSource = "Invd";
        this.effStatus = true;
    }

    /**
     * Constructor.
     *
     * This constructor requires all members except asset properties.
     * No member values are defaulted.
     *
     * @param category The inventory category this asset belongs to.
     * @param assetName The name of this asset.
     * @param assetSource The source of this asset, e.g "Invd" or "User"
     * @param ownerNode The node this asset belongs to.
     * @param effdt The date that this asset was added or changed.
     */     
    public OnmsInventoryAsset(OnmsInventoryCategory category,
                              String assetName,
                              String assetSource,
                              OnmsNode ownerNode,
                              Date effdt) {
        this.category = category;
        this.assetName = assetName;
        this.assetSource = assetSource;
        this.ownerNode = ownerNode;
        this.effectiveDate = effdt;
        this.effStatus = true;
        this.scanDate = new Date();

    }

    /**
     * Constructor.
     *
     * This constructor requires all members except asset properties.
     * No member values are defaulted.
     *
     * @param category The inventory category this asset belongs to.
     * @param assetName The name of this asset.
     * @param assetSource The source of this asset, e.g "Invd" or "User"
     * @param ownerNode The node this asset belongs to.
     * @param effdt The date that this asset was added or changed.
     * @param effStatus Whether this asset is effective (active) or not.
     */
    public OnmsInventoryAsset(OnmsInventoryCategory category,
                              String assetName,
                              String assetSource,
                              OnmsNode ownerNode,
                              Date effdt,
                              Boolean effStatus) {
        this.category = category;
        this.assetName = assetName;
        this.assetSource = assetSource;
        this.ownerNode = ownerNode;
        this.effectiveDate = effdt;
        this.effStatus = effStatus;
        this.scanDate = new Date();

    }
    
    public OnmsInventoryAsset(OnmsInventoryAsset asset) {
		this.id = asset.id;
		this.assetName = asset.assetName;
		this.assetSource = asset.assetSource;
		this.category = asset.category;
		this.effectiveDate = asset.effectiveDate;
		this.scanDate = asset.scanDate;
		this.ownerNode = asset.ownerNode;
		this.properties = new LinkedHashSet<OnmsInventoryAssetProperty>(asset.properties);
	}

	public int getId() {
        return id;
    }

    @XmlID
    @Transient
    public String getAssetId() {
        return Integer.toString(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public OnmsInventoryCategory getCategory() {
        return category;
    }

    public void setCategory(OnmsInventoryCategory category) {
        this.category = category;
    }

    public String getAssetSource() {
        return assetSource;
    }

    public void setAssetSource(String assetSource) {
        this.assetSource = assetSource;
    }

    public OnmsNode getOwnerNode() {
        return ownerNode;
    }

    public void setOwnerNode(OnmsNode ownerNode) {
        this.ownerNode = ownerNode;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effdt) {
        this.effectiveDate = effdt;
    }

    public Set<OnmsInventoryAssetProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<OnmsInventoryAssetProperty> properties) {
        this.properties = properties;
    }

    public boolean addProperty(OnmsInventoryAssetProperty prop) {
        return getProperties().add(prop);
    }

    public boolean removeProperty(OnmsInventoryAssetProperty prop) {
        return getProperties().remove(prop);
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public Boolean getEffStatus() {
        return effStatus;
    }

    public void setEffStatus(Boolean effStatus) {
        this.effStatus = effStatus;
    }

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scandt) {
		this.scanDate = scandt;
	}
	
	public OnmsInventoryAssetProperty getPropertyByName(String name) {
		for(OnmsInventoryAssetProperty prop : getProperties()) {
			if(prop.getAssetKey().equalsIgnoreCase(name))
				return prop;
		}
		
		return null;
	}

	public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("name", getAssetName())
            .toString();
    }
}
