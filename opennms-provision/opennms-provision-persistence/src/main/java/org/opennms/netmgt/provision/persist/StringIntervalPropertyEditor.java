package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.joda.time.Duration;
import org.joda.time.Period;

public class StringIntervalPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.parsePeriod(text).toStandardDuration());
    }

    public String getAsText() {
        Period p = ((Duration)getValue()).toPeriod().normalizedStandard();
        return StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.print(p);
    } 
}
