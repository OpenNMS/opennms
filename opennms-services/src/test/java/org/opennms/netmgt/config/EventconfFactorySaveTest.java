package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.test.FileAnticipator;

import junit.framework.TestCase;

public class EventconfFactorySaveTest extends TestCase {
    private static final String knownUEI1="uei.opennms.org/internal/capsd/snmpConflictsWithDb";
    private static final String knownSubfileUEI1="uei.opennms.org/IETF/Bridge/traps/newRoot";
    
    private static final String newUEI="uei.opennms.org/custom/addedUEI";
    private static final String newEventLabel="A New Event which is added to the eventconf";
    private static final String newDescr="A slightly longer descriptive bit of text";
    private static final String newDest="logndisplay";
    private static final String newContent="Test message";
    private static final String newSeverity="Warning";
    
    private FileAnticipator m_fa;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_fa = new FileAnticipator();

        
        //Create a temporary directory
        String tempHome=m_fa.getTempDir().getAbsolutePath();
        String origHome="src/test/resources";
        m_fa.expecting(new File(tempHome), "etc");
        m_fa.expecting(new File(tempHome), "etc/events");
        createTempCopy(m_fa, origHome, tempHome, "etc/eventconf.xml");
        createTempCopy(m_fa, origHome, tempHome, "etc/events/Standard.events.xml");
        createTempCopy(m_fa, origHome, tempHome, "etc/events/Syslog.test.events.xml");

