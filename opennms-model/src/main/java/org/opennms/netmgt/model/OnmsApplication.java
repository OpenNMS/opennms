/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.core.style.ToStringCreator;

@Entity
/**
 * <p>OnmsApplication class.</p>
 */
@Table(name = "applications")
public class OnmsApplication implements Comparable<OnmsApplication> {

    private Integer m_id;

    private String m_name;

    private Set<OnmsMonitoredService> m_monitoredServices = new LinkedHashSet<OnmsMonitoredService>();

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name = "name", length=32, nullable=false, unique=true)
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getMonitoredServices</p>
     *
     * @return a {@link java.util.Set} object.
     * @since 1.8.1
     */
    @ManyToMany(
                mappedBy="applications",
                cascade={CascadeType.PERSIST, CascadeType.MERGE}
    )
    public Set<OnmsMonitoredService> getMonitoredServices() {
        return m_monitoredServices;
    }

    /**
     * <p>setMonitoredServices</p>
     *
     * @param services a {@link java.util.Set} object.
     * @since 1.8.1
     */
    public void setMonitoredServices(Set<OnmsMonitoredService> services) {
        m_monitoredServices = services;
    }

    /**
     * <p>addMonitoredService</p>
     *
     * @param service a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @since 1.8.1
     */
    public void addMonitoredService(OnmsMonitoredService service) {
        getMonitoredServices().add(service);
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a int.
     */
    @Override
    public int compareTo(OnmsApplication o) {
        return getName().compareToIgnoreCase(o.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        ToStringCreator creator = new ToStringCreator(this);
        creator.append("id", getId());
        creator.append("name", getName());
        return creator.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OnmsApplication) {
            OnmsApplication app = (OnmsApplication)obj;
            return getName().equals(app.getName());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
    


}
