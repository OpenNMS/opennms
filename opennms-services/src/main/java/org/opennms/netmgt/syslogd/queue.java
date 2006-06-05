package org.opennms.netmgt.syslogd;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.apache.log4j.Category;
import org.opennms.netmgt.syslogd.QueueManager;
import org.opennms.netmgt.syslogd.SyslogHandler;
import org.opennms.netmgt.xml.event.Event;

//This is a standard FIFO queue class.

class Queue{
   /* //constant defining maximum queue size
    static final int MAXQUEUE = 100;
    = new byte[MAXQUEUE];
    int front, rear;
    
    Queue(){//constructor
      front = rear = 0;
    }//end constructor
    
    void enQueue(ConvertToEvent item){
      queue[rear] = item;
      rear = next(rear);
    }//end method enQueue
    
    ConvertToEvent deQueue(){
      ConvertToEvent temp = queue[front];
      front = next(front);
      return temp;
    }//end method deQueue

    boolean isEmpty(){
      return front == rear;
    }//end isEmpty
    
    boolean isFull(){
      return (next(rear) == front);
    }//end isFull
    
    int next(int index){
      return (index+1 < MAXQUEUE ? index+1 : 0);
    }//end next
*/
}//end Queue class
//=======================================================//

