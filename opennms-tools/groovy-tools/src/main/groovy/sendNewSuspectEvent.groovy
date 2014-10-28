#!/usr/bin/env groovy

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

import groovy.xml.MarkupBuilder;

class sendEvent {

  static void main(args) {

      if (args.length < 1) {
            System.err.println("Usgage : <new suspect ip> [<hostname of opennms server>]")
            return
      }

     def ipAddr = args[0];

      def openNmsHost = "localhost";
      if (args.length > 1) {
         openNmsHost = args[1];
      }

      

      Socket socket = new Socket(openNmsHost, 5817);
      socket.outputStream.withWriter { out ->
//              System.out.withWriter { out ->
      
              def xml = new MarkupBuilder(out);
              xml.log {
                  events {
                      event {
                          uei("uei.opennms.org/internal/discovery/newSuspect")
                          source("sendEvent-groovy")
                          time(new Date())
                          host(InetAddress.getLocalHost().hostName)
                          'interface'(ipAddr)
                      }
                  }
              }

      
     }
      
  }

}
