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

package org.opennms.netmgt.model;

import java.util.Date;
import java.util.List;

public class SnmpCollectionProfileDto {

    private Integer id;
    private String name;
    private Integer rrdStep;
    private List<String> rrdRras;
    private String storageFlag;
    private List<String> sourceNames;
    private Integer maxVarsPerPdu;
    private Boolean enabled;
    private Date createdTime;
    private Date lastModified;

    public SnmpCollectionProfileDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRrdStep() { return rrdStep; }
    public void setRrdStep(Integer rrdStep) { this.rrdStep = rrdStep; }

    public List<String> getRrdRras() { return rrdRras; }
    public void setRrdRras(List<String> rrdRras) { this.rrdRras = rrdRras; }

    public String getStorageFlag() { return storageFlag; }
    public void setStorageFlag(String storageFlag) { this.storageFlag = storageFlag; }

    public List<String> getSourceNames() { return sourceNames; }
    public void setSourceNames(List<String> sourceNames) { this.sourceNames = sourceNames; }

    public Integer getMaxVarsPerPdu() { return maxVarsPerPdu; }
    public void setMaxVarsPerPdu(Integer maxVarsPerPdu) { this.maxVarsPerPdu = maxVarsPerPdu; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
}
