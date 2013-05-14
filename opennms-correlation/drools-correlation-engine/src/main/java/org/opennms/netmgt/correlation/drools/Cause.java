/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.drools;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>Cause class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Cause {
    
    public enum Type {
        POSSIBLE,
        IMPACT,
        ROOT
    }
    
    private Type m_type;
    private Long m_cause;
    private Event m_symptom;
    private Integer m_timerId;
    private final Set<Cause> m_impacted = new HashSet<Cause>();

    /**
     * <p>Constructor for Cause.</p>
     *
     * @param type a {@link org.opennms.netmgt.correlation.drools.Cause.Type} object.
     * @param cause a {@link java.lang.Long} object.
     * @param symptom a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param timerId a {@link java.lang.Integer} object.
     */
    public Cause(final Type type, final Long cause, final Event symptom, final Integer timerId) {
        m_type = type;
        m_cause = cause;
        m_symptom = symptom;
        m_timerId = timerId;
    }
    
    /**
     * <p>Constructor for Cause.</p>
     *
     * @param type a {@link org.opennms.netmgt.correlation.drools.Cause.Type} object.
     * @param cause a {@link java.lang.Long} object.
     * @param symptom a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Cause(final Type type, final Long cause, final Event symptom) {
        this(type, cause, symptom, null);
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link org.opennms.netmgt.correlation.drools.Cause.Type} object.
     */
    public Type getType() {
        return m_type;
    }
    
    /**
     * <p>setType</p>
     *
     * @param type a {@link org.opennms.netmgt.correlation.drools.Cause.Type} object.
     */
    public void setType(final Type type) {
        m_type = type;
    }

    /**
     * <p>getCause</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getCause() {
        return m_cause;
    }

    /**
     * <p>setCause</p>
     *
     * @param causeNodeId a {@link java.lang.Long} object.
     */
    public void setCause(final Long causeNodeId) {
        m_cause = causeNodeId;
    }

    /**
     * <p>getSymptom</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getSymptom() {
        return m_symptom;
    }

    /**
     * <p>setSymptom</p>
     *
     * @param symptomEvent a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void setSymptom(final Event symptomEvent) {
        m_symptom = symptomEvent;
    }
    
    /**
     * <p>getImpacted</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Cause> getImpacted() {
        return m_impacted;
    }
    
    /**
     * <p>addImpacted</p>
     *
     * @param cause a {@link org.opennms.netmgt.correlation.drools.Cause} object.
     */
    public void addImpacted(final Cause cause) {
        m_impacted.add(cause);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
    	return new ToStringBuilder(this)
            .append("type", m_type)
            .append("cause", m_cause)
            .append("symptom", m_symptom)
            .append("impacted", m_impacted)
            .toString();
    }

    /**
     * <p>getTimerId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getTimerId() {
        return m_timerId;
    }

    /**
     * <p>setTimerId</p>
     *
     * @param timerId a {@link java.lang.Integer} object.
     */
    public void setTimerId(final Integer timerId) {
        m_timerId = timerId;
    }

}
