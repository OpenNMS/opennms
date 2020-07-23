/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.api.reporting.parameter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.ReportMode;

import com.google.common.collect.Lists;


/**
 * <p>ReportParameters class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportParameters implements Serializable {

    private static final long serialVersionUID = -3848794546173077375L;
    protected String m_reportId;
    protected ReportFormat m_format;
    protected List <ReportDateParm> m_dateParms;
    protected List <ReportStringParm> m_stringParms;
    protected List <ReportIntParm> m_intParms;
    protected List<ReportFloatParm> m_floatParms;
    protected List<ReportDoubleParm> m_doubleParms;
    protected List<ReportTimezoneParm> m_timezoneParms;

    public ReportParameters() {
        super();
    }

    public List<ReportDateParm> getDateParms() {
        return m_dateParms;
    }

    public void setDateParms(List<ReportDateParm> dateParms) {
        m_dateParms = dateParms;
    }
    
    public List<ReportStringParm> getStringParms() {
        return m_stringParms;
    }

    public void setStringParms(List<ReportStringParm> strings) {
        m_stringParms = strings;
    }
    
    public List<ReportIntParm> getIntParms() {
        return m_intParms;
    }

    public void setIntParms(List<ReportIntParm> ints) {
        m_intParms = ints;
    }
    
    public List<ReportFloatParm> getFloatParms() {
        return m_floatParms;
    }

    public void setFloatParms(List<ReportFloatParm> floats) {
        m_floatParms = floats;
    }

	public List<ReportDoubleParm> getDoubleParms() {
		return m_doubleParms;
	}

	public void setDoubleParms(List<ReportDoubleParm> doubleParms) {
		m_doubleParms = doubleParms;
	}

    public void setReportId(String reportId) {
        m_reportId = reportId;
    }

    public String getReportId() {
        return m_reportId;
    }

    public void setFormat(ReportFormat format) {
        m_format = format;
    }

    public ReportFormat getFormat() {
        return m_format;
    }

    public void setTimezoneParms(List<ReportTimezoneParm> timezoneParms) {
        m_timezoneParms = timezoneParms;
    }

    public List<ReportTimezoneParm> getTimezoneParms() {
        return m_timezoneParms;
    }

    public Map<String, Object> getReportParms(ReportMode mode) {
        
        HashMap <String,Object>parmMap = new HashMap<String, Object>();
        
        // Add all the strings from the report
        if (m_stringParms != null ) {
            Iterator<ReportStringParm>stringIter = m_stringParms.iterator();
            while (stringIter.hasNext()) {
                ReportStringParm parm = stringIter.next();
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        // Add all the dates from the report
        if (m_dateParms != null) {
            Iterator<ReportDateParm>dateIter = m_dateParms.iterator();
            while (dateIter.hasNext()) {
                ReportDateParm parm = dateIter.next();
                parmMap.put(parm.getName(), parm.getValue(mode));
            }
        }
        
        // Add all the integers from the report
        if (m_intParms != null) {
            Iterator<ReportIntParm>intIter = m_intParms.iterator();
            while (intIter.hasNext()) {
                ReportIntParm parm = intIter.next();
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        
        // Add all the floats from the report
        if (m_floatParms != null) {
            Iterator<ReportFloatParm>floatIter = m_floatParms.iterator();
            while (floatIter.hasNext()) {
                ReportFloatParm parm = floatIter.next();
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        
        // Add all the double parameters from the report
        if (m_doubleParms != null) {
            Iterator<ReportDoubleParm>doubleIter = m_doubleParms.iterator();
            while (doubleIter.hasNext()) {
                ReportDoubleParm parm = doubleIter.next();
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        if (m_timezoneParms != null) {
            for (ReportTimezoneParm parm : m_timezoneParms) {
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        return parmMap;
    }
    
    public Map<String, Object> getReportParms() {
        return getReportParms(ReportMode.IMMEDIATE);
    }

    public List<? extends ReportParm> getParameters() {
        return Lists.newArrayList(m_stringParms, m_dateParms, m_doubleParms, m_floatParms, m_intParms, m_timezoneParms)
                .stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    protected <T extends ReportParm> Map<String, T> asMap() {
        final Map<String, ? extends ReportParm> reportMap = getParameters()
                .stream()
                .collect(Collectors.toMap(p -> p.getName(), Function.identity()));
        return (Map<String, T>) reportMap;
    }

    public <T extends ReportParm> T getParameter(String key) {
        Objects.requireNonNull(key);
        final Optional<? extends ReportParm> any = getParameters()
                .stream()
                .filter(param -> key.equals(param.getName()))
                .findAny();
        if (any.isPresent()) {
            return (T) any.get();
        }
        return null;
    }

    public void apply(ReportParameters parameters) {
        Objects.requireNonNull(parameters);
        final Map<String, ReportParm> reportParmMap = asMap();
        final Map<String, ReportParm> othersParmMap = parameters.asMap();
        othersParmMap.entrySet().forEach(e -> {
            if (!reportParmMap.containsKey(e.getKey())) {
                throw new IllegalArgumentException("Cannot apply property of name " + e.getKey());
            }
            if (reportParmMap.get(e.getKey()).getClass() != othersParmMap.get(e.getKey()).getClass()) {
                throw new IllegalArgumentException("Cannot apply property of name " + e.getKey() + " due to type mismatch. " +
                        "Expected: " + reportParmMap.get(e.getKey()).getClass() +
                        "Actual: " + othersParmMap.get(e.getKey()).getClass());
            }
            final ReportParm thisReportParm = reportParmMap.get(e.getKey());
            final ReportParm otherReportParm = e.getValue();
            if (thisReportParm instanceof ReportStringParm) {
                ((ReportStringParm) thisReportParm).setValue(((ReportStringParm) otherReportParm).getValue());
            } else if (thisReportParm instanceof ReportDoubleParm) {
                ((ReportDoubleParm) thisReportParm).setValue(((ReportDoubleParm) otherReportParm).getValue());
            } else if (thisReportParm instanceof ReportIntParm) {
                ((ReportIntParm) thisReportParm).setValue(((ReportIntParm) otherReportParm).getValue());
            } else if (thisReportParm instanceof ReportFloatParm) {
                ((ReportFloatParm) thisReportParm).setValue(((ReportFloatParm) otherReportParm).getValue());
            } else if (thisReportParm instanceof ReportDateParm) {
                final ReportDateParm thisReportDateParm = (ReportDateParm) thisReportParm;
                final ReportDateParm othersReportDateParm = (ReportDateParm) otherReportParm;
                thisReportDateParm.setUseAbsoluteDate(othersReportDateParm.getUseAbsoluteDate());
                thisReportDateParm.setDate(othersReportDateParm.getDate());
                thisReportDateParm.setHours(othersReportDateParm.getHours());
                thisReportDateParm.setMinutes(othersReportDateParm.getMinutes());
                thisReportDateParm.setCount(othersReportDateParm.getCount());
                thisReportDateParm.setInterval(othersReportDateParm.getInterval());
            } else if (thisReportParm instanceof ReportTimezoneParm) {
                ((ReportTimezoneParm) thisReportParm).setValue(((ReportTimezoneParm) otherReportParm).getValue());
            } else {
                throw new IllegalArgumentException("Unknown parameter type " + otherReportParm.getClass() + " of property with name " + e.getKey());
            }
        });
    }
}
