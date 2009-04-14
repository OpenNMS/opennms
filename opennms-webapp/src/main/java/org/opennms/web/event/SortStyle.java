package org.opennms.web.event;

/** Convenience class to determine sort style of a query. */
public class SortStyle extends Object {
    /* CORBA-style enumeration */
    public static final int _SEVERITY = 1;

    public static final int _TIME = 2;

    public static final int _NODE = 3;

    public static final int _INTERFACE = 4;

    public static final int _SERVICE = 5;

    public static final int _POLLER = 6;

    public static final int _ID = 7;

    public static final SortStyle SEVERITY = new SortStyle("SEVERITY", _SEVERITY);

    public static final SortStyle TIME = new SortStyle("TIME", _TIME);

    public static final SortStyle NODE = new SortStyle("NODE", _NODE);

    public static final SortStyle INTERFACE = new SortStyle("INTERFACE", _INTERFACE);

    public static final SortStyle SERVICE = new SortStyle("SERVICE", _SERVICE);

    public static final SortStyle POLLER = new SortStyle("POLLER", _POLLER);

    public static final SortStyle ID = new SortStyle("ID", _ID);

    public static final int _REVERSE_SEVERITY = 101;

    public static final int _REVERSE_TIME = 102;

    public static final int _REVERSE_NODE = 103;

    public static final int _REVERSE_INTERFACE = 104;

    public static final int _REVERSE_SERVICE = 105;

    public static final int _REVERSE_POLLER = 106;

    public static final int _REVERSE_ID = 107;

    public static final SortStyle REVERSE_SEVERITY = new SortStyle("REVERSE_SEVERITY", _REVERSE_SEVERITY);

    public static final SortStyle REVERSE_TIME = new SortStyle("REVERSE_TIME", _REVERSE_TIME);

    public static final SortStyle REVERSE_NODE = new SortStyle("REVERSE_NODE", _REVERSE_NODE);

    public static final SortStyle REVERSE_INTERFACE = new SortStyle("REVERSE_INTERFACE", _REVERSE_INTERFACE);

    public static final SortStyle REVERSE_SERVICE = new SortStyle("REVERSE_SERVICE", _REVERSE_SERVICE);

    public static final SortStyle REVERSE_POLLER = new SortStyle("REVERSE_POLLER", _REVERSE_POLLER);

    public static final SortStyle REVERSE_ID = new SortStyle("REVERSE_ID", _REVERSE_ID);

    protected String name;

    protected int id;

    private SortStyle(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String toString() {
        return ("Event.SortStyle." + this.name);
    }

    public String getName() {
        return (this.name);
    }

    public int getId() {
        return (this.id);
    }
}