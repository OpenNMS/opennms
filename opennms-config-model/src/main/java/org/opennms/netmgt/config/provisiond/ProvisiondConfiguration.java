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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Behavior configuration for the Provisioner Daemon
 */
@XmlRootElement(name = "provisiond-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("provisiond-configuration.xsd")
public class ProvisiondConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_REQUISITION_DIR = "${install.dir}/etc/imports";
    private static final String DEFAULT_FOREIGN_SOURCE_DIR = "${install.dir}/etc/foreign-sources";

    @XmlAttribute(name = "importThreads")
    private Long m_importThreads;

    @XmlAttribute(name = "scanThreads")
    private Long m_scanThreads;

    @XmlAttribute(name = "rescanThreads")
    private Long m_rescanThreads;

    @XmlAttribute(name = "writeThreads")
    private Long m_writeThreads;

    @XmlAttribute(name = "requistion-dir")
    private String m_requistionDir;

    @XmlAttribute(name = "foreign-source-dir")
    private String m_foreignSourceDir;

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
     */
    @XmlElement(name = "requisition-def")
    private List<RequisitionDef> m_requisitionDefs = new ArrayList<>();

    public Long getImportThreads() {
        return m_importThreads != null ? m_importThreads : 8L;
    }

    public void setImportThreads(final Long importThreads) {
        m_importThreads = importThreads;
    }

    public Long getScanThreads() {
        return m_scanThreads != null ? m_scanThreads : 10L;
    }

    public void setScanThreads(final Long scanThreads) {
        m_scanThreads = scanThreads;
    }

    public Long getRescanThreads() {
        return m_rescanThreads != null ? m_rescanThreads : 10L;
    }

    public void setRescanThreads(final Long rescanThreads) {
        m_rescanThreads = rescanThreads;
    }

    public Long getWriteThreads() {
        return m_writeThreads != null ? m_writeThreads : 8L;
    }

    public void setWriteThreads(final Long writeThreads) {
        m_writeThreads = writeThreads;
    }

    public String getRequistionDir() {
        return m_requistionDir != null ? m_requistionDir : DEFAULT_REQUISITION_DIR;
    }

    public void setRequistionDir(final String requistionDir) {
        m_requistionDir = ConfigUtils.assertNotEmpty(requistionDir, "requisition-dir");
    }

    public String getForeignSourceDir() {
        return m_foreignSourceDir != null ? m_foreignSourceDir : DEFAULT_FOREIGN_SOURCE_DIR;
    }

    public void setForeignSourceDir(final String foreignSourceDir) {
        m_foreignSourceDir = ConfigUtils.assertNotEmpty(foreignSourceDir, "foreign-source-dir");
    }

    public List<RequisitionDef> getRequisitionDefs() {
        return m_requisitionDefs;
    }

    public void setRequisitionDefs(final List<RequisitionDef> requisitionDefs) {
        if (requisitionDefs == m_requisitionDefs) return;
        m_requisitionDefs.clear();
        if (requisitionDefs != null) m_requisitionDefs.addAll(requisitionDefs);
    }

    public void addRequisitionDef(final RequisitionDef def) {
        m_requisitionDefs.add(def);
    }

    public boolean removeRequisitionDef(final RequisitionDef def) {
        return m_requisitionDefs.remove(def);
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(m_importThreads, 
                            m_scanThreads, 
                            m_rescanThreads, 
                            m_writeThreads, 
                            m_requistionDir, 
                            m_foreignSourceDir, 
                            m_requisitionDefs);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ProvisiondConfiguration) {
            final ProvisiondConfiguration that = (ProvisiondConfiguration)obj;
            return Objects.equals(this.m_importThreads, that.m_importThreads)
                    && Objects.equals(this.m_scanThreads, that.m_scanThreads)
                    && Objects.equals(this.m_rescanThreads, that.m_rescanThreads)
                    && Objects.equals(this.m_writeThreads, that.m_writeThreads)
                    && Objects.equals(this.m_requistionDir, that.m_requistionDir)
                    && Objects.equals(this.m_foreignSourceDir, that.m_foreignSourceDir)
                    && Objects.equals(this.m_requisitionDefs, that.m_requisitionDefs);
        }
        return false;
    }

}
