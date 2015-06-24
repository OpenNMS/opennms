/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

/**
 * This class was original generated with Castor, but is no longer.
 */
package org.opennms.netmgt.config.vacuumd;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the vacuumd-configuration.xml configuration file.
 *
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "VacuumdConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class VacuumdConfiguration implements Serializable {
    private static final long serialVersionUID = -3370783056683052503L;

    // --------------------------/
    // - Class/Member Variables -/
    // --------------------------/

    /**
     * how often to vacuum the database in seconds
     */
    @XmlAttribute(name = "period")
    private Integer _period;

    /**
     * This represents the SQL that is performed every <period> seconds
     */
    @XmlElement(name = "statement")
    private List<Statement> _statementList = new ArrayList<Statement>(0);

    /**
     * Field _automations.
     */
    @XmlElement(name = "automations")
    private Automations _automations = new Automations();

    /**
     * A collection of triggers
     */
    @XmlElement(name = "triggers")
    private Triggers _triggers = new Triggers();

    /**
     * A collection of actions
     */
    @XmlElement(name = "actions")
    private Actions _actions = new Actions();

    /**
     * Field _autoEvents.
     */
    @XmlElement(name = "auto-events")
    private AutoEvents _autoEvents = new AutoEvents();;

    /**
     * Field _actionEvents.
     */
    @XmlElement(name = "action-events")
    private ActionEvents _actionEvents = new ActionEvents();

    // ----------------/
    // - Constructors -/
    // ----------------/

    public VacuumdConfiguration() {
        super();
    }

    public VacuumdConfiguration(final int period,
            final List<Statement> statements, final Automations automations,
            final Triggers triggers, final Actions actions,
            final AutoEvents autoEvents, final ActionEvents actionEvents) {
        super();
        setPeriod(period);
        setStatement(statements);
        setAutomations(automations);
        setTriggers(triggers);
        setActions(actions);
        setAutoEvents(autoEvents);
        setActionEvents(actionEvents);
    }

    // -----------/
    // - Methods -/
    // -----------/

    /**
     *
     *
     * @param vStatement
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addStatement(final Statement vStatement)
            throws IndexOutOfBoundsException {
        this._statementList.add(vStatement);
    }

    /**
     *
     *
     * @param index
     * @param vStatement
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void addStatement(final int index, final Statement vStatement)
            throws IndexOutOfBoundsException {
        this._statementList.add(index, vStatement);
    }

    /**
     * Method enumerateStatement.
     *
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<Statement> enumerateStatement() {
        return Collections.enumeration(this._statementList);
    }

    /**
     * Overrides the Object.equals method.
     *
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VacuumdConfiguration other = (VacuumdConfiguration) obj;
        if (_actionEvents == null) {
            if (other._actionEvents != null)
                return false;
        } else if (!_actionEvents.equals(other._actionEvents))
            return false;
        if (_actions == null) {
            if (other._actions != null)
                return false;
        } else if (!_actions.equals(other._actions))
            return false;
        if (_autoEvents == null) {
            if (other._autoEvents != null)
                return false;
        } else if (!_autoEvents.equals(other._autoEvents))
            return false;
        if (_automations == null) {
            if (other._automations != null)
                return false;
        } else if (!_automations.equals(other._automations))
            return false;
        if (_period == null) {
            if (other._period != null)
                return false;
        } else if (!_period.equals(other._period))
            return false;
        if (_statementList == null) {
            if (other._statementList != null)
                return false;
        } else if (!_statementList.equals(other._statementList))
            return false;
        if (_triggers == null) {
            if (other._triggers != null)
                return false;
        } else if (!_triggers.equals(other._triggers))
            return false;
        return true;
    }

    /**
     * Returns the value of field 'actionEvents'.
     *
     * @return the value of field 'ActionEvents'.
     */
    public ActionEvents getActionEvents() {
        return this._actionEvents;
    }

    /**
     * Returns the value of field 'actions'. The field 'actions' has the
     * following description: A collection of actions
     *
     * @return the value of field 'Actions'.
     */
    public Actions getActions() {
        return this._actions;
    }

    /**
     * Returns the value of field 'autoEvents'.
     *
     * @return the value of field 'AutoEvents'.
     */
    public AutoEvents getAutoEvents() {
        return this._autoEvents;
    }

    /**
     * Returns the value of field 'automations'.
     *
     * @return the value of field 'Automations'.
     */
    public Automations getAutomations() {
        return this._automations;
    }

    /**
     * Returns the value of field 'period'. The field 'period' has the
     * following description: how often to vacuum the database in seconds
     *
     * @return the value of field 'Period'.
     */
    public int getPeriod() {
        return _period == null ? 0 : _period;
    }

    /**
     * Method getStatement.
     *
     * @param index
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     * @return the value of the Statement at the given inde
     */
    public Statement getStatement(final int index)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._statementList.size()) {
            throw new IndexOutOfBoundsException("getStatement: Index value '"
                    + index + "' not in range [0.."
                    + (this._statementList.size() - 1) + "]");
        }

        return (Statement) _statementList.get(index);
    }

    /**
     * Method getStatement.Returns the contents of the collection in an Array.
     * <p>
     * Note: Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call.
     * This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     *
     * @return this collection as an Array
     */
    public Statement[] getStatement() {
        Statement[] array = new Statement[0];
        return (Statement[]) this._statementList.toArray(array);
    }

    /**
     * Method getStatementCollection.Returns a reference to '_statementList'.
     * No type checking is performed on any modifications to the Vector.
     *
     * @return a reference to the Vector backing this class
     */
    public List<Statement> getStatementCollection() {
        return this._statementList;
    }

    /**
     * Method getStatementCount.
     *
     * @return the size of this collection
     */
    public int getStatementCount() {
        return this._statementList.size();
    }

    /**
     * Returns the value of field 'triggers'. The field 'triggers' has the
     * following description: A collection of triggers
     *
     * @return the value of field 'Triggers'.
     */
    public Triggers getTriggers() {
        return this._triggers;
    }

    /**
     * Overrides the Object.hashCode method.
     * <p>
     * The following steps came from <b>Effective Java Programming Language
     * Guide</b> by Joshua Bloch, Chapter 3
     *
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_actionEvents == null) ? 0 : _actionEvents.hashCode());
        result = prime * result
                + ((_actions == null) ? 0 : _actions.hashCode());
        result = prime * result
                + ((_autoEvents == null) ? 0 : _autoEvents.hashCode());
        result = prime * result
                + ((_automations == null) ? 0 : _automations.hashCode());
        result = prime * result
                + ((_period == null) ? 0 : _period.hashCode());
        result = prime * result
                + ((_statementList == null) ? 0 : _statementList.hashCode());
        result = prime * result
                + ((_triggers == null) ? 0 : _triggers.hashCode());
        return result;
    }

    /**
     * Method iterateStatement.
     *
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<Statement> iterateStatement() {
        return this._statementList.iterator();
    }

    /**
     */
    public void removeAllStatement() {
        this._statementList.clear();
    }

    /**
     * Method removeStatement.
     *
     * @param vStatement
     * @return true if the object was removed from the collection.
     */
    public boolean removeStatement(final Statement vStatement) {
    	return _statementList.remove(vStatement);
    }

    /**
     * Method removeStatementAt.
     *
     * @param index
     * @return the element removed from the collection
     */
    public Statement removeStatementAt(final int index) {
    	return (Statement) this._statementList.remove(index);
    }

    /**
     * Sets the value of field 'actionEvents'.
     *
     * @param actionEvents
     *            the value of field 'actionEvents'.
     */
    public void setActionEvents(final ActionEvents actionEvents) {
        this._actionEvents = actionEvents;
    }

    /**
     * Sets the value of field 'actions'. The field 'actions' has the
     * following description: A collection of actions
     *
     * @param actions
     *            the value of field 'actions'.
     */
    public void setActions(final Actions actions) {
        this._actions = actions;
    }

    /**
     * Sets the value of field 'autoEvents'.
     *
     * @param autoEvents
     *            the value of field 'autoEvents'.
     */
    public void setAutoEvents(final AutoEvents autoEvents) {
        this._autoEvents = autoEvents;
    }

    /**
     * Sets the value of field 'automations'.
     *
     * @param automations
     *            the value of field 'automations'.
     */
    public void setAutomations(final Automations automations) {
        this._automations = automations;
    }

    /**
     * Sets the value of field 'period'. The field 'period' has the following
     * description: how often to vacuum the database in seconds
     *
     * @param period
     *            the value of field 'period'.
     */
    public void setPeriod(final int period) {
        this._period = period;
    }

    /**
     *
     *
     * @param index
     * @param vStatement
     * @throws IndexOutOfBoundsException
     *             if the index given is outside the bounds of the collection
     */
    public void setStatement(final int index, final Statement vStatement)
            throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._statementList.size()) {
            throw new IndexOutOfBoundsException("setStatement: Index value '"
                    + index + "' not in range [0.."
                    + (this._statementList.size() - 1) + "]");
        }

        this._statementList.set(index, vStatement);
    }

    /**
     *
     *
     * @param vStatementArray
     */
    public void setStatement(final Statement[] vStatementArray) {
        // -- copy array
        _statementList.clear();

        for (int i = 0; i < vStatementArray.length; i++) {
            this._statementList.add(vStatementArray[i]);
        }
    }

    /**
     * Sets the value of '_statementList' by copying the given Vector. All
     * elements will be checked for type safety.
     *
     * @param vStatementList
     *            the Vector to copy.
     */
    public void setStatement(final List<Statement> vStatementList) {
        // copy vector
        this._statementList.clear();

        this._statementList.addAll(vStatementList);
    }

    /**
     * Sets the value of field 'triggers'. The field 'triggers' has the
     * following description: A collection of triggers
     *
     * @param triggers
     *            the value of field 'triggers'.
     */
    public void setTriggers(final Triggers triggers) {
        this._triggers = triggers;
    }
}
