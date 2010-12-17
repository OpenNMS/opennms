package org.opennms.netmgt.rt;

public class RTInaccessibleQueue extends RTQueue {
    private static final long serialVersionUID = 1L;

    public RTInaccessibleQueue(final long id) {
        setId(id);
    }

    public boolean isAccessible() {
        return false;
    }

}
