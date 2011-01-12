// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2009, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opennms.sms.monitor;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.LogUtils;
import org.opennms.sms.reflector.smsservice.MobileMsgTrackerTest;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.USSDRequest;
import org.smslib.USSDResponse;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage.MessageStatuses;

/**
 * TestGateway - virtual gateway to simulate sending and receiving messages to
 * make testing easier.
 */
public class FakeTestGateway extends AGateway
{
	private int refCounter = 0;

	private int counter = 0;

	private class QueueRunner implements Runnable, Delayed {
		InboundMessage m_message;
		private USSDResponse m_response;
		long m_expiration = 0;
		
		public QueueRunner(USSDResponse response, long milliseconds) {
			System.err.println("QueueRunner initialized with timeout " + milliseconds + " for message: " + response);
			m_response = response;
			m_expiration = System.currentTimeMillis() + milliseconds;
		}

		public QueueRunner(InboundMessage message, long milliseconds) {
			System.err.println("QueueRunner initialized with timeout " + milliseconds + " for message: " + message);
			m_message = message;
			m_expiration = System.currentTimeMillis() + milliseconds;
		}
		public void run() {
			if (m_message != null) {
				System.err.println("QueueRunner(run): " + getService().getInboundNotification());
				if (getService().getInboundNotification() != null ) {
					getService().getInboundNotification().process(getGatewayId(), MessageTypes.INBOUND, m_message);
				}
			} else if (m_response != null) {
				System.err.println("QueueRunner(run): " + getService().getUSSDNotification());
				if (getService().getUSSDNotification() != null ) {
					getService().getUSSDNotification().process(getGatewayId(), m_response);
				}
			}
		}

		public long getDelay(TimeUnit unit) {
			long remainder = m_expiration - System.currentTimeMillis();
			return unit.convert(remainder, TimeUnit.MILLISECONDS);
		}

		public int compareTo(Delayed o) {
			long thisVal = this.getDelay(TimeUnit.NANOSECONDS);
			long anotherVal = o.getDelay(TimeUnit.NANOSECONDS);
			return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
		}
		
	}
	
	private DelayQueue<QueueRunner> m_delayQueue = new DelayQueue<QueueRunner>();
	
	Thread incomingMessagesThread;

	public FakeTestGateway(String id)
	{
		super(id);
		System.err.println("Initializing FakeTestGateway");
		setAttributes(GatewayAttributes.SEND);
		setInbound(true);
		setOutbound(true);
	}

	/* (non-Javadoc)
	 * @see org.smslib.AGateway#deleteMessage(org.smslib.InboundMessage)
	 */
	@Override
	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		//NOOP
		return true;
	}

	/* (non-Javadoc)
	 * @see org.smslib.AGateway#startGateway()
	 */
	@Override
	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		super.startGateway();
		this.incomingMessagesThread = new Thread(new Runnable()
		{
			// Run thread to fake incoming messages
			public void run()
			{
				while (!FakeTestGateway.this.incomingMessagesThread.isInterrupted())
				{
					try {
						QueueRunner runner = m_delayQueue.take();
						runner.run();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						LogUtils.warnf(this, e, "failed to run queue");
						break;
					}
				}
			}
		}, "IncomingMessagesThread");
		this.incomingMessagesThread.start();
	}

	/* (non-Javadoc)
	 * @see org.smslib.AGateway#stopGateway()
	 */
	@Override
	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		super.stopGateway();
		if (this.incomingMessagesThread != null)
		{
			this.incomingMessagesThread.interrupt();
		}
	}

	@Override
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		// simulate delay
		getService().getLogger().logInfo("Sending to: " + msg.getRecipient() + " via: " + msg.getGatewayId(), null, getGatewayId());
		Thread.sleep(500);
		this.counter++;

		msg.setDispatchDate(new Date());
		msg.setMessageStatus(MessageStatuses.SENT);
		msg.setRefNo(Integer.toString(++this.refCounter));
		msg.setGatewayId(getGatewayId());
		getService().getLogger().logInfo("Sent to: " + msg.getRecipient() + " via: " + msg.getGatewayId(), null, getGatewayId());
		
		String msgText = msg.getText();
		if (msgText != null) {
			if (msgText.startsWith("ping")) {
			    msgText = "pong";
			} else if (msgText.startsWith("You suck")) {
				msgText = "No";
			}
		}

		InboundMessage inbound = new InboundMessage(msg.getDate(), msg.getRecipient(), msgText, 1, "DEADBEEF");
		QueueRunner runner = new QueueRunner(inbound, 500);
		m_delayQueue.offer(runner);
		return true;
	}

	@Override
	public boolean sendUSSDRequest(USSDRequest request) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		getService().getLogger().logInfo("Sending to: " + request.getContent() + " via: " + request.getGatewayId(), null, getGatewayId());
		Thread.sleep(500);
		this.counter++;

		request.setGatewayId(getGatewayId());

		String content = request.getContent();
		if (content != null && content.equals("#225#")) {
			content = "+CUSD: 0,\"" + MobileMsgTrackerTest.TMOBILE_RESPONSE + "\"";
		}

		USSDResponse response  = new USSDResponse(content, getGatewayId());
		QueueRunner runner = new QueueRunner(response, 500);
		m_delayQueue.offer(runner);
		return true;
	}

	@Override
	public int getQueueSchedulingInterval()
	{
		return 500;
	}
}
