/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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