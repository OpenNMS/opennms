package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachineManager {
	
	private Map<Integer, StateMachine> m_machines = new HashMap<Integer, StateMachine>();
	private List<MachineLifetimeListener> m_listeners = new ArrayList<MachineLifetimeListener>();
	
	/**
	 * Listeners add via this method are stored as references and will be garbage
	 * collected if no other references to them exist.
	 * @param listener
	 */
	public void addTransientMachineLifetimeListener(MachineLifetimeListener listener) {
		m_listeners.add(listener);
	}
	
	/**
	 * Removes listeners added using addTransientMachineListimeListener
	 * @param listener
	 */
	public void removeTransientMachineLifetimeListener(MachineLifetimeListener listener) {
		m_listeners.remove(listener);	
	}

	public void setMachine(Integer id, StateMachine stateMachine) {
		m_machines.put(id, stateMachine);
		fireMachineCreated(stateMachine);
	}

	private MachineLifetimeListener[] getListeners() {
		return m_listeners.toArray(new MachineLifetimeListener[m_listeners.size()]);
	}

	public StateMachine getMachine(Integer id) {
		return m_machines.get(id);
	}

	public void removeMachine(Integer id) {
		StateMachine stateMachine = m_machines.remove(id);
		fireMachineCompleted(stateMachine);
	}
	
	private void fireMachineCreated(StateMachine stateMachine) {
		MachineLifetimeListener[] listeners = getListeners();
		MachineLifetimeEvent e = null;
		for (MachineLifetimeListener listener : listeners) {
			if (e == null) {
				e = new MachineLifetimeEvent(this, MachineLifetimeEvent.Type.MACHINE_CREATED, stateMachine);
			}
			listener.machineCreated(e);
		}
	}

	private void fireMachineCompleted(StateMachine stateMachine) {
		MachineLifetimeListener[] listeners = getListeners();
		MachineLifetimeEvent e = null;
		for (MachineLifetimeListener listener : listeners) {
			if (e == null) {
				e = new MachineLifetimeEvent(this, MachineLifetimeEvent.Type.MACHINE_COMPLETED, stateMachine);
			}
			listener.machineCompleted(e);
		}
	}


}
