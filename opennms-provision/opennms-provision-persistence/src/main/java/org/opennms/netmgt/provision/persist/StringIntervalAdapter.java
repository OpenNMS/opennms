package org.opennms.netmgt.provision.persist;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class StringIntervalAdapter extends XmlAdapter<String, Long> {
    private final PeriodFormatter m_format;
    
    public StringIntervalAdapter() {
        m_format = new PeriodFormatterBuilder()
            .appendYears().appendSuffix("y").appendSeparator(" ")
            .appendMonths().appendSuffix("M").appendSeparator(" ")
            .appendDays().appendSuffix("d").appendSeparator(" ")
            .appendHours().appendSuffix("h").appendSeparator(" ")
            .appendMinutes().appendSuffix("m").appendSeparator(" ")
            .appendSeconds().appendSuffix("s").appendSeparator(" ")
            .appendMillis().appendSuffix("ms")
            .toFormatter();
    }
    
    @Override
    public String marshal(Long v) throws Exception {
        Period p = new Period(v.longValue()).normalizedStandard();
        return m_format.print(p);
    }

    @Override
    public Long unmarshal(String v) throws Exception {
        return m_format.parsePeriod(v).normalizedStandard().toStandardDuration().getMillis();
    }

}
