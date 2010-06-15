//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Organize imports to eliminate warnings. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

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
@Table(name = "applications")
public class OnmsApplication implements Comparable<OnmsApplication> {

    private Integer m_id;

    private String m_name;

    private Set<OnmsMonitoredService> m_monitoredServices = new LinkedHashSet<OnmsMonitoredService>();

    @Id
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    @Column(name = "name", length=32, nullable=false, unique=true)
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @ManyToMany(
                mappedBy="applications",
                cascade={CascadeType.PERSIST, CascadeType.MERGE}
    )
    public Set<OnmsMonitoredService> getMonitoredServices() {
        return m_monitoredServices;
    }

    public void setMonitoredServices(Set<OnmsMonitoredService> services) {
        m_monitoredServices = services;
    }

    public void addMonitoredService(OnmsMonitoredService service) {
        getMonitoredServices().add(service);
    }

    public int compareTo(OnmsApplication o) {
        return getName().compareToIgnoreCase(o.getName());
    }
    
    @Override
    public String toString() {
        ToStringCreator creator = new ToStringCreator(this);
        creator.append("id", getId());
        creator.append("name", getName());
        return creator.toString();
    }

}
