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
package org.opennms.api.reporting.parameter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.opennms.api.reporting.ReportMode;

/**
 * <p>ReportDateParm class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportDateParm extends ReportParm implements Serializable {
    private static final long serialVersionUID = -8528562178984136887L;

    private Date m_date;
    private Boolean m_useAbsoluteDate;
    private String m_interval;
    private Integer m_count;
    private Integer m_hours;
    private Integer m_minutes;
    private boolean m_isAdjustedDate;
    
    public Boolean getUseAbsoluteDate() {
        return m_useAbsoluteDate;
    }

    public void setUseAbsoluteDate(Boolean useAbsoluteDate) {
        m_useAbsoluteDate = useAbsoluteDate;
    }

    public String getInterval() {
        return m_interval;
    }

    public void setInterval(String interval) {
        m_interval = interval;
    }

    public Integer getCount() {
        return m_count;
    }

    public void setCount(Integer count) {
        m_count = count;
    }
    
    public Date getDate() {
        return m_date;
    }

    public void setDate(Date date) {
        m_date = date;
    }

    public Date getValue(ReportMode mode) {
        if ((mode == ReportMode.SCHEDULED) && (m_useAbsoluteDate == false)) {
            final Calendar cal = Calendar.getInstance();
            // use the offset date set when the report was scheduled
            int amount = 0 - m_count;
            if (m_interval.equals("year")) {
                cal.add(Calendar.YEAR, amount);
            } else { 
                if (m_interval.equals("month")) {
                    cal.add(Calendar.MONTH, amount);
                } else {
                    cal.add(Calendar.DATE, amount);
                }
            }
            if (m_hours != null) {
                cal.set(Calendar.HOUR_OF_DAY, m_hours);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 0);
            }
            if (m_minutes != null) {
                cal.set(Calendar.MINUTE, m_minutes);
            } else {
                cal.set(Calendar.MINUTE, 0);
            }
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            return cal.getTime();
        }

        return this.getAdjustedDate();
    }

    public Integer getHours() {
        return m_hours;
    }

    public void setHours(Integer hour) {
        m_hours = hour;
    }

    public Integer getMinutes() {
        return m_minutes;
    }

    public void setMinutes(Integer minute) {
        m_minutes = minute;
    }

    @Override
    void accept(ReportParmVisitor visitor) {
        Objects.requireNonNull(visitor).visit(this);
    }

    public boolean isAdjustedDate() {
        return m_isAdjustedDate;
    }

    public void setIsAdjustedDate(final boolean adjusted) {
        m_isAdjustedDate = adjusted;
    }

    private Date getAdjustedDate() {
        if (m_isAdjustedDate) {
            return m_date;
        }

        long millis = m_date.getTime();
        if (!m_isAdjustedDate) {
            if (m_hours != null) {
                millis += (m_hours * 60 * 60 * 1000);
            }
            if (m_minutes != null) {
                millis += (m_minutes * 60 * 1000);
            }
        }
        return new Date(millis);
    }

}
