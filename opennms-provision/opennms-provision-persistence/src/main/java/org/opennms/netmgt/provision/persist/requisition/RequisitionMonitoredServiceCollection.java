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
 * <p>RequisitionMonitoredServiceCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="services")
public class RequisitionMonitoredServiceCollection extends LinkedList<RequisitionMonitoredService> {

    private static final long serialVersionUID = -1248948292315231797L;

    /**
	 * <p>Constructor for RequisitionMonitoredServiceCollection.</p>
	 */
	public RequisitionMonitoredServiceCollection() {
        super();
    }

    /**
     * <p>Constructor for RequisitionMonitoredServiceCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public RequisitionMonitoredServiceCollection(Collection<? extends RequisitionMonitoredService> c) {
        super(c);
    }

    /**
     * <p>getMonitoredServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="monitored-service")
    public List<RequisitionMonitoredService> getMonitoredServices() {
        return this;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param services a {@link java.util.List} object.
     */
    public void setMonitoredServices(List<RequisitionMonitoredService> services) {
        if (services == this) return;
        clear();
        addAll(services);
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

