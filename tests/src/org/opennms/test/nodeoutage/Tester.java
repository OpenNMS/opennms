package org.opennms.test.nodeoutage;

import org.opennms.test.netsim.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Tester
{
    private static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance("OpenNMS.Pollers");

    public static void main(String[] args) throws UnknownHostException, java.io.IOException
    {
        if (args.length < 4)
        {
            System.err.println("Usage: <cmd> <interval> <controllerIp> <int1Ip> <int2Ip>");
        }

        int interval = Integer.parseInt(args[0]);

        log.info("outagesim: Starting the node outage simulator; " + interval + " pause between states.");

        InetAddress controllerIp = InetAddress.getByName(args[1]);
        log.debug("outagesim: Getting a SimulatorController for host '" + controllerIp + "'");
        SimulatorController cont = new SimulatorController(controllerIp);

        SimulatedNode node = new SimulatedNode("testnode");
        cont.addNode(node);

        SimulatedInterface int1 = new SimulatedInterface(InetAddress.getByName(args[2]));
        SimulatedInterface int2 = new SimulatedInterface(InetAddress.getByName(args[3]));
        node.addInterface(int1);
        node.addInterface(int2);

        SimulatedService httpd1 = new SimulatedService("httpd", "httpd");
        SimulatedService pg1 = new SimulatedService("postgresql", "postgresql");
        SimulatedService icmp1 = new SimulatedService("icmp", "icmp");
        int1.addService(httpd1);
        int1.addService(pg1);
        int1.addService(icmp1);

        SimulatedService httpd2 = new SimulatedService("httpd", "httpd");
        SimulatedService pg2 = new SimulatedService("postgresql", "postgresql");
        SimulatedService icmp2 = new SimulatedService("icmp", "icmp");
        int2.addService(httpd2);
        int2.addService(pg2);
        int2.addService(icmp2);

        ChineseMenu stateMenu = new ChineseMenu(
                                new String[] {"httpd1", "pg1", "icmp1", "httpd2", "pg2", "icmp2"}
                            );
        SimulatedService[] service = new SimulatedService[] {
                                        httpd1,
                                        pg1,
                                        icmp1,
                                        httpd2,
                                        pg2,
                                        icmp2
                                     };

        boolean[] initialState = new boolean[] {true, true, true, true, true, true};
        log.debug("outagesim: Creating inital state - httpd1,pg1,icmp1,httpd2,pg2,icmp2 up");
        createState(initialState, service);

        for (int i = 0; i < stateMenu.numberOfPossibilities(); i++)
        {
            log.debug("outagesim: waiting " + interval  + " milliseconds...");
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.warn("Interrupted: " + e.getMessage());
            }

            log.debug("outagesim: Creating state #" + i + ": " + stateMenu.getPossibilityAsString(i));
            createState(stateMenu.getPossibilityAt(i), service);

        }
        

        log.info("outagesim: Node outage simulator complete");
    }

    private static void createState(boolean[] on, SimulatedService[] service)
    {
        if (!(on.length == service.length))
            throw new IllegalArgumentException("Number of on/off positions and number of services do not match.");

        for (int i = 0; i < on.length; i++)
        {
            if (on[i]) {
                if (service[i].isDown())
                {
                    log.debug("outagesim: Starting service " + service[i].getName());
                    service[i].start();
                }
            } else {
                if (service[i].isUp())
                {
                    log.debug("outagesim: Stopping service " + service[i].getName());
                    service[i].stop();
                }
            }
        }
    }
}
