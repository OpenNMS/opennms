package org.opennms.web.event;

/** Convenience class to determine what sort of events to include in a query. */
public class AcknowledgeType extends Object {
    /* CORBA-style enumeration */
    public static final int _ACKNOWLEDGED = 1;

    public static final int _UNACKNOWLEDGED = 2;

    public static final int _BOTH = 3;

    public static final AcknowledgeType ACKNOWLEDGED = new AcknowledgeType("ACKNOWLEDGED", _ACKNOWLEDGED);

    public static final AcknowledgeType UNACKNOWLEDGED = new AcknowledgeType("UNACKNOWLEDGED", _UNACKNOWLEDGED);

    public static final AcknowledgeType BOTH = new AcknowledgeType("BOTH", _BOTH);

    protected String name;

    protected int id;

    private AcknowledgeType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String toString() {
        return ("Event.AcknowledgeType." + this.name);
    }

    public String getName() {
        return (this.name);
    }

    public int getId() {
        return (this.id);
    }

}