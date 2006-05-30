package org.opennms.netmgt.syslogd;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;

import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.apache.log4j.Category;
import org.opennms.netmgt.syslogd.QueueManager;
import org.opennms.netmgt.syslogd.SyslogHandler;

public class SyslogConnection implements Runnable {

private DatagramPacket _packet;
private FifoQueue _queue;
private String m_logPrefix;

private static final String LOG4J_CATEGORY = "OpenNMS.Syslogd";



public SyslogConnection(DatagramPacket packet) {
_packet = packet;
m_logPrefix = LOG4J_CATEGORY;
}

public void run() { 
    {
        
        ThreadCategory.setPrefix(m_logPrefix);
        Category log = ThreadCategory.getInstance(getClass());
        
        ConvertToEvent re =null;
        try {
            re = ConvertToEvent.make(_packet.getAddress(),_packet.getPort(),_packet.getData(),_packet.getLength());
        } catch (UnsupportedEncodingException e1) {
            log.debug("Failure to convert package");
        }
         
        log.debug("Sending received packet to the queue");
   
           
            SyslogHandler.queueManager.putInQueue(re);
            //delay a random period of time
            try{ 
              Thread.sleep(
                      (int)(Math.random()*100));
            }catch(InterruptedException e){};        
          
            }
            // We just add to the queue so we do not notify
            // in the middle of stuff.
        
            

}
    
    void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }
    



}
//END OF CLASS
