//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
// Tab Size = 8
//
package org.opennms.netmgt.notifd;

import org.opennms.netmgt.notifd.*;
import org.opennms.core.fiber.PausableFiber;

/**
 * This interface defines a handler for a Notifd queue. As notifications
 * are parsed from events they will be put on a process queue and will
 * be handled by a class implementing this interface.
 *
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/>OpenNMS</a>
 *
 */
public interface NotifdQueueHandler extends Runnable, PausableFiber
{
        /**
         *
         */
        public void setQueueID(String queueID);
        
        /**
         *
         */
        public void setNoticeQueue(NoticeQueue queue);
        
        /**
         *
         */
        public void setInterval(String interval);
        
        /**
         *
         */
        public void processQueue();
}
