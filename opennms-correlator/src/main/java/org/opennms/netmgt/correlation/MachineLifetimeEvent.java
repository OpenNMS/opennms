package org.opennms.netmgt.correlation;

import java.util.EventObject;

import org.springframework.core.style.ToStringCreator;

public class MachineLifetimeEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;

	public static enum Type {
		MACHINE_CREATED,
		MACHINE_COMPLETED
	}
	
	Type m_type;
	StateMachine m_machine;

	public MachineLifetimeEvent(StateMachineManager manager, Type type, StateMachine machine) {
		super(manager);
		m_type = type;
		m_machine = machine;
	}
	
	Type getType() {
		return m_type;
	}
	
	StateMachine getStateMachine() {
		return m_machine;
	}
	
	public int hashCode() {
		return getSource().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof MachineLifetimeEvent) {
			MachineLifetimeEvent other = (MachineLifetimeEvent)o;
			return (getSource() == other.getSource())
				&& (getType() == other.getType())
				&& (getStateMachine() == other.getStateMachine());
		}
		return false;
	}
	
	public String toString() {
		ToStringCreator buf = new ToStringCreator(this);
		buf.append("source", getSource());
		buf.append("type", m_type);
		buf.append("machine", m_machine);
		return buf.toString();
	}

}
