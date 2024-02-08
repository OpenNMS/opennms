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
package org.opennms.features.reporting.model.remoterepository;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "remoteRepositoryConfig")
public class RemoteRepositoryConfig {

    private List<RemoteRepositoryDefinition> m_repositoryList = new ArrayList<>();

    private String jasperReportsVersion;
    
    @XmlElement(name = "remoteRepository")
    public List<RemoteRepositoryDefinition> getRepositoryList() {
        return m_repositoryList;
    }

    public void setRepositoryList(List<RemoteRepositoryDefinition> repositoryList) {
        this.m_repositoryList = repositoryList;
    }
    
    public String getJasperReportsVersion() {
        return jasperReportsVersion;
    }

    public void setJasperReportsVersion(String jasperReportsVersion) {
        this.jasperReportsVersion = jasperReportsVersion;
    }
    
}
