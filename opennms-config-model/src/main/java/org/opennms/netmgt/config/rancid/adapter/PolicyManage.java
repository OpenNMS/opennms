/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.rancid.adapter;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * This represents a policy to manage a provisioned node
 *  if matched a node will be added updated or deleted using
 *  the element attribute definitions .
 */
@XmlRootElement(name = "policy-manage")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("rancid-adapter-configuration.xsd")
public class PolicyManage implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The name of the policy
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * The time in sec to wait before trying
     *  to set the download flag to up in router.db.
     *  If schedule is there then it is verified if you are able
     *  to write to router.db in rancid.
     *  Otherwise you wait until schedule let you write on rancid.
     *  
     */
    @XmlAttribute(name = "delay")
    private Long m_delay;

    /**
     * The maximum number of retry before
     *  sending a failure.
     */
    @XmlAttribute(name = "retries")
    private Integer m_retries;

    /**
     * If you want to use opennms categories
     *  to match rancid device type.
     */
    @XmlAttribute(name = "useCategories")
    private Boolean m_useCategories;

    /**
     * The Default Rancid type, it is used when no device type
     *  for provisioned node is found.
     */
    @XmlAttribute(name = "default-type")
    private String m_defaultType;

    /**
     * Package encapsulating addresses, services to be polled
     *  for these addresses, etc..
     */
    @XmlElement(name = "package", required = true)
    private Package m_package;

    /**
     * This is a time when you can schedule set up/down
     *  to rancid
     */
    @XmlElement(name = "schedule")
    private List<Schedule> m_schedules = new ArrayList<>();

    public PolicyManage() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<Long> getDelay() {
        return Optional.ofNullable(m_delay);
    }

    public void setDelay(final Long delay) {
        m_delay = delay;
    }

    public Optional<Integer> getRetries() {
        return Optional.ofNullable(m_retries);
    }

    public void setRetries(final Integer retries) {
        m_retries = retries;
    }

    public Optional<Boolean> getUseCategories() {
        return Optional.ofNullable(m_useCategories);
    }

    public void setUseCategories(final Boolean useCategories) {
        m_useCategories = useCategories;
    }

    public Optional<String> getDefaultType() {
        return Optional.ofNullable(m_defaultType);
    }

    public void setDefaultType(final String defaultType) {
        m_defaultType = ConfigUtils.normalizeString(defaultType);
    }

    public Package getPackage() {
        return m_package;
    }

    public void setPackage(final Package p) {
        m_package = ConfigUtils.assertNotNull(p, "package");
    }

    public List<Schedule> getSchedules() {
        return m_schedules;
    }

    public void setSchedules(final List<Schedule> schedules) {
        if (schedules == m_schedules) return;
        m_schedules.clear();
        if (schedules != null) m_schedules.addAll(schedules);
    }

    public void addSchedule(final Schedule schedule) {
        m_schedules.add(schedule);
    }

    public boolean removeSchedule(final Schedule schedule) {
        return m_schedules.remove(schedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_delay, 
                            m_retries, 
                            m_useCategories, 
                            m_defaultType, 
                            m_package, 
                            m_schedules);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof PolicyManage) {
            final PolicyManage that = (PolicyManage)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_delay, that.m_delay)
                    && Objects.equals(this.m_retries, that.m_retries)
                    && Objects.equals(this.m_useCategories, that.m_useCategories)
                    && Objects.equals(this.m_defaultType, that.m_defaultType)
                    && Objects.equals(this.m_package, that.m_package)
                    && Objects.equals(this.m_schedules, that.m_schedules);
        }
        return false;
    }

}
