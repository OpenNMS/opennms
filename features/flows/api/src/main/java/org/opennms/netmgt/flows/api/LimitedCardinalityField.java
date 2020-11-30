package org.opennms.netmgt.flows.api;

/**
 * Describes fields that have only a limited number of possible values.
 *
 * Aggregations over these fields can contain results for all values.
 */
public enum LimitedCardinalityField {
    TOS(256, "netflow.tos"),
    DSCP(64,"netflow.dscp"),
    ECN(4, "netflow.ecn");
    public final int size;
    public final String fieldName;
    LimitedCardinalityField(int size, String fieldName) {
        this.size = size;
        this.fieldName = fieldName;
    }
}
