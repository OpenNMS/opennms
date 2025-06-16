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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opennms.netmgt.config.utils.ConfigUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Behavior configuration for the Provisioner Daemon
 */
public class ProvisiondConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_REQUISITION_DIR = "${install.dir}/etc/imports";
    private static final String DEFAULT_FOREIGN_SOURCE_DIR = "${install.dir}/etc/foreign-sources";

    @JsonProperty("importThreads")
    private Long importThreads;

    @JsonProperty("scanThreads")
    private Long scanThreads;

    @JsonProperty("rescanThreads")
    private Long rescanThreads;

    @JsonProperty("writeThreads")
    private Long writeThreads;

    private String requistionDir;

    private String foreignSourceDir;

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
    @JsonProperty("requisition-def")
    private List<RequisitionDef> requisitionDefs = new ArrayList<>();

    public Long getImportThreads() {
        return this.importThreads != null ? this.importThreads : 8L;
    }

    public void setImportThreads(final Long importThreads) {
        this.importThreads = importThreads;
    }

    public Long getScanThreads() {
        return this.scanThreads != null ? this.scanThreads : 10L;
    }

    public void setScanThreads(final Long scanThreads) {
        this.scanThreads = scanThreads;
    }

    public Long getRescanThreads() {
        return this.rescanThreads != null ? this.rescanThreads : 10L;
    }

    public void setRescanThreads(final Long rescanThreads) {
        this.rescanThreads = rescanThreads;
    }

    public Long getWriteThreads() {
        return this.writeThreads != null ? this.writeThreads : 8L;
    }

    public void setWriteThreads(final Long writeThreads) {
        this.writeThreads = writeThreads;
    }

    public String getRequistionDir() {
        return this.requistionDir != null ? this.requistionDir : DEFAULT_REQUISITION_DIR;
    }

    public void setRequistionDir(final String requistionDir) {
        this.requistionDir = ConfigUtils.assertNotEmpty(requistionDir, "requisition-dir");
    }

    public String getForeignSourceDir() {
        return this.foreignSourceDir != null ? this.foreignSourceDir : DEFAULT_FOREIGN_SOURCE_DIR;
    }

    public void setForeignSourceDir(final String foreignSourceDir) {
        this.foreignSourceDir = ConfigUtils.assertNotEmpty(foreignSourceDir, "foreign-source-dir");
    }

    public List<RequisitionDef> getRequisitionDefs() {
        return this.requisitionDefs;
    }

    public void setRequisitionDefs(final List<RequisitionDef> requisitionDefs) {
        if (requisitionDefs == this.requisitionDefs) return;
        this.requisitionDefs.clear();
        if (requisitionDefs != null) this.requisitionDefs.addAll(requisitionDefs);
    }

    public void addRequisitionDef(final RequisitionDef def) {
        this.requisitionDefs.add(def);
    }

    public boolean removeRequisitionDef(final RequisitionDef def) {
        return this.requisitionDefs.remove(def);
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.importThreads, 
                            this.scanThreads, 
                            this.rescanThreads, 
                            this.writeThreads, 
                            this.requistionDir, 
                            this.foreignSourceDir, 
                            this.requisitionDefs);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ProvisiondConfiguration) {
            final ProvisiondConfiguration that = (ProvisiondConfiguration)obj;
            return Objects.equals(this.importThreads,that.importThreads)
                    && Objects.equals(this.scanThreads,that.scanThreads)
                    && Objects.equals(this.rescanThreads,that.rescanThreads)
                    && Objects.equals(this.writeThreads,that.writeThreads)
                    && Objects.equals(this.requistionDir,that.requistionDir)
                    && Objects.equals(this.foreignSourceDir,that.foreignSourceDir)
                    && Objects.equals(this.requisitionDefs,that.requisitionDefs);
        }
        return false;
    }
}