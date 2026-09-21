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
package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Table(name="classification_groups")
@Entity
public class Group {
    @Id
    @SequenceGenerator(name = "ruleSequence", sequenceName = "ruleNxtId", allocationSize = 1)
    @GeneratedValue(generator = "ruleSequence")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "readonly")
    private boolean readOnly = false;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "group", orphanRemoval = true)
    private List<Rule> rules = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Rule> getRules() {
        rules.sort(RulePositionComparator.INSTANCE);
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void addRule(Rule rule) {
        Objects.requireNonNull(rule);
        if (!rules.contains(rule)) {
            rules.add(rule);
            rule.setGroup(this);
        }
    }

    public void removeRule(Rule rule) {
        rules.remove(rule);
        rule.setGroup(null);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", readOnly=" + readOnly +
                ", enabled=" + enabled +
                ", position=" + position +
                ", description='" + description + '\'' +
                '}';
    }
}
