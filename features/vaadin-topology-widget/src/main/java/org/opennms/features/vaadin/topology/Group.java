package org.opennms.features.vaadin.topology;

public class Group {
	
	private int m_level;
	

	private Group m_parent;
	public static final Group ROOT = new Group(0);
	
	public Group(int level) {
		this(level, null);
	}
	
	public Group(int level, Group parent) {
		if(level <= parent.getLevel()) {
			throw new IllegalArgumentException("A group must have a level greater than its parents");
		}
		setLevel(level);
		setParent(parent);
	}

	public int getLevel() {
		return m_level;
	}

	public void setLevel(int level) {
		m_level = level;
	}
	public Group getParent() {
		return m_parent;
	}

	public void setParent(Group parent) {
		m_parent = parent;
	}
	
}
