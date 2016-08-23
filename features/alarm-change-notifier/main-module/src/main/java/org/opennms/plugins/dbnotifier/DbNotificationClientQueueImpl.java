package org.opennms.plugins.dbnotifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * This is a notification client which receives the notification and adds it to a queue for processing
 * @author admin
 *
 */
public class DbNotificationClientQueueImpl implements DbNotificationClient{

	private static final Logger LOG = LoggerFactory.getLogger(DbNotificationClientQueueImpl.class);

	private DatabaseChangeNotifier databaseChangeNotifier;
	
	private Integer maxQueueLength=1000;

	private LinkedBlockingQueue<DbNotification> queue=null;
	private AtomicBoolean clientRunning = new AtomicBoolean(false);

	private RemovingConsumer removingConsumer = new RemovingConsumer();
	private Thread removingConsumerThread = new Thread(removingConsumer);
	
	private Map<String,NotificationClient> channelHandlingClients = new HashMap<String, NotificationClient>();


	/**
	 * @param channelHandlingClients the channelHandlingClients to set
	 */
	public void setChannelHandlingClients(Map<String,NotificationClient> channelHandlingClients) {
		this.channelHandlingClients.putAll(channelHandlingClients);
	}

	/**
	 * @param databaseChangeNotifier
	 */
	public void setDatabaseChangeNotifier(DatabaseChangeNotifier databaseChangeNotifier) {
		this.databaseChangeNotifier = databaseChangeNotifier;
	}

	/**
	 * @return the databaseChangeNotifier
	 */
	public DatabaseChangeNotifier getDatabaseChangeNotifier() {
		return databaseChangeNotifier;
	}
	
	public Integer getMaxQueueLength() {
		return maxQueueLength;
	}

	public void setMaxQueueLength(Integer maxQueueLength) {
		this.maxQueueLength = maxQueueLength;
	}

	public void init(){
		LOG.debug("initialising dbNotificationClientQueue with queue size "+maxQueueLength);
		if (databaseChangeNotifier==null) throw new IllegalStateException("databaseChangeNotifier cannot be null");
		
		queue= new LinkedBlockingQueue<DbNotification>(maxQueueLength);
		
		// start consuming thread
		clientRunning.set(true);
		removingConsumerThread.start();

		// start listening for notifications
		databaseChangeNotifier.addDbNotificationClient(this);

	}

	public void destroy(){
		LOG.debug("shutting down client");
		if (databaseChangeNotifier==null) throw new IllegalStateException("databaseChangeNotifier cannot be null");

		// stop listening for notifications
		databaseChangeNotifier.removeDbNotificationClient(this);

		// signal consuming thread to stop
		clientRunning.set(false);
		removingConsumerThread.interrupt();
	}

	@Override
	public void sendDbNotification(DbNotification dbNotification) {
		if(LOG.isDebugEnabled()) LOG.debug("client received notification - adding notification to queue");
		
		if (! queue.offer(dbNotification)){
			LOG.warn("Cannot queue any more dbNotification. dbNotification queue full. size="+queue.size());
		};

	}



	/*
	 * Class run in separate thread to remove and process notifications from the queue 
	 */
	private class RemovingConsumer implements Runnable {
		//TODO final Logger LOG = LoggerFactory.getLogger(DbNotificationClientQueueImpl.class);

		@Override
		public void run() {

			// we remove elements from the queue until interrupted and clientRunning==false.
			while (clientRunning.get()) {
				try {
					DbNotification dbNotification = queue.take();

					if(LOG.isDebugEnabled()) LOG.debug("Notification received from queue by consumer thread :\n processId:"+dbNotification.getProcessId()
							+ "\n channelName:"+dbNotification.getChannelName()
							+ "\n payload:"+dbNotification.getPayload());
					
					// we look in hashtable for channel handling clients to handle this received notification
					if(channelHandlingClients.isEmpty()) { 
						LOG.warn("no channel handing clients have been set to receive notification");
					} else {
						NotificationClient channelHandlingClient = channelHandlingClients.get(dbNotification.getChannelName());
						if (channelHandlingClient==null){
							LOG.warn("no channel handing client has been set for channel:"+dbNotification.getChannelName());
						} else
						try {
							channelHandlingClient.sendDbNotification(dbNotification);
						} catch (Exception e){
							LOG.error("problem processing dbNotification:",e);
						}
					}

				} catch (InterruptedException e) { }

			}

			LOG.debug("shutting down notification consumer thread");
		}
	}

}