        //Change the opennms.home and load the EventconfFactory from the new temporary copy
        System.setProperty("opennms.home", tempHome);
        EventconfFactory.reinit();

    }

    @Override
    protected void tearDown() throws Exception {
        m_fa.deleteExpected();
        m_fa.tearDown();
        super.tearDown();
    }
    
    private void copyFile(File source, File dest) throws Exception {
        File destDir=dest.getParentFile();
        if(!destDir.exists()) {
            destDir.mkdirs();
        }
        
        FileInputStream input=new FileInputStream(source);
        FileOutputStream output= new FileOutputStream(dest);
        byte[] iobuff = new byte[1024];
        int bytes;
        while ( (bytes = input.read( iobuff )) != -1 ) {
            output.write( iobuff, 0, bytes );
        }
        input.close();
        output.close();
    }
    private void copyFile(String source, String dest) throws Exception {
        copyFile(new File(source), new File(dest));
    }
   
    /**
     * Copys sourceDir/relativeFilePath to destDir/relativeFilePath
     * 
     * @param sourceDir
     * @param destDir
     * @param relativeFilePath
     */
    private void createTempCopy(FileAnticipator fa, String sourceDir, String destDir, String relativeFilePath) throws Exception {
        copyFile(sourceDir+File.separator+relativeFilePath, destDir+File.separator+relativeFilePath);
        fa.expecting(new File(destDir), relativeFilePath);
               
    }
    
    public void testSave() throws Exception {
        String newUEI1="uei.opennms.org/custom/newTestUEI1";
        String newUEI2="uei.opennms.org/custom/newTestUEI2";
        
        //Now do the test
        { 
            EventconfFactory.getInstance().reload();
            List<Event> events=EventconfFactory.getInstance().getEvents(knownUEI1);
            Event event=events.get(0);
            event.setUei(newUEI1);
        }
        
        EventconfFactory.getInstance().saveCurrent();
        
        EventconfFactory.getInstance().reload(); //The reload might happen as part of the saveCurrent, but is not assured.  We do so here to be certain 
        { 
            List<Event> events=EventconfFactory.getInstance().getEvents(knownUEI1);
            assertNull("Shouldn't be any events by that uei", events);
            
            events=EventconfFactory.getInstance().getEvents(newUEI1);
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event event=events.get(0);
            assertEquals("Should be the new UEI", newUEI1, event.getUei());
        }
       
        //Check that we can change and save a UEI in a sub file
        { 
            List<Event> events=EventconfFactory.getInstance().getEvents(knownSubfileUEI1);
            Event event=events.get(0);
            event.setUei(newUEI2);
        }
        
        EventconfFactory.getInstance().saveCurrent();
        
        EventconfFactory.getInstance().reload(); //The reload might happen as part of the saveCurrent, but is not assured.  We do so here to be certain 
        { 
            List<Event> events=EventconfFactory.getInstance().getEvents(knownSubfileUEI1);
            assertNull("Shouldn't be any events by that uei", events);
            
            events=EventconfFactory.getInstance().getEvents(newUEI2);
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event event=events.get(0);
            assertEquals("Should be the new UEI", newUEI2, event.getUei());
        }

    }

    private Event getAddableEvent() {
        Event event=new Event();
        event.setUei(newUEI);
        event.setEventLabel(newEventLabel);
        event.setDescr(newDescr);
        Logmsg logmsg=new Logmsg();
        logmsg.setDest(newDest);
        logmsg.setContent(newContent);
        event.setLogmsg(logmsg);
        event.setSeverity(newSeverity);
        return event;
    }
    
    private void checkAddableEvent(Event event) {
        assertEquals("Should be the new UEI", newUEI, event.getUei());
        assertEquals(newEventLabel, event.getEventLabel());
        assertEquals(newDescr, event.getDescr());
        assertEquals(newDest, event.getLogmsg().getDest());
        assertEquals(newContent, event.getLogmsg().getContent());
        assertEquals(newSeverity, event.getSeverity()); 
    }
    
    public void testAddEvent() {
        Event event=getAddableEvent();
        
        //The tested event
        EventconfFactory.getInstance().addEvent(event);
        
        {
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event fetchedEvent=events.get(0);
            checkAddableEvent(fetchedEvent);
        }
        
        try {
            EventconfFactory.getInstance().saveCurrent();
            EventconfFactory.getInstance().reload();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Shouldn't throw exception while saving  and reloading factory: "+e.getMessage());
        }
        
        {
            //Check that the new Event is still there
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event fetchedEvent=events.get(0);
            checkAddableEvent(fetchedEvent);
        }
    }
    
    /**
     * Test adding and event to a specific file
     *
     */
    public void testAddEventToProgrammaticStore() {
        Event event=getAddableEvent();
        
        EventconfFactory.getInstance().addEventToProgrammaticStore(event);
        
        //Check that the new Event is still there
        {
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
  
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event fetchedEvent=events.get(0);
            checkAddableEvent(fetchedEvent);
        }
        
        try {
            EventconfFactory.getInstance().saveCurrent();
            EventconfFactory.getInstance().reload();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Saving/reloading should not have caused an execption ");
        }
        
        //We are expecting this new file to be there - if it's not, that's an issue
        m_fa.expecting(new File(m_fa.getTempDir().getAbsolutePath()+File.separator+"etc"+File.separator+"events"),"programmatic.events.xml");
        //Check again after the reload
        {
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
  
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event fetchedEvent=events.get(0);
            checkAddableEvent(fetchedEvent);
        }
       
    }
    
    public void testRemoveEventToProgrammaticStore() {
        Event event=getAddableEvent();
        
        EventconfFactory.getInstance().addEventToProgrammaticStore(event);
        {
            //Check that the new Event is still there
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
            assertNotNull("Should be at least one event", events);
            assertEquals("Should be only one event", 1, events.size());
            Event fetchedEvent=events.get(0);
            checkAddableEvent(fetchedEvent);     
        }
        
        //Check before the save/reload
        assertTrue("remove should have returned true", EventconfFactory.getInstance().removeEventFromProgrammaticStore(event));
        {
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
            assertNull(events);
        }

        try {
            EventconfFactory.getInstance().saveCurrent();
            EventconfFactory.getInstance().reload();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Saving/reloading should not have caused an execption ");
        }

        //Should get a "false" when the event is already missing
        assertFalse("remove should have returned false",EventconfFactory.getInstance().removeEventFromProgrammaticStore(event));
        //Check again after save/reload

        {
            List<Event> events=EventconfFactory.getInstance().getEvents(newUEI);
            assertNull(events);
        }

    }   
}
