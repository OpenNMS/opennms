/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Phonebook;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;
import org.smslib.crypto.AESKey;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.modem.ModemGateway;
import org.smslib.modem.SerialModemGateway;
import org.smslib.test.TestGateway;

@RunWith(JUnit4TestRunner.class)
public class SmsLibTest {
    private static final Logger LOG = LoggerFactory.getLogger(SmsLibTest.class);

    @Configuration
    public static Option[] configuration(){
        return options(equinox(), provision(
                mavenBundle().groupId("org.ops4j.pax.logging").artifactId("pax-logging-service"),
                mavenBundle().groupId("org.ops4j.pax.logging").artifactId("pax-logging-api"),
                mavenBundle().groupId("org.opennms.smslib").artifactId("smslib").version("3.4.3-SNAPSHOT"),
                mavenBundle().groupId("commons-net").artifactId("commons-net"),
                mavenBundle().groupId("org.rxtx").artifactId("rxtx-osgi").version("2.2-pre2")
        ));
    }

    @Inject
    private BundleContext m_bundleContext;

    @Test
    @Ignore
    public void myFirstTest(){
        assertNotNull(m_bundleContext);
    }
    
    @Test
    public void testCommPortEnumerator() {
         Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();
         
         while(commPorts.hasMoreElements()) {
             CommPortIdentifier commPort = commPorts.nextElement();
             System.err.println(commPort.getName());
         }
    }

    @Test
    @Ignore
    public void testSendAMessage() throws Exception {
        Service srv = null;
        try {
            OutboundMessage msg;
            OutboundNotification outboundNotification = new OutboundNotification();
            System.out.println("Example: Send message from a serial gsm modem.");
            System.out.println(Library.getLibraryDescription());
            System.out.println("Version: " + Library.getLibraryVersion());
            srv = new Service();
            // SerialModemGateway gateway = new
            // SerialModemGateway("modem.com1", "COM1", 57600, "Nokia",
            // "6310i");
            AGateway gateway = createGateway();
            gateway.setInbound(true);
            gateway.setOutbound(true);
            srv.setOutboundNotification(outboundNotification);
            srv.addGateway(gateway);
            srv.startService();

            printGatewayInfo(gateway);

            // Send a message synchronously.
            msg = new OutboundMessage("+19198124984",
            "If you can read this then I got SMSLib to work from my mac!");
            // msg = new OutboundMessage("+19194125045",
            // "If you can read this then I got SMSLib to work from my mac!");
            srv.sendMessage(msg);
            System.out.println(msg);

        } finally {
            if (srv != null)
                srv.stopService();
        }
    }

    private void printGatewayInfo(AGateway gw) throws Exception {
        System.out.println();
        System.out.println(gw);
        if (gw instanceof ModemGateway) {
            ModemGateway gateway = (ModemGateway) gw;
            System.out.println();
            System.out.println("Modem Information:");
            System.out.println("  Manufacturer: " + gateway.getManufacturer());
            System.out.println("  Model: " + gateway.getModel());
            System.out.println("  Serial No: " + gateway.getSerialNo());
            System.out.println("  SIM IMSI: " + gateway.getImsi());
            System.out.println("  Signal Level: " + gateway.getSignalLevel()
                    + "%");
            System.out.println("  Battery Level: "
                    + gateway.getBatteryLevel() + "%");
            System.out.println();
        }
    }

    private AGateway createGateway() {
        File file = new File("/dev/tty.usbmodem2414");
        if (file.exists()) {
            return new SerialModemGateway("modem.com1",
                    "/dev/tty.usbmodem2414", 57600,
                    "SonyEricsson", "W760");
        } else {
            return new TestGateway("testGateway") {

                @Override
                public int getQueueSchedulingInterval() {
                    return 1000;
                }

            };
        }
    }

    // ReadMessages.java - Sample application.
    //
    // This application shows you the basic procedure needed for reading
    // SMS messages from your GSM modem, in synchronous mode.
    //
    // Operation description:
    // The application setup the necessary objects and connects to the phone.
    // As a first step, it reads all messages found in the phone.
    // Then, it goes to sleep, allowing the asynchronous callback handlers to
    // be called. Furthermore, for callback demonstration purposes, it
    // responds
    // to each received message with a "Got It!" reply.
    //
    // Tasks:
    // 1) Setup Service object.
    // 2) Setup one or more Gateway objects.
    // 3) Attach Gateway objects to Service object.
    // 4) Setup callback notifications.
    // 5) Run

