package org.opennms.sms.reflector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.IQueueSendingNotification;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Phonebook;
import org.smslib.queues.QueueManager;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.Settings;
import org.smslib.TimeoutException;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Service.ServiceStatus;
import org.smslib.balancing.LoadBalancer;
import org.smslib.crypto.KeyManager;
import org.smslib.helper.Logger;
import org.smslib.routing.Router;

public class SmsServiceImpl implements SmsService {
	
	private Service m_service;
	
	public void addGateway(AGateway gateway) throws GatewayException {
		m_service.addGateway(gateway);
	}

	public boolean addToGroup(String groupName, String number) {
		return m_service.addToGroup(groupName, number);
	}

	public boolean createGroup(String groupName) {
		return m_service.createGroup(groupName);
	}

	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.deleteMessage(msg);
	}

	public ArrayList<String> expandGroup(String groupName) {
		return m_service.expandGroup(groupName);
	}

	public AGateway findGateway(String gatewayId) {
		return m_service.findGateway(gatewayId);
	}

	public ICallNotification getCallNotification() {
		return m_service.getCallNotification();
	}

	public IGatewayStatusNotification getGatewayStatusNotification() {
		return m_service.getGatewayStatusNotification();
	}

	public Collection<AGateway> getGateways() {
		return m_service.getGateways();
	}

	public AGateway[] getGatewaysNET() {
		return m_service.getGatewaysNET();
	}

	public int getInboundMessageCount(String gatewayId) {
		return m_service.getInboundMessageCount(gatewayId);
	}

	public int getInboundMessageCount(AGateway gateway) {
		return m_service.getInboundMessageCount(gateway);
	}

	public int getInboundMessageCount() {
		return m_service.getInboundMessageCount();
	}

	public IInboundMessageNotification getInboundNotification() {
		return m_service.getInboundNotification();
	}

	public KeyManager getKeyManager() {
		return m_service.getKeyManager();
	}

	public LoadBalancer getLoadBalancer() {
		return m_service.getLoadBalancer();
	}

	public Logger getLogger() {
		return m_service.getLogger();
	}

	public int getOutboundMessageCount(String gatewayId) {
		return m_service.getOutboundMessageCount(gatewayId);
	}

	public int getOutboundMessageCount(AGateway gateway) {
		return m_service.getOutboundMessageCount(gateway);
	}

	public int getOutboundMessageCount() {
		return m_service.getOutboundMessageCount();
	}

	public IOutboundMessageNotification getOutboundNotification() {
		return m_service.getOutboundNotification();
	}

	public QueueManager getQueueManager() {
		return m_service.getQueueManager();
	}

	public IQueueSendingNotification getQueueSendingNotification() {
		return m_service.getQueueSendingNotification();
	}

	public Router getRouter() {
		return m_service.getRouter();
	}

	public ServiceStatus getServiceStatus() {
		return m_service.getServiceStatus();
	}

	public Settings getSettings() {
		return m_service.getSettings();
	}

	public long getStartMillis() {
		return m_service.getStartMillis();
	}

	public boolean queueMessage(OutboundMessage msg) {
		return m_service.queueMessage(msg);
	}

	public boolean queueMessage(OutboundMessage msg, String gatewayId) {
		return m_service.queueMessage(msg, gatewayId);
	}

	public int queueMessages(Collection<OutboundMessage> msgList) {
		return m_service.queueMessages(msgList);
	}

	public int queueMessages(OutboundMessage[] msgArray) {
		return m_service.queueMessages(msgArray);
	}

	public int queueMessages(Collection<OutboundMessage> msgList, String gatewayId) {
		return m_service.queueMessages(msgList, gatewayId);
	}

	public int queueMessages(OutboundMessage[] msgArray, String gatewayId) {
		return m_service.queueMessages(msgArray, gatewayId);
	}

	public InboundMessage readMessage(String gatewayId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessage(gatewayId, memLoc, memIndex);
	}

	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass);
	}

	public InboundMessage[] readMessages(MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass);
	}

	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass, gatewayId);
	}

	public InboundMessage[] readMessages(MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gatewayId);
	}

	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass, gateway);
	}

	public InboundMessage[] readMessages(MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gateway);
	}

	public int readPhonebook(Phonebook phonebook, String gatewayId) throws TimeoutException, GatewayException, IOException,InterruptedException {
		return m_service.readPhonebook(phonebook, gatewayId);
	}

	public boolean removeFromGroup(String groupName, String number) {
		return m_service.removeFromGroup(groupName, number);
	}

	public boolean removeGateway(AGateway gateway) throws GatewayException {
		return m_service.removeGateway(gateway);
	}

	public boolean removeGroup(String groupName) {
		return m_service.removeGroup(groupName);
	}

	public boolean removeMessage(OutboundMessage msg) {
		return m_service.removeMessage(msg);
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg);
	}

	public boolean sendMessage(OutboundMessage msg, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg, gatewayId);
	}

	public int sendMessages(Collection<OutboundMessage> msgList) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList);
	}

	public int sendMessages(OutboundMessage[] msgArray) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray);
	}

	public int sendMessages(Collection<OutboundMessage> msgList, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList, gatewayId);
	}

	public int sendMessages(OutboundMessage[] msgArray, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray, gatewayId);
	}

	public void setCallNotification(ICallNotification callNotification) {
		m_service.setCallNotification(callNotification);
	}

	public void setGatewayStatusNotification(IGatewayStatusNotification gatewayStatusNotification) {
		m_service.setGatewayStatusNotification(gatewayStatusNotification);
	}

	public void setInboundNotification(IInboundMessageNotification inboundNotification) {
		m_service.setInboundNotification(inboundNotification);
	}

	public void setLoadBalancer(LoadBalancer loadBalancer) {
		m_service.setLoadBalancer(loadBalancer);
	}

	public void setOutboundNotification(IOutboundMessageNotification outboundNotification) {
		m_service.setOutboundNotification(outboundNotification);
	}

	public void setQueueSendingNotification(IQueueSendingNotification queueSendingNotification) {
		m_service.setQueueSendingNotification(queueSendingNotification);
	}

	public void setRouter(Router router) {
		m_service.setRouter(router);
	}

	public void startService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException {
		m_service.startService();
	}

	public void stopService() throws TimeoutException, GatewayException, IOException, InterruptedException {
		m_service.stopService();
	}

	public void setService(Service m_service) {
		this.m_service = m_service;
	}

	public Service getService() {
		return m_service;
	}

}
