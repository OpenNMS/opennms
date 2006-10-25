package org.opennms.web.graph;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class RelativeTimePeriod {
    private static final RelativeTimePeriod[] s_defaultPeriods;
    
    private String m_id = null;
    private String m_name = null;
    private int m_offsetField = Calendar.DATE;
    private int m_offsetAmount = -1;
    
    static {
        s_defaultPeriods = new RelativeTimePeriod[] {
                new RelativeTimePeriod("lastday", "Last Day", Calendar.DATE,
                                       -1),
                new RelativeTimePeriod("lastweek", "Last Week",
                                       Calendar.DATE, -7),
                new RelativeTimePeriod("lastmonth", "Last Month",
                                       Calendar.DATE, -31),
                new RelativeTimePeriod("lastyear", "Last Year",
                                       Calendar.DATE, -366) };
    }

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
        return s_defaultPeriods;
    }
    
    public static RelativeTimePeriod getPeriodByIdOrDefault(String id) {
        return getPeriodByIdOrDefault(s_defaultPeriods, id,
                                      s_defaultPeriods[0]);
    }
    public static RelativeTimePeriod
        getPeriodByIdOrDefault(RelativeTimePeriod[] periods, String id,
                RelativeTimePeriod defaultPeriod) {
        // default to the first time period
        RelativeTimePeriod chosenPeriod = defaultPeriod;
        
        for (RelativeTimePeriod period : periods) {
            if (period.getId().equals(id)) {
                chosenPeriod = period;
                break;
            }
        }
        
        return chosenPeriod;
    }
    
    public long[] getStartAndEndTimes() {
        Calendar cal = new GregorianCalendar();
        long end = cal.getTime().getTime();
        cal.add(getOffsetField(), getOffsetAmount());
        long start = cal.getTime().getTime();        

        return new long[] { start, end };
    }
}
