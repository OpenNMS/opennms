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

package org.opennms.netmgt.config.provisiond;


import java.io.Serializable;
import java.util.Arrays;
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
 * Defines an import job with a cron expression
 *  
 *  http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
 *  Field Name Allowed Values Allowed Special Characters
 *  Seconds 0-59 , - /
 *  Minutes 0-59 , - /
 *  Hours 0-23 , - /
 *  Day-of-month 1-31 , - ? / L W C
 *  Month 1-12 or JAN-DEC , - /
 *  Day-of-Week 1-7 or SUN-SAT , - ? / L C #
 *  Year (Opt) empty, 1970-2099 , - /
 */
public class RequisitionDef implements Serializable {
    private static final List<String> RESCAN_EXISTING_OPTIONS = Arrays.asList("true", "false", "dbonly");

    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_RESCAN_EXISTING = System.getProperty("org.opennms.provisiond.scheduleRescanForUpdatedNodes", "true");

    private String importUrlResource;

    private String importName;

    private String rescanExisting;

    private String cronSchedule;

    public Optional<String> getImportUrlResource() {
        return Optional.ofNullable(this.importUrlResource);
    }

    public void setImportUrlResource(final String importUrlResource) {
        this.importUrlResource = ConfigUtils.assertNotEmpty(importUrlResource, "import-url-resource");
    }

    public Optional<String> getImportName() {
        return Optional.ofNullable(this.importName);
    }

    public void setImportName(final String importName) {
        this.importName = ConfigUtils.assertNotEmpty(importName, "import-name");
    }

    public String getRescanExisting() {
        return this.rescanExisting != null ? this.rescanExisting : DEFAULT_RESCAN_EXISTING;
    }

    public void setRescanExisting(final String rescanExisting) {
        this.rescanExisting = ConfigUtils.assertOnlyContains(ConfigUtils.normalizeString(rescanExisting), RESCAN_EXISTING_OPTIONS, "rescan-existing");
    }

    public Optional<String> getCronSchedule() {
        return Optional.ofNullable(this.cronSchedule);
    }

    public void setCronSchedule(final String cronSchedule) {
        this.cronSchedule = ConfigUtils.assertNotEmpty(cronSchedule, "cron-schedule");
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.importUrlResource, 
            this.importName, 
            this.rescanExisting, 
            this.cronSchedule);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof RequisitionDef) {
            final RequisitionDef that = (RequisitionDef)obj;
            return Objects.equals(this.importUrlResource, that.importUrlResource)
                && Objects.equals(this.importName, that.importName)
                && Objects.equals(this.rescanExisting, that.rescanExisting)
                && Objects.equals(this.cronSchedule, that.cronSchedule);
        }
        return false;
    }
}