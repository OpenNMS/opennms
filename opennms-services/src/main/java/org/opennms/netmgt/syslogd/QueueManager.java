package org.opennms.netmgt.syslogd;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.queue.FifoQueueImpl;


public class QueueManager {
    
    FifoQueue m_backlogQ =  new FifoQueueImpl();
    
    ConvertToEvent ret ;
    
    public synchronized void putInQueue(ConvertToEvent re){
        //This synchronized method places a message in the queue
       // Category log = ThreadCategory.getInstance(this.getClass());   
        
        ret =re ;
        
        try {
            m_backlogQ.add(ret);

        } catch (FifoQueueException e) {
            //log.debug("Caught an exception adding to queue");
        } catch (InterruptedException e) {
            //Error handling by ignoring the problem.
        }
        //wake up getByteFromQueue() if it has invoked wait().
        notify();
      }//end method putByteInQueue()
      //-----------------------------------------------------//

      public synchronized ConvertToEvent getFromQueue(){
        //This synchronized method removes a message from the queue 
          Category log = ThreadCategory.getInstance(this.getClass());

        try{
          while(m_backlogQ.isEmpty()){  
            wait();
          }// end while
        }catch (InterruptedException E){
          System.out.println("InterruptedException: " + E);
        }//end catch block
        
        //get the byte from the queue
      
        try {
            ret = (ConvertToEvent) m_backlogQ.remove();
        } catch (FifoQueueException e) {
            log.debug("FifoQueue exception " + e);
        } catch (InterruptedException e) {
            log.debug("Interrupted exception " +e);
        }

        //wake up putByteInQueue() if it has invoked wait().
        notify();
        return ret;
      }//end getByteFromQueue()

}
