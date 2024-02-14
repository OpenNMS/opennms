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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * <p>Generic memo for any element inside OpenNMS</p>
 *
 * @author <a href="mailto:Markus@OpenNMS.com">Markus Neumann</a>
 */
@XmlRootElement(name="memo")
@Entity
@Table(name = "memos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type", discriminatorType= DiscriminatorType.STRING)
@DiscriminatorValue(value="Memo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsMemo implements Serializable {

    private static final long serialVersionUID = 7272348439687562161L;

    @Id
    @Column(name = "id", nullable = false)
    @SequenceGenerator(name = "memoSequence", sequenceName = "memoNxtId")
    @GeneratedValue(generator = "memoSequence")
    @XmlAttribute(name="id")
    private Integer m_id;

    @Column(name = "body")
    private String m_body;

    @Column(name = "author")
    private String m_author;

    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date m_updated;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date m_created;
    
    @PreUpdate
    private void preUpdate() {
        m_updated = new Date();
    }

    @PrePersist
    private void prePersist() {
        m_created = new Date();
    }

    public String getBody() {
        return m_body;
    }

    public void setBody(String body) {
        this.m_body = body;
    }

    public Date getCreated() {
        return m_created;
    }

    public Integer getId() {
        return m_id;
    }

    public void setId(final Integer id) {
        m_id = id;
    }

    public Date getUpdated() {
        return m_updated;
    }

    public void setCreated(Date created) {
        this.m_created = created;
    }

    public void setUpdated(Date updated) {
        this.m_updated = updated;
    }
    
    public String getAuthor() {
        return m_author;
    }

    public void setAuthor(String author) {
        this.m_author = author;
    }
}