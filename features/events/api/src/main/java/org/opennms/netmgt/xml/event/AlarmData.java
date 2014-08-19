/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.event;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This element is used for converting events into alarms.
 * 
 */

@XmlRootElement(name="alarm-data")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class AlarmData implements Serializable {
	private static final long serialVersionUID = 3681502418413339216L;


    /**
     * Field _reductionKey.
     */
	@XmlAttribute(name="reduction-key", required=true)
    private java.lang.String _reductionKey;

    /**
     * Field _alarmType.
     */
	@XmlAttribute(name="alarm-type", required=true)
    private Integer _alarmType;

    /**
     * Field _clearKey.
     */
	@XmlAttribute(name="clear-key")
    private java.lang.String _clearKey;

    /**
     * Field _autoClean.
     */
	@XmlAttribute(name="auto-clean")
    private Boolean _autoClean = false;

    /**
     * Field _x733AlarmType.
     */
	@XmlAttribute(name="x733-alarm-type")
    private java.lang.String _x733AlarmType;

    /**
     * Field _x733ProbableCause.
     */
	@XmlAttribute(name="x733-probable-cause")
    private Integer _x733ProbableCause;
	
	/**
	 * Field m_updateField
	 */
    @XmlElement(name="update-field", required=false)
    private List<UpdateField> m_updateFieldList = new ArrayList<UpdateField>();
    

    public AlarmData() {
        super();
    }


    public void deleteAlarmType(
    ) {
    	this._alarmType = null;
    }

    /**
     */
    public void deleteAutoClean(
    ) {
        this._autoClean = null;
    }

    /**
     */
    public void deleteX733ProbableCause(
    ) {
        this._x733ProbableCause = null;
    }

    /**
     * Returns the value of field 'alarmType'.
     * 
     * @return the value of field 'AlarmType'.
     */
    public Integer getAlarmType() {
        return this._alarmType == null? 0 : this._alarmType;
    }

    /**
     * Returns the value of field 'autoClean'.
     * 
     * @return the value of field 'AutoClean'.
     */
    public Boolean getAutoClean() {
        return this._autoClean == null? false : this._autoClean;
    }

    /**
     * Returns the value of field 'clearKey'.
     * 
     * @return the value of field 'ClearKey'.
     */
    public java.lang.String getClearKey() {
        return this._clearKey;
    }

    /**
     * Returns the value of field 'reductionKey'.
     * 
     * @return the value of field 'ReductionKey'.
     */
    public java.lang.String getReductionKey(
    ) {
        return this._reductionKey;
    }

    /**
     * Returns the value of field 'x733AlarmType'.
     * 
     * @return the value of field 'X733AlarmType'.
     */
    public java.lang.String getX733AlarmType(
    ) {
        return this._x733AlarmType;
    }

    /**
     * Returns the value of field 'x733ProbableCause'.
     * 
     * @return the value of field 'X733ProbableCause'.
     */
    public Integer getX733ProbableCause() {
        return this._x733ProbableCause == null ? 0 : this._x733ProbableCause;
    }

    /**
     * Method hasAlarmType.
     * 
     * @return true if at least one AlarmType has been added
     */
    public boolean hasAlarmType(
    ) {
        return this._alarmType != null;
    }

    /**
     * Method hasAutoClean.
     * 
     * @return true if at least one AutoClean has been added
     */
    public boolean hasAutoClean(
    ) {
        return this._autoClean != null;
    }

    /**
     * Method hasX733ProbableCause.
     * 
     * @return true if at least one X733ProbableCause has been added
     */
    public boolean hasX733ProbableCause(
    ) {
        return this._x733ProbableCause != null;
    }

    /**
     * Returns the value of field 'autoClean'.
     * 
     * @return the value of field 'AutoClean'.
     */
    public Boolean isAutoClean(
    ) {
        return getAutoClean();
    }

    /**
     * Sets the value of field 'alarmType'.
     * 
     * @param alarmType the value of field 'alarmType'.
     */
    public void setAlarmType(
            final Integer alarmType) {
        this._alarmType = alarmType;
    }

    /**
     * Sets the value of field 'autoClean'.
     * 
     * @param autoClean the value of field 'autoClean'.
     */
    public void setAutoClean(
            final Boolean autoClean) {
        this._autoClean = autoClean;
    }

    /**
     * Sets the value of field 'clearKey'.
     * 
     * @param clearKey the value of field 'clearKey'.
     */
    public void setClearKey(
            final java.lang.String clearKey) {
        this._clearKey = clearKey;
    }

    /**
     * Sets the value of field 'reductionKey'.
     * 
     * @param reductionKey the value of field 'reductionKey'.
     */
    public void setReductionKey(
            final java.lang.String reductionKey) {
        this._reductionKey = reductionKey;
    }

    /**
     * Sets the value of field 'x733AlarmType'.
     * 
     * @param x733AlarmType the value of field 'x733AlarmType'.
     */
    public void setX733AlarmType(
            final java.lang.String x733AlarmType) {
        this._x733AlarmType = x733AlarmType;
    }

    /**
     * Sets the value of field 'x733ProbableCause'.
     * 
     * @param x733ProbableCause the value of field
     * 'x733ProbableCause'.
     */
    public void setX733ProbableCause(
            final Integer x733ProbableCause) {
        this._x733ProbableCause = x733ProbableCause;
    }
    
    public UpdateField[] getUpdateField() {
        return m_updateFieldList.toArray(new UpdateField[0]);
    }
    
    public Collection<UpdateField> getUpdateFieldCollection() {
        return m_updateFieldList;
    }
    
    public List<UpdateField> getUpdateFieldList() {
        return m_updateFieldList;
    }
    
    public int getUpdateFieldListCount() {
        return m_updateFieldList.size();
    }
    
    public Boolean hasUpdateFields() {
        Boolean hasFields = true;
        if (m_updateFieldList == null || m_updateFieldList.isEmpty()) {
            hasFields = false;
        }
        return hasFields;
    }
    
    public void setUpdateField(UpdateField[] fields) {
        m_updateFieldList.clear();
        for (int i = 0; i < fields.length; i++) {
            m_updateFieldList.add(fields[i]);
        }
    }
    
    public void setUpdateField(final List<UpdateField> fields) {
        if (m_updateFieldList == fields) return;
        m_updateFieldList.clear();
        m_updateFieldList.addAll(fields);
    }
    
    public void setUpdateFieldCollection(final Collection<UpdateField> fields) {
        if (m_updateFieldList == fields) return;
        m_updateFieldList.clear();
        m_updateFieldList.addAll(fields);
    }

        @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("reduction-key", _reductionKey)
    		.append("alarm-type", _alarmType)
    		.append("clear-key", _clearKey)
    		.append("auto-clean", _autoClean)
    		.append("x733-alarm-type", _x733AlarmType)
    		.append("x733-probable-cause", _x733ProbableCause)
    		.toString();
    }
}
