/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.model.remoterepository;

import javax.xml.bind.annotation.*;
import java.net.URI;

@XmlRootElement(name = "remoteRepository")
public class RemoteRepositoryDefinition {

    private Boolean m_repositoryActive;
    private URI m_URI;
    private String m_loginUser;
    private String m_loginRepoPassword;
    private String m_repositoryName;
    private String m_repositoryDescription;
    private String m_repositoryManagementURL;
    private String m_repositoryId;
     
    public String getLoginRepoPassword() {
        return m_loginRepoPassword;
    }
    public String getLoginUser() {
        return m_loginUser;
    }
    public String getRepositoryDescription() {
        return m_repositoryDescription;
    } 
    public String getRepositoryId() {
        return m_repositoryId;
    }
    public String getRepositoryManagementURL() {
        return m_repositoryManagementURL;
    }
    public String getRepositoryName() {
        return m_repositoryName;
    }
    public URI getURI() {
        return m_URI;
    }
    public Boolean isRepositoryActive() {
        return m_repositoryActive;
    }
    @XmlElement(name = "login-password")
    public void setLoginRepoPassword(String loginRepoPassword) {
        m_loginRepoPassword = loginRepoPassword;
    }
    @XmlElement(name = "login-user")
    public void setLoginUser(String loginUser) {
        m_loginUser = loginUser;
    }
    @XmlElement(name = "active")
    public void setRepositoryActive(Boolean repositoryActive) {
        m_repositoryActive = repositoryActive;
    }
    @XmlElement(name = "description")
    public void setRepositoryDescription(String repositoryDescription) {
        m_repositoryDescription = repositoryDescription;
    }
    @XmlElement(name = "id")
    public void setRepositoryId(String repositoryId) {
        m_repositoryId = repositoryId;
    }
    @XmlElement(name = "management-url")
    public void setRepositoryManagementURL(String repositoryManagementURL) {
        m_repositoryManagementURL = repositoryManagementURL;
    }
    @XmlElement(name = "name")
    public void setRepositoryName(String repositoryName) {
        m_repositoryName = repositoryName;
    }
    @XmlElement(name = "uri")
    public void setURI(URI uri) {
        m_URI = uri;
    }
    @Override
    public String toString() {
        return "RemoteRepositoryConfig [m_repositoryActive="
                + m_repositoryActive + ", m_URI=" + m_URI + ", m_loginUser="
                + m_loginUser + ", m_loginRepoPassword="
                + m_loginRepoPassword + ", m_repositoryName="
                + m_repositoryName + ", m_repositoryDescription="
                + m_repositoryDescription + ", m_repositoryManagementURL="
                + m_repositoryManagementURL + ", m_repositoryId="
                + m_repositoryId + "]";
    }
}
