package org.opennms.systemreport;

import java.util.TreeMap;

import org.springframework.core.io.Resource;

public interface SystemReportPlugin extends Comparable<SystemReportPlugin> {
    /**
     * Get the name of this report plugin.
     * @return the name
     */
    public String getName();

    /**
     * Get a short description of the plugin's operation.
     * @return the description
     */
    public String getDescription();
    
    /**
     * Get the priority of this plugin.  This will be used to sort the various plugins' output when creating an aggregate report.
     * 1-10: system-level plugins
     * 11-50: related to core system functionality (eg, events, alarms, notifications)
     * 51-98: related to non-essential system functionality (eg, UI, reporting)
     * 99: unknown priority
     * @return the priority, from 1 to 99
     */
    public int getPriority();

    /**
     * Get a map of key/value pairs of data exposed by the plugin.
     * @return the plugin's data
     */
    public TreeMap<String,Resource> getEntries();
}
