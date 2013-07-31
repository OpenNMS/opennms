/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tl1d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.opennms.netmgt.config.tl1d.Tl1Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Implementation of the Tl1Client API.
 *
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public class Tl1ClientImpl implements Tl1Client {
	
	private static final Logger LOG = LoggerFactory.getLogger(Tl1ClientImpl.class);

    String m_host;
    int m_port;
    private volatile boolean m_started = false;

    private Socket m_tl1Socket;
    private Thread m_socketReader;
    private BlockingQueue<Tl1AutonomousMessage> m_tl1Queue;
    private BufferedReader m_reader;
    private TimeoutSleeper m_sleeper;
    private Tl1AutonomousMessageProcessor m_messageProcessor;
    //private long m_reconnectionDelay = 30000;
    private long m_reconnectionDelay;  //see configuration xsd for default and set by Tl1d after instantiation
    private int m_reconnectAttempts = 0;
    
    /**
     * <p>Constructor for Tl1ClientImpl.</p>
     */
    public Tl1ClientImpl() {
    }
    
    /**
     * <p>Constructor for Tl1ClientImpl.</p>
     *
     * @param queue a {@link java.util.concurrent.BlockingQueue} object.
     * @param element a {@link org.opennms.netmgt.config.tl1d.Tl1Element} object.
     * @throws java.lang.InstantiationException if any.
     * @throws java.lang.IllegalAccessException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public Tl1ClientImpl(BlockingQueue<Tl1AutonomousMessage> queue, Tl1Element element) 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        
        m_host = element.getHost();
        m_port = element.getPort();
        
        m_tl1Queue = queue;
        m_messageProcessor = (Tl1AutonomousMessageProcessor) Class.forName(element.getTl1MessageParser()).newInstance();
        m_reconnectionDelay = element.getReconnectDelay();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#start()
     */
    /**
     * <p>start</p>
     */
    @Override
    public void start() {
        LOG.info("start: TL1 client: {}:{}", m_host, String.valueOf(m_port));
        LOG.info("start:Connection delay = {}", m_reconnectionDelay);
        setStarted(true);

        m_socketReader = new Thread("TL1-Socket-Reader") {

            @Override
            public void run() {
                readMessages();
            }

        };

        m_socketReader.start();
        LOG.info("Started TL1 client: {}:{}", m_host, String.valueOf(m_port));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#stop()
     */
    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        LOG.info("Stopping TL1 client: {}:{}", m_host, String.valueOf(m_port));
        setStarted(false);
        
        //waiting a second or so for the reader thread to clean up the socket and shut
        //itself down.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error("stop: {}", e);
        }
    }

    private BufferedReader getReader() throws InterruptedException {
        if (m_reader == null) {
            m_reader = createReader();
        }
        return m_reader;
    }

    private BufferedReader createReader() throws InterruptedException {
        BufferedReader reader;
        
        while (isStarted()) {
            
            try {
                m_tl1Socket = new Socket(m_host, m_port);
                reader = new BufferedReader(new InputStreamReader(m_tl1Socket.getInputStream()));
                resetTimeout();
                return reader;
                
            } catch (IOException e) {
                LOG.error("TL1 Connection Failed to {}:{}", m_host, m_port);
                LOG.debug(e.getMessage());
                
                waitUntilNextConnectTime();
            } 
        }
        
        return null;
    }

    private void resetTimeout() {
        LOG.debug("resetTimeout: Resetting timeout Thread");
        m_reconnectAttempts = 0;
        m_sleeper = null;
    }

    private void waitUntilNextConnectTime() throws InterruptedException {
        LOG.debug("waitUntilNextConnectTime: current connection attempts: {}", m_reconnectAttempts);
        
        if (isStarted()) {
            
            if (m_sleeper == null) {
                m_sleeper = new TimeoutSleeper();
            }
            
            m_reconnectAttempts++;
            /* If the system is not responding, we want to wait longer and longer for the retry */
            long waitTime = computeWait();
            LOG.info("waitUntilNextConnectTime: Waiting {} ms......", waitTime);
            
            m_sleeper.sleep(waitTime);
        }
    }

    private long computeWait() {
        long waitTime = m_reconnectionDelay;
        
        if (m_reconnectAttempts > 5) {
            waitTime = m_reconnectionDelay * 5;
        } else if (m_reconnectAttempts > 10) {
            waitTime = m_reconnectionDelay * 10;
        }
        return waitTime;
    }

    private void readMessages() {
        StringBuilder rawMessageBuilder = new StringBuilder();
        
        LOG.info("readMessages: Begin reading off socket...");
        
        while (isStarted()) {
            try {
                LOG.debug("readMessages: reading line from TL1 socket...");
                
                BufferedReader reader = null;
                
                try {
                    reader = getReader();
                    
                } catch (InterruptedException e) {
                    LOG.warn("readMessages: interrupted.");
                    return;
                }
                
                if (reader != null) {
                    int ch;
                    
                    while((ch = reader.read()) != -1 && isStarted()) {
                        rawMessageBuilder.append((char)ch);
                        
                        if((char)ch == ';') {
                            createAndQueueTl1Message(rawMessageBuilder);
                            rawMessageBuilder.setLength(0);
                        }
                    }
                    rawMessageBuilder = null;
                    
                    LOG.warn("readMessages: resetting socket reader to client: {}:{}", m_host, m_port);
                    resetReader(null);
                }
                
            } catch (IOException e) {
                resetReader(e);
            }
        }
        
        LOG.info("TL1 client stopped for: {}:{}", m_host, String.valueOf(m_port));
    }

    private void createAndQueueTl1Message(StringBuilder rawMessageBuilder) {
        LOG.debug("readMessages: offering message to queue: {}", rawMessageBuilder.toString());
        Tl1AutonomousMessage message = detectMessageType(rawMessageBuilder);
        if (message != null) {
            m_tl1Queue.offer(message);
            LOG.debug("readMessages: successfully offered to queue.");
        } else {
            LOG.debug("readMessages: message was null, not offered to queue.");
        }
    }

    //TODO: Lots of work to do here
    private Tl1AutonomousMessage detectMessageType(StringBuilder rawMessage) {
        
        //check token 5 to see if this is a reply message.  This implies that the Tl1Client must
        //track message TAGs (Correlation TAGs (CTAG) vs. Autonomous TAGs (ATAG))
        
        if(isAutonomousMessage(rawMessage)) {
            return m_messageProcessor.process(rawMessage.toString(), Tl1Message.AUTONOMOUS);
        }
        
        return null;
    }

    //TODO: Lots of work to do here
    private boolean isAutonomousMessage(StringBuilder rawMessage) {
        return true;
    }

    private void resetReader(IOException ex) {
        
        if (ex != null) {
            LOG.error("resetReader: connection failure.", ex);
        }
        
        try {
            m_reader.close();
            
        } catch (IOException e) {
            LOG.warn("resetReader", e);
            
        } finally {
            m_reader = null;
        }
        
        try {
            m_tl1Socket.close();
            
        } catch (IOException e) {
            LOG.warn("resetReader", e);
            m_tl1Socket = null;
        }
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#getHost()
     */
    /**
     * <p>getHost</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getHost() {
        return m_host;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#setHost(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void setHost(String host) {
        m_host = host;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#getPort()
     */
    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    @Override
    public int getPort() {
        return m_port;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#setPort(int)
     */
    /** {@inheritDoc} */
    @Override
    public void setPort(int port) {
        m_port = port;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#getReconnectionDelay()
     */
     /**
      * <p>getReconnectionDelay</p>
      *
      * @return a long.
      */
    @Override
     public long getReconnectionDelay() {
        return m_reconnectionDelay;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.tl1d.Tl1Client#setReconnectionDelay(long)
     */
    /** {@inheritDoc} */
    @Override
    public void setReconnectionDelay(long reconnectionDelay) {
        m_reconnectionDelay = reconnectionDelay;
    }

    /**
     * <p>getTl1Queue</p>
     *
     * @return a {@link java.util.concurrent.BlockingQueue} object.
     */
    @Override
    public BlockingQueue<Tl1AutonomousMessage> getTl1Queue() {
        return m_tl1Queue;
    }

    /** {@inheritDoc} */
    @Override
    public void setTl1Queue(BlockingQueue<Tl1AutonomousMessage> tl1Queue) {
        m_tl1Queue = tl1Queue;
    }

    /**
     * <p>getMessageProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor} object.
     */
    @Override
    public Tl1AutonomousMessageProcessor getMessageProcessor() {
        return m_messageProcessor;
    }

    /** {@inheritDoc} */
    @Override
    public void setMessageProcessor(Tl1AutonomousMessageProcessor messageProcessor) {
        m_messageProcessor = messageProcessor;
    }

    /**
     * <p>setStarted</p>
     *
     * @param started a boolean.
     */
    public void setStarted(boolean started) {
        m_started = started;
    }

    /**
     * <p>isStarted</p>
     *
     * @return a boolean.
     */
    public boolean isStarted() {
        return m_started;
    }

    private class TimeoutSleeper {

        public void sleep(long sleepTime) throws InterruptedException {
            Thread.sleep(sleepTime);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Tl1Client: class: "+getClass()+"; host: "+m_host+"; port: "+m_port;
    }
    
}
