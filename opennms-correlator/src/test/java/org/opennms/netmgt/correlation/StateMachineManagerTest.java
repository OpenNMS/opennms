package org.opennms.netmgt.correlation;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class StateMachineManagerTest extends TestCase {
	
	private List<Object> m_mocks = new ArrayList<Object>();
	private StateMachineManager m_mgr;
	private MachineLifetimeListener m_listener;
	private StateMachine m_stateMachine;

	@Override
	protected void setUp() throws Exception {
		
		m_listener = createMock(MachineLifetimeListener.class);
		
		m_stateMachine = createMock(StateMachine.class);

		m_mgr = new StateMachineManager();		
		
	}

	public void testMachineCreated() {

		m_listener.machineCreated(EasyMock.eq(created()));
		m_listener.machineCompleted(EasyMock.eq(completed()));

		m_mgr.addTransientMachineLifetimeListener(m_listener);
		
		replayMocks();
		
		m_mgr.setMachine(1, m_stateMachine);
		
		assertSame(m_stateMachine, m_mgr.getMachine(1));
		
		m_mgr.removeMachine(1);
		
		verifyMocks();
		
	}
	
	public void testSoftReferences() {
		
		byte[] bytes = new byte[1024*1024*32];
		SoftReference<byte[]> ref = new SoftReference<byte[]>(bytes);
		bytes = null;
		assertNotNull("expect reference to still exist", ref.get());
		bytes = new byte[1024*1024*32];
		assertNull("expect reference to be gc'd", ref.get());
		
	}
	
	class BigListener implements MachineLifetimeListener {
		
		byte[] bytes = new byte[1024*1024*32];
		
		public boolean[] m_flags = new boolean[2];
		
		BigListener(boolean[] flags) {
			assertTrue("need to have at least two boolean in the flags", flags.length >= 2);
			m_flags = flags;
			reset();
		}

		public void reset() {
			m_flags[0] = false;
			m_flags[1] = false;
		}
		
		public void completed() {
			m_flags[1] = true;
		}
		
		public void created() {
			m_flags[0] = true;
		}

		public void machineCompleted(MachineLifetimeEvent event) {
			completed();
		}

		public void machineCreated(MachineLifetimeEvent event) {
			created();
		}
		
	}
	
	public void testGarbageCollectListeners() {
		boolean[] flags = new boolean[2];
		BigListener listener = new BigListener(flags);
		
		m_mgr.addTransientMachineLifetimeListener(listener);
		listener = null; // so it can be garbage collected
		
		m_mgr.setMachine(1, m_stateMachine);

		try {
			byte[] temp = new byte[1024*1024*32];
			temp[0] = 1;
		} catch (OutOfMemoryError err) {
			fail("Memory should have been freed up by BigListener");
		}

		m_mgr.removeMachine(1);

		assertTrue("Expected the create event", flags[0]);
		assertFalse("Shouldn't get this because it should have been gc'd", flags[1]);

		
		
	}

	private MachineLifetimeEvent created() {
		return new MachineLifetimeEvent(m_mgr, MachineLifetimeEvent.Type.MACHINE_CREATED, m_stateMachine);
	}
	
	private MachineLifetimeEvent completed() {
		return new MachineLifetimeEvent(m_mgr, MachineLifetimeEvent.Type.MACHINE_COMPLETED, m_stateMachine);
	}

	protected <T> T createMock(Class<T> toMock) {
		T mock = EasyMock.createMock(toMock);
		m_mocks.add(mock);
		return mock;
	}
	
	protected void replayMocks() {
		EasyMock.replay(m_mocks.toArray());
	}
	
	protected void verifyMocks() {
		EasyMock.verify(m_mocks.toArray());
	}
	




}
