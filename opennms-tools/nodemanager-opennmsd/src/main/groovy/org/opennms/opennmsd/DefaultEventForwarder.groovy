/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.opennmsd;


import org.opennms.opennmsd.AbstractEventForwarder
import groovy.xml.MarkupBuilder

class DefaultEventForwarder extends AbstractEventForwarder {
    
    String host = InetAddress.getLocalHost().hostName;
    String openNmsHost;
    
    protected void forwardEvents(List eventsToFoward) {
        
        System.err.println("openNmsHost is ${openNmsHost}")
        Socket socket = new Socket(openNmsHost, 5817);
        socket.outputStream.withWriter { out ->
        
          def xml = new MarkupBuilder(out);
          xml.log {
              events {
                  for(NNMEvent e in eventsToFoward) {
                      event {
                          uei("uei.opennms.org/internal/discovery/${e.name}")
                          source("opennmsd")
                          time(e.timeStamp)
                          host(m_host)
                          'interface'(e.sourceAddress)
                      }
                  }
              }
          }
        }
        
    }

}