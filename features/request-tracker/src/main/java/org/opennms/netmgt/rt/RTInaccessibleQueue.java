package org.opennms.netmgt.rt;

public class RTInaccessibleQueue extends RTQueue {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3834736202776385557L;

	public RTInaccessibleQueue(final long id) {
        setId(id);
    }

    public boolean isAccessible() {
        return false;
    }

}
