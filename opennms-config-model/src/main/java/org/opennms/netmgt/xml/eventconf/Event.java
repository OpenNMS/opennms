/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;
import org.opennms.netmgt.xml.eventconf.EventOrdering.EventOrderIndex;

@XmlRootElement(name="event")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
@XmlType(propOrder={})
public class Event implements Serializable, Comparable<Event> {
    private static final long serialVersionUID = 2L;

    /**
     * The event mask which helps to uniquely identify an event
     */
    @XmlElement(name="mask",required=false)
    private Mask m_mask;

    /**
     * The Universal Event Identifier
     */
    @XmlElement(name="uei", required=true)
    private String m_uei;

    /**
     * A human readable name used to identify an event in the web ui
     */
    @XmlElement(name="event-label", required=true)
    private String m_eventLabel;

    /**
     * The SNMP information from the trap
     */
    @XmlElement(name="snmp", required=false)
    private Snmp m_snmp;

    /**
     * The event description
     */
    @XmlElement(name="descr", required=true)
    private String m_descr;

    /**
     * The event logmsg
     */
    @XmlElement(name="logmsg", required=true)
    private Logmsg m_logmsg;

    /**
     * The event severity
     */
    @XmlElement(name="severity", required=true)
    private String m_severity;

    /**
     * The event correlation information
     */
    @XmlElement(name="correlation", required=false)
    private Correlation m_correlation;

    /**
     * The operator instruction for this event
     */
    @XmlElement(name="operinstruct", required=false)
    private String m_operinstruct;

    /**
     * The automatic action to occur when this event occurs
     */
    @XmlElement(name="autoaction", required=false)
    private List<Autoaction> m_autoactions = new ArrayList<>();

    /**
     * The varbind decoding tag used to decode value into a string
     */
    @XmlElement(name="varbindsdecode", required=false)
    private List<Varbindsdecode> m_varbindsdecodes = new ArrayList<>();

    /**
     * The varbind decoding tag used to decode value into a string
     */
    @XmlElement(name="parameter", required=false)
    private List<Parameter> m_parameters = new ArrayList<>();

    /**
     * The operator action to be taken when this event occurs
     */
    @XmlElement(name="operaction", required=false)
    private List<Operaction> m_operactions = new ArrayList<>();

    /**
     * The autoacknowledge information for the user
     */
    @XmlElement(name="autoacknowledge", required=false)
    private Autoacknowledge m_autoacknowledge;

    /**
     * A logical group with which to associate this event
     */
    @XmlElement(name="loggroup", required=false)
    private List<String> m_loggroups = new ArrayList<>();

    /**
     * The trouble ticket info
     */
    @XmlElement(name="tticket", required=false)
    private Tticket m_tticket;

    /**
     * The forwarding information for this event
     */
    @XmlElement(name="forward", required=false)
    private List<Forward> m_forwards = new ArrayList<>();

    /**
     * The script information for this event
     */
    @XmlElement(name="script", required=false)
    private List<Script> m_scripts = new ArrayList<>();

    /**
     * The text to be displayed on a 'mouseOver' event
     *  when this event is displayed in the event browser.
     */
    @XmlElement(name="mouseovertext", required=false)
    private String m_mouseovertext;

    /**
     * Data used to create an event.
     */
    @XmlElement(name="alarm-data", required=false)
    private AlarmData m_alarmData;

    @XmlElementWrapper(name="filters", required=false)
    @XmlElement(name="filter", required=true)
    private List<Filter> m_filters;

    @XmlTransient
    private EventMatcher m_matcher;

    /**
     * Index in the eventconf files used for ordering when searching for matching events.
     * Set by the DefaultEventConfDao
     */
    @XmlTransient
    private EventOrderIndex m_index;

    public Mask getMask() {
        return m_mask;
    }

    public void setMask(final Mask mask) {
        m_mask = mask;
    }

    public String getUei() {
        return m_uei;
    }

    public void setUei(final String uei) {
        m_uei = ConfigUtils.assertNotEmpty(uei, "uei").intern();
    }

    public String getEventLabel() {
        return m_eventLabel;
    }

    public void setEventLabel(final String eventLabel) {
        m_eventLabel = ConfigUtils.assertNotEmpty(eventLabel, "event-label").intern();
    }

    public Snmp getSnmp() {
        return m_snmp;
    }

