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
package org.opennms.features.jest.client.credentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="elastic-credentials")
@XmlAccessorType(XmlAccessType.NONE)
public class ElasticCredentials {
    @XmlElement(name = "credentials")
    private List<CredentialsScope> credentialsScopes = new ArrayList<>();

    public List<CredentialsScope> getCredentialsScopes() {
        return credentialsScopes;
    }

    public void setCredentialsScopes(List<CredentialsScope> credentialsScopes) {
        this.credentialsScopes = credentialsScopes;
    }

    public ElasticCredentials withCredentials(CredentialsScope credentialsScope) {
        if (credentialsScope != null) {
            this.credentialsScopes.add(credentialsScope);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof ElasticCredentials) {
            final ElasticCredentials that = (ElasticCredentials) o;
            return Objects.equals(credentialsScopes, that.credentialsScopes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialsScopes);
    }
}
