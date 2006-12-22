package org.opennms.netmgt.correlation;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;

public class NodeCorrelationStateMachine implements StateMachine {
	
	enum State {
		INITIAL,
		PARENT_DOWN_PENDING,
		FINAL
	}
	
	public interface NodeState {
		
		public State getState();
		
		public boolean isEndState();
		
		public NodeState nodeDownEvent(Event e);
		
		public NodeState parentNodeDownEvent(Event e);
		
	}
	
	public class ParentDownPendingState extends AbstractState {
		public ParentDownPendingState() {
			super(State.PARENT_DOWN_PENDING);
			setTimer(300000);
		}
	}

	public class FinalState extends  AbstractState {

		public FinalState() {
			super(State.FINAL, true);
		}

	}

	public class AbstractState implements NodeState {
		
		private boolean m_endState;
		private State m_state;
		
		public AbstractState(State state) {
			this(state, false);
		}
		
		public AbstractState(State state, boolean endState) {
			m_state = state;
			m_endState = endState;
		}
		
		public State getState() {
			return m_state;
		}

		public boolean isEndState() {
			return m_endState;
		}

		public NodeState nodeDownEvent(Event e) {
			throw new UnsupportedOperationException("AbstractState.nodeDownEvent not yet implemented.");
		}

		public NodeState parentNodeDownEvent(Event e) {
			throw new UnsupportedOperationException("AbstractState.parentNodeDownEvent not yet implemented.");
		}
		
		

	}

	public class InitialState extends AbstractState {

		public InitialState() {
			super(State.INITIAL);
		}

		@Override
		public NodeState nodeDownEvent(Event e) {
			return new ParentDownPendingState();
		}
		
	}

	private Integer m_nodeId;
	private Integer m_parentNodeId;
	private NodeState m_currentState;
	
	public NodeCorrelationStateMachine(Integer nodeId, Integer parentNodeId) {
		m_nodeId = nodeId;
		m_parentNodeId = parentNodeId;
		m_currentState = new InitialState();
	}
	

	public void setTimer(int millis) {
		throw new UnsupportedOperationException("NodeCorrelationStateMachine.setTimer not yet implemented.");
	}
	
	public void processTimerExpired() {
		
	}

	public void processEvent(Event e) {
		if (isCurrentNodeDown(e)) {
			m_currentState = m_currentState.nodeDownEvent(e);
		} else if (isParentNodeDown(e)) {
			m_currentState = m_currentState.parentNodeDownEvent(e);
		}
	}

	private boolean isNodeDownEvent(Event e) {
		return EventConstants.NODE_DOWN_EVENT_UEI.equals(e.getUei());
	}
	private boolean isCurrentNodeDown(Event e) {
		return eventNodeEquals(e, m_nodeId) && isNodeDownEvent(e);
	}
	private boolean isParentNodeDown(Event e) {
		return eventNodeEquals(e, m_parentNodeId) && isNodeDownEvent(e);
	}

	private boolean eventNodeEquals(Event e, Integer nodeId) {
		return (nodeId != null && e.hasNodeid() && nodeId.longValue() == e.getNodeid());
	}


	public State getState() {
		return m_currentState.getState();
	}

}