    public void setSnmp(final Snmp snmp) {
        m_snmp = snmp;
    }

    public String getDescr() {
        return m_descr;
    }

    public void setDescr(final String descr) {
        m_descr = ConfigUtils.normalizeAndInternString(descr);
    }

    public Logmsg getLogmsg() {
        return m_logmsg;
    }

    public void setLogmsg(final Logmsg logmsg) {
        m_logmsg = ConfigUtils.assertNotNull(logmsg, "logmsg");
    }

    public String getSeverity() {
        return m_severity;
    }

    public void setSeverity(final String severity) {
        m_severity = ConfigUtils.assertNotEmpty(severity, "severity").intern();
    }

    public Correlation getCorrelation() {
        return m_correlation;
    }

    public void setCorrelation(final Correlation correlation) {
        m_correlation = correlation;
    }

    public String getOperinstruct() {
        return m_operinstruct;
    }

    public void setOperinstruct(final String operinstruct) {
        m_operinstruct = ConfigUtils.normalizeAndInternString(operinstruct);
    }

    public List<Autoaction> getAutoactions() {
        return m_autoactions;
    }

    public void setAutoactions(final List<Autoaction> autoactions) {
        if (m_autoactions == autoactions) return;
        m_autoactions.clear();
        if (autoactions != null) m_autoactions.addAll(autoactions);
    }

    public void addAutoaction(final Autoaction autoaction) {
        m_autoactions.add(autoaction);
    }

    public boolean removeAutoaction(final Autoaction autoaction) {
        return m_autoactions.remove(autoaction);
    }

    public List<Varbindsdecode> getVarbindsdecodes() {
        return m_varbindsdecodes;
    }

    public void setVarbindsdecodes(final List<Varbindsdecode> decodes) {
        if (m_varbindsdecodes == decodes) return;
        m_varbindsdecodes.clear();
        if (decodes != null) m_varbindsdecodes.addAll(decodes);
    }

    public void addVarbindsdecode(final Varbindsdecode varbindsdecode) {
        m_varbindsdecodes.add(varbindsdecode);
    }

