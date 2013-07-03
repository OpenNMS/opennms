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

package org.opennms.sms.reflector.smsservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.IQueueSendingNotification;
import org.smslib.IUSSDNotification;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Phonebook;
import org.smslib.SMSLibException;
import org.smslib.Settings;
import org.smslib.TimeoutException;
import org.smslib.USSDRequest;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Service.ServiceStatus;
import org.smslib.balancing.LoadBalancer;
import org.smslib.crypto.KeyManager;
import org.smslib.queues.AbstractQueueManager;
import org.smslib.routing.Router;

/**
 * <p>SmsService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SmsService {

	/**
	 * <p>addGateway</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @throws org.smslib.GatewayException if any.
	 */
	void addGateway(AGateway gateway) throws GatewayException;
	/**
	 * <p>removeGateway</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a boolean.
	 * @throws org.smslib.GatewayException if any.
	 */
	boolean removeGateway(AGateway gateway) throws GatewayException;
	/**
	 * <p>startService</p>
	 *
	 * @throws org.smslib.SMSLibException if any.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	void startService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>stopService</p>
	 *
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	void stopService() throws SMSLibException, IOException, InterruptedException;
	/**
	 * <p>readMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readMessages</p>
	 *
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @return an array of {@link org.smslib.InboundMessage} objects.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	InboundMessage[] readMessages(MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readMessages</p>
	 *
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return an array of {@link org.smslib.InboundMessage} objects.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	InboundMessage[] readMessages(MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readMessages</p>
	 *
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return an array of {@link org.smslib.InboundMessage} objects.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	InboundMessage[] readMessages(MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readMessage</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param memLoc a {@link java.lang.String} object.
	 * @param memIndex a int.
	 * @return a {@link org.smslib.InboundMessage} object.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	InboundMessage readMessage(String gatewayId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>sendMessage</p>
	 *
	 * @param msg a {@link org.smslib.OutboundMessage} object.
	 * @return a boolean.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>sendMessage</p>
	 *
	 * @param msg a {@link org.smslib.OutboundMessage} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a boolean.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	boolean sendMessage(OutboundMessage msg, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>sendMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int sendMessages(Collection<OutboundMessage> msgList) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>sendMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int sendMessages(OutboundMessage[] msgArray) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>sendMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int sendMessages(Collection<OutboundMessage> msgList, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>sendMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int sendMessages(OutboundMessage[] msgArray, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>queueMessage</p>
	 *
	 * @param msg a {@link org.smslib.OutboundMessage} object.
	 * @return a boolean.
	 */
	boolean queueMessage(OutboundMessage msg);
	/**
	 * <p>queueMessage</p>
	 *
	 * @param msg a {@link org.smslib.OutboundMessage} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean queueMessage(OutboundMessage msg, String gatewayId);
	/**
	 * <p>queueMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @return a int.
	 */
	int queueMessages(Collection<OutboundMessage> msgList);
	/**
	 * <p>queueMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @return a int.
	 */
	int queueMessages(OutboundMessage[] msgArray);
	/**
	 * <p>queueMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 */
	int queueMessages(Collection<OutboundMessage> msgList, String gatewayId);
	/**
	 * <p>queueMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 */
	int queueMessages(OutboundMessage[] msgArray, String gatewayId);
	/**
	 * <p>removeMessage</p>
	 *
	 * @param msg a {@link org.smslib.OutboundMessage} object.
	 * @return a boolean.
	 */
	boolean removeMessage(OutboundMessage msg);
	/**
	 * <p>deleteMessage</p>
	 *
	 * @param msg a {@link org.smslib.InboundMessage} object.
	 * @return a boolean.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>readPhonebook</p>
	 *
	 * @param phonebook a {@link org.smslib.Phonebook} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	int readPhonebook(Phonebook phonebook, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException;
	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 */
	int getInboundMessageCount(String gatewayId);
	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 */
	int getInboundMessageCount(AGateway gateway);
	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a int.
	 */
	int getOutboundMessageCount(String gatewayId);
	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 */
	int getOutboundMessageCount(AGateway gateway);
	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @return a int.
	 */
	int getInboundMessageCount();
	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @return a int.
	 */
	int getOutboundMessageCount();
	/**
	 * <p>findGateway</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a {@link org.smslib.AGateway} object.
	 */
	AGateway findGateway(String gatewayId);
	/**
	 * <p>getGateways</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	Collection<AGateway> getGateways();
	/**
	 * <p>getGatewaysNET</p>
	 *
	 * @return an array of {@link org.smslib.AGateway} objects.
	 */
	AGateway[] getGatewaysNET();
	/**
	 * <p>getLoadBalancer</p>
	 *
	 * @return a {@link org.smslib.balancing.LoadBalancer} object.
	 */
	LoadBalancer getLoadBalancer();
	/**
	 * <p>setLoadBalancer</p>
	 *
	 * @param loadBalancer a {@link org.smslib.balancing.LoadBalancer} object.
	 */
	void setLoadBalancer(LoadBalancer loadBalancer);
	/**
	 * <p>getRouter</p>
	 *
	 * @return a {@link org.smslib.routing.Router} object.
	 */
	Router getRouter();
	/**
	 * <p>setRouter</p>
	 *
	 * @param router a {@link org.smslib.routing.Router} object.
	 */
	void setRouter(Router router);
	/**
	 * <p>getInboundNotification</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification} object.
	 */
	OnmsInboundMessageNotification getInboundNotification();
	/**
	 * <p>setInboundNotification</p>
	 *
	 * @param inboundNotification a {@link org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification} object.
	 */
	void setInboundNotification(OnmsInboundMessageNotification inboundNotification);
	/**
	 * <p>getOutboundNotification</p>
	 *
	 * @return a {@link org.smslib.IOutboundMessageNotification} object.
	 */
	IOutboundMessageNotification getOutboundNotification();
	/**
	 * <p>setOutboundNotification</p>
	 *
	 * @param outboundNotification a {@link org.smslib.IOutboundMessageNotification} object.
	 */
	void setOutboundNotification(IOutboundMessageNotification outboundNotification);
	/**
	 * <p>getCallNotification</p>
	 *
	 * @return a {@link org.smslib.ICallNotification} object.
	 */
	ICallNotification getCallNotification();
	/**
	 * <p>setCallNotification</p>
	 *
	 * @param callNotification a {@link org.smslib.ICallNotification} object.
	 */
	void setCallNotification(ICallNotification callNotification);
	/**
	 * <p>getGatewayStatusNotification</p>
	 *
	 * @return a {@link org.smslib.IGatewayStatusNotification} object.
	 */
	IGatewayStatusNotification getGatewayStatusNotification();
	/**
	 * <p>setGatewayStatusNotification</p>
	 *
	 * @param gatewayStatusNotification a {@link org.smslib.IGatewayStatusNotification} object.
	 */
	void setGatewayStatusNotification(IGatewayStatusNotification gatewayStatusNotification);
	/**
	 * <p>getQueueSendingNotification</p>
	 *
	 * @return a {@link org.smslib.IQueueSendingNotification} object.
	 */
	IQueueSendingNotification getQueueSendingNotification();
	/**
	 * <p>setQueueSendingNotification</p>
	 *
	 * @param queueSendingNotification a {@link org.smslib.IQueueSendingNotification} object.
	 */
	void setQueueSendingNotification(IQueueSendingNotification queueSendingNotification);
	/**
	 * <p>getStartMillis</p>
	 *
	 * @return a long.
	 */
	long getStartMillis();
	/**
	 * <p>getServiceStatus</p>
	 *
	 * @return a {@link org.smslib.Service.ServiceStatus} object.
	 */
	ServiceStatus getServiceStatus();
	/**
	 * <p>getSettings</p>
	 *
	 * @return a {@link org.smslib.Settings} object.
	 */
	Settings getSettings();
	/**
	 * <p>createGroup</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean createGroup(String groupName);
	/**
	 * <p>removeGroup</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean removeGroup(String groupName);
	/**
	 * <p>expandGroup</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @return a {@link java.util.ArrayList} object.
	 */
	ArrayList<String> expandGroup(String groupName);
	/**
	 * <p>addToGroup</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @param number a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean addToGroup(String groupName, String number);
	/**
	 * <p>removeFromGroup</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 * @param number a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean removeFromGroup(String groupName, String number);
	/**
	 * <p>getQueueManager</p>
	 *
	 * @return a {@link org.smslib.queues.QueueManager} object.
	 */
	AbstractQueueManager getQueueManager();
	/**
	 * <p>getKeyManager</p>
	 *
	 * @return a {@link org.smslib.crypto.KeyManager} object.
	 */
	KeyManager getKeyManager();
	/**
	 * <p>sendUSSDRequest</p>
	 *
	 * @param req a {@link org.smslib.USSDRequest} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return a boolean.
	 * @throws org.smslib.GatewayException if any.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
	boolean sendUSSDRequest(USSDRequest req, String gatewayId) throws GatewayException, TimeoutException, IOException, InterruptedException;
	/**
	 * <p>setUSSDNotification</p>
	 *
	 * @param notif a {@link org.smslib.IUSSDNotification} object.
	 */
	void setUSSDNotification(IUSSDNotification notif);
	/**
	 * <p>getUSSDNotification</p>
	 *
	 * @return a {@link org.smslib.IUSSDNotification} object.
	 */
	IUSSDNotification getUSSDNotification();
}
