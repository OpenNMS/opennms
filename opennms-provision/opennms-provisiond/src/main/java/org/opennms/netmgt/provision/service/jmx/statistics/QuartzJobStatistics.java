/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.jmx.statistics;

import java.util.Date;

import org.opennms.netmgt.provision.service.jmx.annotation.CompositeField;
import org.opennms.netmgt.provision.service.jmx.annotation.Description;
import org.opennms.netmgt.provision.service.jmx.annotation.TableId;
import org.quartz.JobDetail;
import org.quartz.Trigger;

@Description("Holds statistics about a Quartz DetailJob and Trigger with the same key")
public class QuartzJobStatistics {
    @CompositeField
    @Description("The key of the job. Usually 'name.group'")
    @TableId
    private final String key;

    @CompositeField
    private final String group;

    @CompositeField
    private final String description;

    @CompositeField
    private final String fullName;

    @CompositeField
    private final String jobClass;

    @CompositeField
    private final Date endTime;

    @CompositeField
    private final Date nextFireTime;

    @CompositeField
    private final String name;

    public QuartzJobStatistics(JobDetail jobDetail, Trigger trigger) {
        this.name = jobDetail.getName();
        this.key = jobDetail.getKey().toString();
        this.description = jobDetail.getDescription();
        this.fullName = jobDetail.getFullName();
        this.group = jobDetail.getGroup();
        this.jobClass = jobDetail.getJobClass().getName();
        this.nextFireTime = trigger.getNextFireTime();
        this.endTime = trigger.getEndTime();
    }
}