    @Test
    @Ignore
    public void testReadMessage() throws Exception {
        Service srv = null;
        // Define a list which will hold the phonebook entries.
        Phonebook phonebook;

        // Define a list which will hold the read messages.
        List<InboundMessage> msgList;

        // Create the notification callback method for inbound & status report
        // messages.
        InboundNotification inboundNotification = new InboundNotification();

        // Create the notification callback method for inbound voice calls.
        CallNotification callNotification = new CallNotification();

        // Create the notification callback method for gateway statuses.
        GatewayStatusNotification statusNotification = new GatewayStatusNotification();

        try {
            System.out.println("Example: Read messages from a serial gsm modem.");
            System.out.println(Library.getLibraryDescription());
            System.out.println("Version: " + Library.getLibraryVersion());

            // Create new Service object - the parent of all and the main
            // interface
            // to you.
            srv = new Service();

            // Create the Gateway representing the serial GSM modem.
            AGateway gateway = createGateway();

            // Set the modem protocol to PDU (alternative is TEXT). PDU is the
            // default, anyway...
            gateway.setProtocol(Protocols.PDU);

            // Do we want the Gateway to be used for Inbound messages?
            gateway.setInbound(true);

            // Do we want the Gateway to be used for Outbound messages?
            gateway.setOutbound(true);

            // Set up the notification methods.
            srv.setInboundNotification(inboundNotification);
            srv.setCallNotification(callNotification);
            srv.setGatewayStatusNotification(statusNotification);

            // Add the Gateway to the Service object.
            srv.addGateway(gateway);

            // Similarly, you may define as many Gateway objects, representing
            // various GSM modems, add them in the Service object and control
            // all of them.

            // Start! (i.e. connect to all defined Gateways)
            srv.startService();

            // Printout some general information about the modem.
            printGatewayInfo(gateway);

            // In case you work with encrypted messages, its a good time to
            // declare your keys.
            // Create a new AES Key with a known key value.
            // Register it in KeyManager in order to keep it active. SMSLib
            // will then automatically
            // encrypt / decrypt all messages send to / received from this
            // number.
            srv.getKeyManager().registerKey("+306948494037", new AESKey(new SecretKeySpec("0011223344556677".getBytes(), "AES")));

            // Read Messages. The reading is done via the Service object and
            // affects all Gateway objects defined. This can also be more
            // directed to a specific
            // Gateway - look the JavaDocs for information on the Service
            // method calls.
            msgList = new ArrayList<InboundMessage>();
            srv.readMessages(msgList, MessageClasses.ALL);
            for (InboundMessage msg : msgList)
                System.out.println(msg);

            /*
             * // Read Phonebook. phonebook = new Phonebook();
             * System.out.println("TOTAL PHONEBOOK ENTRIES = " +
             * srv.readPhonebook(phonebook, gateway.getGatewayId())); // Print
             * out all contacts retrieved. for (Contact entry :
             * phonebook.getContacts()) System.out.println(entry); // Print
             * out contact stored on the SIM card. for (Contact entry :
             * phonebook.getContacts(Contact.ContactLocation.SIM_ENTRIES))
             * System.out.println(entry);
             */

            // Sleep now. Emulate real world situation and give a chance to
            // the notifications
            // methods to be called in the event of message or voice call
            // reception.

            Thread.sleep(20000);

        } catch (Throwable e) {
            LOG.warn("failed to read message");
        } finally {
            if (srv != null) srv.stopService();
        }
    }

    public class OutboundNotification implements IOutboundMessageNotification {
        public void process(String gatewayId, OutboundMessage msg) {
            System.out.println("Outbound handler called from Gateway: "
                    + gatewayId);
            System.out.println(msg);
        }
    }

    public class InboundNotification implements IInboundMessageNotification {
        public void process(String gatewayId, MessageTypes msgType,
                InboundMessage msg) {
            if (msgType == MessageTypes.INBOUND)
                System.out.println(">>> New Inbound message detected from Gateway: "
                        + gatewayId);
            else if (msgType == MessageTypes.STATUSREPORT)
                System.out.println(">>> New Inbound Status Report message detected from Gateway: "
                        + gatewayId);
            System.out.println(msg);
            try {
                // Uncomment following line if you wish to delete the message
                // upon arrival.
                // ReadMessages.this.srv.deleteMessage(msg);
            } catch (Throwable e) {
                LOG.error("Oops!!! Something gone bad...");
            }
        }
    }

    public class CallNotification implements ICallNotification {
        public void process(String gatewayId, String callerId) {
            System.out.println(">>> New call detected from Gateway: "
                    + gatewayId + " : " + callerId);
        }
    }

    public class GatewayStatusNotification implements
    IGatewayStatusNotification {
        public void process(String gatewayId, GatewayStatuses oldStatus,
                GatewayStatuses newStatus) {
            System.out.println(">>> Gateway Status change for " + gatewayId
                    + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
        }
    }

}
