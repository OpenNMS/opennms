#!/usr/bin/env groovy

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
