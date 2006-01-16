package org.opennms.web.graph;

import java.util.Calendar;

public class RelativeTimePeriod {
    private String m_id = null;
    private String m_name = null;
    private int m_offsetField = Calendar.DATE;
    private int m_offsetAmount = -1;

    public RelativeTimePeriod() {
    }

    public RelativeTimePeriod(String id, String name, int offsetField,
                              int offsetAmount) {
        m_id = id;
        m_name = name;
        m_offsetField = offsetField;
        m_offsetAmount = offsetAmount;
    }

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        m_id = id;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public int getOffsetField() {
        return m_offsetField;
    }

    public void setOffsetField(int offsetField) {
        m_offsetField = offsetField;
    }

    public int getOffsetAmount() {
        return m_offsetAmount;
    }

    public void setOffsetAmount(int offsetAmount) {
        m_offsetAmount = offsetAmount;
    }

    public static RelativeTimePeriod[] getDefaultPeriods() {
        return new RelativeTimePeriod[] {
            new RelativeTimePeriod("lastday", "Last Day", Calendar.DATE, -1),
            new RelativeTimePeriod("lastweek", "Last Week", Calendar.DATE, -7),
            new RelativeTimePeriod("lastmonth", "Last Month", Calendar.DATE,
                                   -31),
            new RelativeTimePeriod("lastyear", "Last Year", Calendar.DATE, -366)
        };
    }
}
