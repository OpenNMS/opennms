package org.opennms.client;

/**
 * Possible states of an item whose progress we are monitoring.
 *
 * @author ranger
 * @version $Id: $
 */
public enum Progress {
    INDETERMINATE,
    INCOMPLETE,
    IN_PROGRESS,
    COMPLETE
}
