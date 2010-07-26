package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.joda.time.Duration;
import org.joda.time.Period;

/**
 * <p>StringIntervalPropertyEditor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class StringIntervalPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    /** {@inheritDoc} */
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.parsePeriod(text).toStandardDuration());
    }

    /**
     * <p>getAsText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAsText() {
        Period p = ((Duration)getValue()).toPeriod().normalizedStandard();
        return StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.print(p);
    } 
}
