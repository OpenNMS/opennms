package org.opennms.netmgt.provision.persist;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class StringIntervalAdapter extends XmlAdapter<String, Duration> {
    public static final PeriodFormatter DEFAULT_PERIOD_FORMATTER = new PeriodFormatterBuilder()
    .appendWeeks().appendSuffix("w").appendSeparator(" ")
    .appendDays().appendSuffix("d").appendSeparator(" ")
    .appendHours().appendSuffix("h").appendSeparator(" ")
    .appendMinutes().appendSuffix("m").appendSeparator(" ")
    .appendSeconds().appendSuffix("s").appendSeparator(" ")
    .appendMillis().appendSuffix("ms")
    .toFormatter();
    
    @Override
    public String marshal(Duration v) {
        Period p = v.toPeriod().normalizedStandard();
        return DEFAULT_PERIOD_FORMATTER.print(p);
    }

    @Override
    public Duration unmarshal(String v) {
        return DEFAULT_PERIOD_FORMATTER.parsePeriod(v).toStandardDuration();
    }

}
