package org.opennms.netmgt.correlation;

import junit.framework.TestCase;

public class NodeCorrelationStateMachineTest extends TestCase {
	
	public void testProcessEvent() {
		NodeCorrelationStateMachine stateMachine = new NodeCorrelationStateMachine(1, 2);
		
		assertEquals(NodeCorrelationStateMachine.State.INITIAL, stateMachine.getState());
		
		
	}

}

// inputs to the node state machine
// nodeUpEvent
// nodeDownEvent
// timerExpired
// parentNodeStateChanged

// create state machine on nodeDown event
// -- state == INITIAL after creation
//
// 
//
// State       event             condition            NextState   action
// INITIAL     -                 parentState != UP    IMPACTED    send impacted event
// INITIAL     parent == UP -

// engine behavior needed
// -- need to create a state machine based on an event
// -- need to a way to pass state change events to other interested state machines
// ---- a state change needs to include the creation of the machine
// -- need a way to specify interest
// -- need a way to remove interest
// -- need a timer mechanism that is associated with a state of a given machine
// 
// ideas:
// -- use proxies to represent the parent state machine so we can use it as though its
//    there even though it isn't yet
// -- need a way to 'get' the real machine when it gets created to 'fill in the proxy'
// -- need a way to register for state change events even though the machine isn't
//    really there
// 