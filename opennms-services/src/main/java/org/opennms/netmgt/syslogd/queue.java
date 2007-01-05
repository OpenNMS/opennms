//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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

