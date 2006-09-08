package org.opennms.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class ThrowableAnticipatorTest extends TestCase {
    private ThrowableAnticipator m_anticipator;
    private Throwable m_throwable = new Throwable("our test throwable");

    public ThrowableAnticipatorTest() {
    }

    protected void setUp() throws Exception {
        m_anticipator = new ThrowableAnticipator();
    }

    protected void tearDown() throws Exception {
        m_anticipator.verifyAnticipated();
    }
    
    public void testConstructor() throws Exception {
        setUp();
    }
    
    public void testAnticipate() {
        m_anticipator.anticipate(m_throwable);
        m_anticipator.reset();
    }
    
    public void testThrowableReceivedVoid() {
        try {
            m_anticipator.throwableReceived(null);
        } catch (IllegalArgumentException e) {
            if ("Throwable must not be null".equals(e.getMessage())) {
                return; // This is what we were expecting
            } else {
                fail("Received unexpected IllegalArgumentException: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected IllegalArgumentException.");
    }
    
    public void testThrowableReceivedVoidMessage() {
        try {
            m_anticipator.throwableReceived(new Exception());
        } catch (AssertionFailedError e) {
            if ("Received an unexpected Exception: java.lang.Exception".equals(e.getMessage())) {
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }
    
    public void testThrowableReceivedIgnoreMessage() {
        m_anticipator.anticipate(new Exception(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_anticipator.throwableReceived(new Exception("something random"));
        } catch (AssertionFailedError e) {
            fail("Received unexpected AssertionFailedError: " + e);
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
    }
    public void testThrowableReceived() {
        m_anticipator.anticipate(m_throwable);
        m_anticipator.throwableReceived(m_throwable);
    }
    
    public void testThrowableReceivedNotAnticipated() {
        try {
            m_anticipator.throwableReceived(m_throwable);
        } catch (AssertionFailedError e) {
            if ("Received an unexpected Exception: java.lang.Throwable: our test throwable".equals(e.getMessage())) {
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }
    
    public void testThrowableReceivedNotAnticipatedCheckCause() {
        try {
            m_anticipator.throwableReceived(m_throwable);
        } catch (AssertionFailedError e) {
            if ("Received an unexpected Exception: java.lang.Throwable: our test throwable".equals(e.getMessage())) {
                if (e.getCause() == null) {
                    fail("No cause throwable on received exception.");
                }
                assertEquals(m_throwable.getMessage(), e.getCause().getMessage());
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }
    
    public void testSetFailFast() {
        assertTrue(m_anticipator.isFailFast());
        m_anticipator.setFailFast(false);
        assertFalse(m_anticipator.isFailFast());
    }
    
    public void testSetFailFastWithUnanticipated() {
        assertTrue(m_anticipator.isFailFast());
        m_anticipator.setFailFast(false);
        m_anticipator.throwableReceived(new Throwable("this should be unanticipated"));
        
        try {
            m_anticipator.setFailFast(true);
        } catch (AssertionFailedError e) {
            if (e.getMessage().startsWith("failFast is being changed from false to true and unanticipated exceptions have been received:")) {
                m_anticipator.reset();
                return; // This is what we were expecting
            } else {
                fail("Received unexpected AssertionFailedError: " + e);
            }
        } catch (Throwable t) {
            fail("Received unexpected Throwable: " + t);
        }
        
        fail("Did not receive expected AssertionFailedError.");
    }

    public void testReset() {
        m_anticipator.setFailFast(false);
        m_anticipator.anticipate(m_throwable);
        m_anticipator.anticipate(new Throwable("something else"));
        m_anticipator.throwableReceived(m_throwable);
        m_anticipator.throwableReceived(new Throwable("yet another thing"));
        m_anticipator.reset();
    }
    
    public void testVerifyAnticipated() {
        m_anticipator.verifyAnticipated();
    }
    
}
