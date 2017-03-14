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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.provisiond;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "requisition-def")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequisitionDef implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_RESCAN_EXISTING = "true";

    @XmlAttribute(name = "import-url-resource", required = true)
    private String importUrlResource;

    @XmlAttribute(name = "import-name", required = true)
    private String importName;

    @XmlAttribute(name = "rescan-existing")
    private String rescanExisting;

    @XmlElement(name = "cron-schedule", required = true)
    private String cronSchedule;

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof RequisitionDef) {
            RequisitionDef temp = (RequisitionDef)obj;
            boolean equals = Objects.equals(temp.importUrlResource, importUrlResource)
                && Objects.equals(temp.importName, importName)
                && Objects.equals(temp.rescanExisting, rescanExisting)
                && Objects.equals(temp.cronSchedule, cronSchedule);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'cronSchedule'.
     * 
     * @return the value of field 'CronSchedule'.
     */
    public String getCronSchedule() {
        return this.cronSchedule;
    }

    /**
     * Returns the value of field 'importName'.
     * 
     * @return the value of field 'ImportName'.
     */
    public String getImportName() {
        return this.importName;
    }

    /**
     * Returns the value of field 'importUrlResource'.
     * 
     * @return the value of field 'ImportUrlResource'.
     */
    public String getImportUrlResource() {
        return this.importUrlResource;
    }

    /**
     * Returns the value of field 'rescanExisting'.
     * 
     * @return the value of field 'RescanExisting'.
     */
    public String getRescanExisting() {
        return this.rescanExisting != null ? this.rescanExisting : DEFAULT_RESCAN_EXISTING;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            importUrlResource, 
            importName, 
            rescanExisting, 
            cronSchedule);
        return hash;
    }

    /**
     * Sets the value of field 'cronSchedule'.
     * 
     * @param cronSchedule the value of field 'cronSchedule'.
     */
    public void setCronSchedule(final String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    /**
     * Sets the value of field 'importName'.
     * 
     * @param importName the value of field 'importName'.
     */
    public void setImportName(final String importName) {
        this.importName = importName;
    }

    /**
     * Sets the value of field 'importUrlResource'.
     * 
     * @param importUrlResource the value of field 'importUrlResource'.
     */
    public void setImportUrlResource(final String importUrlResource) {
        this.importUrlResource = importUrlResource;
    }

    /**
     * Sets the value of field 'rescanExisting'.
     * 
     * @param rescanExisting the value of field 'rescanExisting'.
     */
    public void setRescanExisting(final String rescanExisting) {
        this.rescanExisting = rescanExisting;
    }

}
