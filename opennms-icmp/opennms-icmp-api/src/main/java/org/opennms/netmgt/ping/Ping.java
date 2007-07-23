package org.opennms.netmgt.ping;

/*
 * $Id: Ping.java 7736 2007-03-07 02:28:01Z dfs $
 *
 * Copyright 2004-2005 Daniel F. Savarese
 * Contact Information: http://www.savarese.org/contact.html
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.savarese.org/software/ApacheLicense-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.*;

import org.savarese.vserv.tcpip.*;
import org.savarese.rocksaw.net.RawSocket;

/**
 * <p>The Ping class is a simple demo showing how you can send
 * ICMP echo requests and receive echo replies using raw sockets.</p>
 * 
 * @author <a href="http://www.savarese.org/">Daniel F. Savarese</a>
 */

public class Ping {

  public static interface EchoReplyListener {
    public void notifyEchoReply(ICMPEchoPacket packet,
                                byte[] data, int dataOffset);
  }

  private int TIMEOUT = 10000;

  private RawSocket socket;
  private ICMPEchoPacket sendPacket, recvPacket;
  private int offset, length, dataOffset;
  private byte[] sendData, recvData;
  private int sequence;
  private int identifier;
  private EchoReplyListener listener;

  public Ping(int id) throws IOException {
    sequence   = 0;
    identifier = id;
    setEchoReplyListener(null);

    sendPacket = new ICMPEchoPacket(1);
    recvPacket = new ICMPEchoPacket(1);
    sendData = new byte[84];
    recvData = new byte[84];

    sendPacket.setData(sendData);
    recvPacket.setData(recvData);
    sendPacket.setIPHeaderLength(5);
    recvPacket.setIPHeaderLength(5);
    sendPacket.setICMPDataByteLength(56);
    recvPacket.setICMPDataByteLength(56);

    offset     = sendPacket.getIPHeaderByteLength();
    dataOffset = offset + sendPacket.getICMPHeaderByteLength();
    length     = sendPacket.getICMPPacketByteLength();

    socket = new RawSocket();
    socket.open(RawSocket.PF_INET, RawSocket.getProtocolByName("icmp"));

    try {
      socket.setSendTimeout(TIMEOUT);
      socket.setReceiveTimeout(TIMEOUT);
    } catch(java.net.SocketException se) {
      socket.setUseSelectTimeout(true);
      socket.setSendTimeout(TIMEOUT);
      socket.setReceiveTimeout(TIMEOUT);
    }
  }


  public void setEchoReplyListener(EchoReplyListener l) {
    listener = l;
  }


  /**
   * Closes the raw socket opened by the constructor.  After calling
   * this method, the object cannot be used.
   */
  public void close() throws IOException {
    socket.close();
  }


  public void sendEchoRequest(InetAddress host) throws IOException {
    sendPacket.setType(ICMPPacket.TYPE_ECHO_REQUEST);
    sendPacket.setCode(0);
    sendPacket.setIdentifier(identifier);
    sendPacket.setSequenceNumber(sequence++);

    OctetConverter.longToOctets(System.nanoTime(), sendData, dataOffset);
    sendPacket.computeICMPChecksum();

    socket.write(host, sendData, offset, length);
  }
  

  public void receive(InetAddress host) throws IOException {
    socket.read(host, recvData);
  }


  public void receiveEchoReply(InetAddress host) throws IOException {
    do {
      receive(host);
    } while(recvPacket.getType() != ICMPPacket.TYPE_ECHO_REPLY ||
            recvPacket.getIdentifier() != identifier);

    if(listener != null)
      listener.notifyEchoReply(recvPacket, recvData, dataOffset);
  }


  /**
   * Issues a synchronous ping.
   *
   * @param host The host to ping.
   * @return The round trip time in nanoseconds.
   */
  public long ping(InetAddress host) throws IOException {
    sendEchoRequest(host);
    receiveEchoReply(host);

    long end   = System.nanoTime();
    long start = OctetConverter.octetsToLong(recvData, dataOffset);

    return (end - start);
  }


  /**
   * @return The number of bytes in the data portion of the ICMP ping request
   * packet.
   */
  public int getRequestDataLength() {
    return sendPacket.getICMPDataByteLength();
  }


  /** @return The number of bytes in the entire IP ping request packet. */
  public int getRequestPacketLength() {
    return sendPacket.getIPPacketLength();
  }


  public static final void main(String[] args) throws Exception {

    if(args.length < 1 || args.length > 2) {
      System.err.println("usage: Ping host [count]");
      System.exit(1);
    }

    final ScheduledThreadPoolExecutor executor =
      new ScheduledThreadPoolExecutor(2);

    try{
      final InetAddress address = InetAddress.getByName(args[0]);
      final String hostname = address.getCanonicalHostName();
      final String hostaddr = address.getHostAddress();
      final int count;

      if(args.length == 2)
        count = Integer.parseInt(args[1]);
      else
        count = 5;

      // Ping programs usually use the process ID for the identifier,
      // but we can't get it and this is only a demo.
      final Ping ping = new Ping(65535);

      ping.setEchoReplyListener(new EchoReplyListener() {
          public void notifyEchoReply(ICMPEchoPacket packet,
                                      byte[] data, int dataOffset) {
            long end   = System.nanoTime();
            long start = OctetConverter.octetsToLong(data, dataOffset);
            double rtt = (double)(end - start) / 1e6;
            System.out.println(packet.getICMPPacketByteLength() + 
                               " bytes from " + hostname + " (" + hostaddr +
                               "): icmp_seq=" + packet.getSequenceNumber() +
                               " ttl=" + packet.getTTL() +
                               " time=" + rtt + " ms");
          }
        });

      System.out.println("PING " + hostname + " (" + hostaddr + ") " +
                         ping.getRequestDataLength() + "(" +
                         ping.getRequestPacketLength() + ") bytes of data).");

      final CountDownLatch latch = new CountDownLatch(1);

      executor.scheduleAtFixedRate(new Runnable() {
          int counter = count;

          public void run() {
            try {
              if(counter > 0) {
                ping.sendEchoRequest(address);
                if(counter == count)
                  latch.countDown();
                --counter;
              } else
                executor.shutdown();
            } catch(IOException ioe) {
              ioe.printStackTrace();
            }
          }
        }, 0, 1, TimeUnit.SECONDS);

      // We wait for first ping to be sent because Windows times out
      // with WSAETIMEDOUT if echo request hasn't been sent first.
      // POSIX does the right thing and just blocks on the first receive.
      latch.await();

      for(int i = 0; i < count; ++i)
        ping.receiveEchoReply(address);

      ping.close();
    } catch(Exception e) {
      executor.shutdown();
      e.printStackTrace();
    }
  }

}
