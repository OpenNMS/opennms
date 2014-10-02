package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.ArrayList;
import java.util.List;

public enum AlarmSeverity {
//    INDETERMINATE(1, "Indeterminate"),
//    CLEARED(2, "Cleared"),
    NORMAL(3, "Normal"),
    WARNING(4, "Warning"),
    MINOR(5, "Minor"),
    MAJOR(6, "Major"),
    CRITICAL(7, "Critical");

    private final int m_id;
    private final String m_label;

    private AlarmSeverity(final int id, final String label) {
        m_id    = id;
        m_label = label;
    }

    public int getId() {
        return m_id;
    }
    
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>isLessThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.AlarmSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThan(final AlarmSeverity other) {
        return compareTo(other) < 0;
    }

    /**
     * <p>isLessThanOrEqual</p>
     *
     * @param other a {@link org.opennms.netmgt.model.AlarmSeverity} object.
     * @return a boolean.
     */
    public boolean isLessThanOrEqual(final AlarmSeverity other) {
        return compareTo(other) <= 0;
    }

    /**
     * <p>isGreaterThan</p>
     *
     * @param other a {@link org.opennms.netmgt.model.AlarmSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThan(final AlarmSeverity other) {
        return compareTo(other) > 0;
    }
    
    /**
     * <p>isGreaterThanOrEqual</p>
     *
     * @param other a {@link org.opennms.netmgt.model.AlarmSeverity} object.
     * @return a boolean.
     */
    public boolean isGreaterThanOrEqual(final AlarmSeverity other) {
        return compareTo(other) >= 0;
    }

    /**
     * <p>get</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.netmgt.model.AlarmSeverity} object.
     */
    public static AlarmSeverity get(final int id) {
        for (final AlarmSeverity severity : values()) {
            if (severity.getId() == id) {
                return severity;
            }
        }
        return AlarmSeverity.NORMAL;
    }

    /**
     * <p>get</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.AlarmSeverity} object.
     */
    public static AlarmSeverity get(final String label) {
        for (final AlarmSeverity severity : values()) {
            if (severity.getLabel().equalsIgnoreCase(label)) {
                return severity;
            }
        }
        return AlarmSeverity.NORMAL;
    }

    public static List<String> labels() {
        final List<String> labels = new ArrayList<String>();
        for (final AlarmSeverity severity : values()) {
            labels.add(severity.getLabel());
        }
        return labels;
    }
}
