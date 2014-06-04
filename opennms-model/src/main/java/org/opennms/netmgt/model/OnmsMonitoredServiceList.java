/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsMonitoredServiceList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "services")
public class OnmsMonitoredServiceList extends LinkedList<OnmsMonitoredService> {

    private static final long serialVersionUID = 8031737923157780179L;

    /**
     * <p>Constructor for OnmsMonitoredServiceList.</p>
     */
    public OnmsMonitoredServiceList() {
        super();
    }

    /**
     * <p>Constructor for OnmsMonitoredServiceList.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsMonitoredServiceList(Collection<? extends OnmsMonitoredService> c) {
        super(c);
    }

    /**
     * <p>getServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "service")
    public List<OnmsMonitoredService> getServices() {
        return this;
    }

    /**
     * <p>setServices</p>
     *
     * @param services a {@link java.util.List} object.
     */
    public void setServices(List<OnmsMonitoredService> services) {
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

    /**
     * <p>getTotalCount</p>
     * 
     * Note that this list is different than others: count always equals totalCount
     * because we don't perform any limit/offset queries inside {$link OnmsMonitoredServiceResource}
     *
     * @return a int.
     */
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return this.size();
    }
}
