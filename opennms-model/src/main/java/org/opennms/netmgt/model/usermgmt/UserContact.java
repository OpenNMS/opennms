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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * One way of reaching a {@link UserAccount}; a user has at most one contact per {@link ContactType}.
 * Owned by the user's {@code contacts} collection, use {@link UserAccount#setContact} to change them.
 */
@Entity
@Table(name = "user_contacts")
public class UserContact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_contacts_seq")
    @SequenceGenerator(name = "user_contacts_seq", sequenceName = "user_contacts_id_seq", allocationSize = 1)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 32)
    private ContactType type;

    @Column(length = 256)
    private String info;

    @Column(name = "service_provider", length = 256)
    private String serviceProvider;

    public UserContact() {
    }

    public UserContact(final ContactType type, final String info, final String serviceProvider) {
        this.type = type;
        this.info = info;
        this.serviceProvider = serviceProvider;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(final ContactType type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(final String info) {
        this.info = info;
    }

    public String getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(final String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public String toString() {
        return "UserContact[" + type + "=" + info + "]";
    }
}
