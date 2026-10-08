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
package org.opennms.netmgt.model.usermgmt;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A security role ({@code ROLE_ADMIN}, ...): a Spring Security authority granted to the users that hold it.
 * Not to be confused with {@link OnCallRole}, a notification rota.
 */
@Entity
@Table(name = "security_roles")
public class SecurityRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "security_roles_seq")
    @SequenceGenerator(name = "security_roles_seq", sequenceName = "security_roles_id_seq", allocationSize = 1)
    private Integer id;

    /** The full authority name, {@code ROLE_} included; see {@link SecurityRoles#toRoleName(String)}. */
    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    /** Shipped with OpenNMS (one of {@link SecurityRoles#BUILTIN_ROLES}); the DAO refuses to delete it. */
    @Column(nullable = false)
    private boolean builtin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    private Date lastModified;

    public SecurityRole() {
    }

    public SecurityRole(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        final Date now = new Date();
        if (createdTime == null) {
            createdTime = now;
        }
        lastModified = now;
    }

    @PreUpdate
    void onUpdate() {
        lastModified = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public void setBuiltin(final boolean builtin) {
        this.builtin = builtin;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(final Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /** Equal by name, the natural key, so roles behave in a user's role set whether loaded or new. */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SecurityRole)) {
            return false;
        }
        return name != null && name.equals(((SecurityRole) o).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "SecurityRole[" + name + "]";
    }
}