    public boolean removeVarbindsdecode(final Varbindsdecode decode) {
        return m_varbindsdecodes.remove(decode);
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (m_parameters == parameters) return;
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    public List<Operaction> getOperactions() {
        return m_operactions;
    }

    public void setOperactions(final List<Operaction> operactions) {
        if (m_operactions == operactions) return;
        m_operactions.clear();
        if (operactions != null) m_operactions.addAll(operactions);
    }

    public void addOperaction(final Operaction operaction) {
        m_operactions.add(operaction);
    }

    public boolean removeOperaction(final Operaction operaction) {
        return m_operactions.remove(operaction);
    }

    public Autoacknowledge getAutoacknowledge() {
        return m_autoacknowledge;
    }

    public void setAutoacknowledge(final Autoacknowledge autoacknowledge) {
        m_autoacknowledge = autoacknowledge;
    }

    public List<String> getLoggroups() {
        return m_loggroups;
    }

    public void setLoggroups(final List<String> loggroups) {
        if (m_loggroups == loggroups) return;
        m_loggroups.clear();
        if (loggroups != null) m_loggroups.addAll(loggroups);
    }

    public void addLoggroup(final String loggroup) {
        m_loggroups.add(loggroup.intern());
    }

    public boolean removeLoggroup(final String loggroup) {
        return m_loggroups.remove(loggroup);
    }

    public Tticket getTticket() {
        return m_tticket;
    }

    public void setTticket(final Tticket tticket) {
        m_tticket = tticket;
    }

    public List<Forward> getForwards() {
        return m_forwards;
    }

    public void setForwards(final List<Forward> forwards) {
        if (m_forwards == forwards) return;
        m_forwards.clear();
        if (forwards != null) m_forwards.addAll(forwards);
    }

    public void addForward(final Forward forward) {
        m_forwards.add(forward);
    }

    public boolean removeForward(final Forward forward) {
        return m_forwards.remove(forward);
    }

    public List<Script> getScripts() {
        return m_scripts;
    }

    public void setScripts(final List<Script> scripts) {
        if (m_scripts == scripts) return;
        m_scripts.clear();
        if (scripts != null) m_scripts.addAll(scripts);
    }

    public void addScript(final Script script) {
        m_scripts.add(script);
    }

    public boolean removeScript(final Script script) {
        return m_scripts.remove(script);
    }

    public String getMouseovertext() {
        return m_mouseovertext;
    }

    public void setMouseovertext(final String mouseovertext) {
        m_mouseovertext = ConfigUtils.normalizeAndInternString(mouseovertext);
    }

    public AlarmData getAlarmData() {
        return m_alarmData;
    }

    public void setAlarmData(final AlarmData alarmData) {
        m_alarmData = alarmData;
    }

    public List<Filter> getFilters() {
        return m_filters == null? Collections.emptyList() : m_filters;
    }

    public void setFilters(final List<Filter> filters) {
        if (filters == m_filters) return;
        if (filters == null) {
            m_filters = null;
        } else {
            m_filters.clear();
            m_filters.addAll(filters);
        }
    }

    public void addFilter(final Filter filter) {
        if (m_filters == null) m_filters = new ArrayList<>();
        m_filters.add(filter);
    }

    public boolean removeFilter(final Filter filter) {
        if (m_filters == null) return false;
        return m_filters.remove(filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_mask,
                            m_uei,
                            m_eventLabel,
                            m_snmp,
                            m_descr,
                            m_logmsg,
                            m_severity,
                            m_correlation,
                            m_operinstruct,
                            m_autoactions,
                            m_varbindsdecodes,
                            m_parameters,
                            m_operactions,
                            m_autoacknowledge,
                            m_loggroups,
                            m_tticket,
                            m_forwards,
                            m_scripts,
                            m_mouseovertext,
                            m_alarmData,
                            m_filters,
                            m_matcher);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Event) {
            final Event that = (Event) obj;
            return Objects.equals(this.m_mask, that.m_mask) &&
                    Objects.equals(this.m_uei, that.m_uei) &&
                    Objects.equals(this.m_eventLabel, that.m_eventLabel) &&
                    Objects.equals(this.m_snmp, that.m_snmp) &&
                    Objects.equals(this.m_descr, that.m_descr) &&
                    Objects.equals(this.m_logmsg, that.m_logmsg) &&
                    Objects.equals(this.m_severity, that.m_severity) &&
                    Objects.equals(this.m_correlation, that.m_correlation) &&
                    Objects.equals(this.m_operinstruct, that.m_operinstruct) &&
                    Objects.equals(this.m_autoactions, that.m_autoactions) &&
                    Objects.equals(this.m_varbindsdecodes, that.m_varbindsdecodes) &&
                    Objects.equals(this.m_parameters, that.m_parameters) &&
                    Objects.equals(this.m_operactions, that.m_operactions) &&
                    Objects.equals(this.m_autoacknowledge, that.m_autoacknowledge) &&
                    Objects.equals(this.m_loggroups, that.m_loggroups) &&
                    Objects.equals(this.m_tticket, that.m_tticket) &&
                    Objects.equals(this.m_forwards, that.m_forwards) &&
                    Objects.equals(this.m_scripts, that.m_scripts) &&
                    Objects.equals(this.m_mouseovertext, that.m_mouseovertext) &&
                    Objects.equals(this.m_alarmData, that.m_alarmData) &&
                    Objects.equals(this.m_filters, that.m_filters) &&
                    Objects.equals(this.m_matcher, that.m_matcher);
        }
        return false;
    }

    public EventOrderIndex getIndex() {
        return m_index;
    }

    public void setIndex(final EventOrderIndex index) {
        m_index = index;
    }

    private EventMatcher constructMatcher() {
        if (m_mask == null || m_mask.getMaskelements().size() <= 0) {
            return m_uei == null ? EventMatchers.falseMatcher() : EventMatchers.ueiMatcher(m_uei);
        } else {
            return m_mask.constructMatcher();
        }
    }

    public boolean matches(final org.opennms.netmgt.xml.event.Event matchingEvent) {
        return m_matcher.matches(matchingEvent);
    }

    public void initialize(final EventOrderIndex eventOrderIndex) {
        m_index = eventOrderIndex;
        m_matcher = constructMatcher();
    }

    public List<String> getMaskElementValues(final String mename) {
        return m_mask == null ? null : m_mask.getMaskElementValues(mename);
    }

    @Override
    public int compareTo(final Event o) {
        return getIndex().compareTo(o.getIndex());
    }
}
