/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist.requisition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * <p>RequisitionAssetCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="assets")
public class RequisitionAssetCollection extends LinkedList<RequisitionAsset> {

    private static final long serialVersionUID = -6944256042334743348L;

    /**
	 * <p>Constructor for RequisitionAssetCollection.</p>
	 */
	public RequisitionAssetCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionAssetCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionAssetCollection(Collection<? extends RequisitionAsset> c) {
        super(c);
    }

    /**
     * <p>getAssetFields</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="asset")
    public List<RequisitionAsset> getAssetFields() {
        return this;
    }

    /**
     * <p>setAssetFields</p>
     *
     * @param assets a {@link java.util.List} object.
     */
    public void setAssetFields(List<RequisitionAsset> assets) {
        if (assets == this) return;
        clear();
        addAll(assets);
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

