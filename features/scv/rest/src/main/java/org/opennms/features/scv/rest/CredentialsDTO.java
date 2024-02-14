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
package org.opennms.features.scv.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class CredentialsDTO {

    private String alias;

    private String username;
    private String password;
    private Map<String, String> attributes = new HashMap<>();

    public CredentialsDTO() {
    }

    public CredentialsDTO(String alias, String username, String password) {
        this.alias = alias;
        this.username = username;
        this.password = password;
    }

    public CredentialsDTO(String alias, String username, String password, Map<String, String> attributes) {
        this.alias = alias;
        this.username = username;
        this.password = password;
        this.attributes = attributes;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CredentialsDTO)) return false;
        CredentialsDTO dto = (CredentialsDTO) o;
        return Objects.equals(alias, dto.alias) && Objects.equals(username, dto.username) &&
                Objects.equals(password, dto.password) && Objects.equals(attributes, dto.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, username, password, attributes);
    }

    @Override
    public String toString() {
        return String.format("Credentials[alias=%s, username=%s,password=XXXXXX]", alias, username);
    }
}
