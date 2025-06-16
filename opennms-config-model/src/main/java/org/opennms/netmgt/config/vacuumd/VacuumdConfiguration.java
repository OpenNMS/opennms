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
package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Top-level element for the vacuumd-configuration.xml configuration file.
 */
@XmlRootElement(name = "VacuumdConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class VacuumdConfiguration implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * how often to vacuum the database in seconds
     */
    @XmlAttribute(name = "period", required = true)
    private Integer m_period;

    /**
     * This represents the SQL that is performed every <period> seconds
     */
    @XmlElement(name = "statement")
    private List<Statement> m_statements = new ArrayList<>();

    /**
     * Field m_automations.
     */
    @XmlElementWrapper(name = "automations")
    @XmlElement(name = "automation")
    private List<Automation> m_automations = new ArrayList<>();

    /**
     * A collection of triggers
     */
    @XmlElementWrapper(name = "triggers")
    @XmlElement(name = "trigger")
    private List<Trigger> m_triggers = new ArrayList<>();

    /**
     * A collection of actions
     */
    @XmlElementWrapper(name = "actions")
    @XmlElement(name = "action")
    private List<Action> m_actions = new ArrayList<>();

    @XmlElementWrapper(name = "auto-events")
    @XmlElement(name = "auto-event")
    private List<AutoEvent> m_autoEvents = new ArrayList<>();

    @XmlElementWrapper(name = "action-events")
    @XmlElement(name = "action-event")
    private List<ActionEvent> m_actionEvents = new ArrayList<>();

    public VacuumdConfiguration() {
        super();
    }

    public VacuumdConfiguration(final Integer period,
        final List<Statement> statements, final List<Automation> automations,
        final List<Trigger> triggers, final List<Action> actions,
        final List<AutoEvent> autoEvents, final List<ActionEvent> actionEvents) {
        setPeriod(period);
        setStatements(statements);
        setAutomations(automations);
        setTriggers(triggers);
        setActions(actions);
        setAutoEvents(autoEvents);
        setActionEvents(actionEvents);
    }

    public Integer getPeriod() {
        return m_period == null ? 0 : m_period;
    }

    public void setPeriod(final Integer period) {
        m_period = ConfigUtils.assertNotNull(period, "period");
    }

    public List<Statement> getStatements() {
        return m_statements;
    }

    public void setStatements(final List<Statement> statements) {
        if (statements == m_statements) return;
        m_statements.clear();
        if (statements != null) m_statements.addAll(statements);
    }

    public void addStatement(final Statement statement) {
        m_statements.add(statement);
    }

    public boolean removeStatement(final Statement statement) {
        return m_statements.remove(statement);
    }

    public List<Automation> getAutomations() {
        return m_automations;
    }

    public void setAutomations(final List<Automation> automations) {
        if (automations == m_automations) return;
        m_automations.clear();
        if (automations != null) m_automations.addAll(automations);
    }

    public void addAutomation(final Automation automation) {
        m_automations.add(automation);
    }

    public boolean removeAutomation(final Automation automation) {
        return m_automations.remove(automation);
    }

    public List<Trigger> getTriggers() {
        return m_triggers;
    }

    public void setTriggers(final List<Trigger> triggers) {
        if (triggers == m_triggers) return;
        m_triggers.clear();
        if (triggers != null) m_triggers.addAll(triggers);
    }

    public void addTrigger(final Trigger trigger) {
        m_triggers.add(trigger);
    }

    public boolean removeTrigger(final Trigger trigger) {
        return m_triggers.remove(trigger);
    }

    public List<Action> getActions() {
        return m_actions;
    }

    public void setActions(final List<Action> actions) {
        if (actions == m_actions) return;
        m_actions.clear();
        if (actions != null) m_actions.addAll(actions);
    }

    public void addAction(final Action action) {
        m_actions.add(action);
    }

    public boolean removeAction(final Action action) {
        return m_actions.remove(action);
    }

    public List<AutoEvent> getAutoEvents() {
        return m_autoEvents;
    }

    public void setAutoEvents(final List<AutoEvent> autoEvents) {
        if (autoEvents == m_autoEvents) return;
        m_autoEvents.clear();
        if (autoEvents != null) m_autoEvents.addAll(autoEvents);
    }

    public void addAutoEvent(final AutoEvent autoEvent) {
        m_autoEvents.add(autoEvent);
    }

    public boolean removeAutoEvent(final AutoEvent autoEvent) {
        return m_autoEvents.remove(autoEvent);
    }

    public List<ActionEvent> getActionEvents() {
        return m_actionEvents;
    }

    public void setActionEvents(final List<ActionEvent> actionEvents) {
        if (actionEvents == m_actionEvents) return;
        m_actionEvents.clear();
        if (actionEvents != null) m_actionEvents.addAll(actionEvents);
    }

    public void addActionEvent(final ActionEvent actionEvent) {
        m_actionEvents.add(actionEvent);
    }

    public boolean removeActionEvent(final ActionEvent actionEvent) {
        return m_actionEvents.remove(actionEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_period,
                            m_statements,
                            m_automations,
                            m_triggers,
                            m_actions,
                            m_autoEvents,
                            m_actionEvents);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VacuumdConfiguration) {
            final VacuumdConfiguration that = (VacuumdConfiguration) obj;
            return Objects.equals(this.m_period, that.m_period) &&
                    Objects.equals(this.m_statements, that.m_statements) &&
                    Objects.equals(this.m_automations, that.m_automations) &&
                    Objects.equals(this.m_triggers, that.m_triggers) &&
                    Objects.equals(this.m_actions, that.m_actions) &&
                    Objects.equals(this.m_autoEvents, that.m_autoEvents) &&
                    Objects.equals(this.m_actionEvents, that.m_actionEvents);
        }
        return false;
    }
}
