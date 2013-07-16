/* Release Version : 3.0 Alpha
 *
 * Date: dd/mm/06
 *
 * Copyright 2006 University of Southampton School of Electronics and Computer Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openoss.opennms.spring.qosd.ejb;

import java.rmi.RemoteException;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.oss.fm.monitor.AlarmKey;
import javax.oss.fm.monitor.AlarmValue;
import javax.oss.fm.monitor.JVTAlarmMonitorHome;
import javax.oss.fm.monitor.JVTAlarmMonitorSession;
import javax.rmi.PortableRemoteObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openoss.opennms.spring.qosd.AlarmListConnectionManager;
import org.openoss.opennms.spring.qosd.PropertiesLoader;
import org.openoss.opennms.spring.qosd.QoSDimpl2;
import org.openoss.ossj.fm.monitor.j2ee.AlarmMonitor;
import java.util.Properties;
import java.util.Hashtable;
import org.openoss.ossj.jvt.fm.monitor.OOSSAlarmValue;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The AlarmListJ2eeConnectionManagerThread tries to register with the AlarmMonitorBean and maintains the connection
 * for the QosD. If the connection is lost, it reestablishes the connection.
 *
 * @author ranger
 * @version $Id: $
 */
public class AlarmListJ2eeConnectionManagerThread extends Thread implements AlarmListConnectionManager 
{
    private static final Logger LOG = LoggerFactory.getLogger(AlarmListJ2eeConnectionManagerThread.class);
	private int status = DISCONNECTED;
	private PropertiesLoader props;
	public Properties env;
	private JVTAlarmMonitorHome home;
	private JVTAlarmMonitorSession session;
	private Object ref;
	private AlarmMonitor alarmInternals;
	private Hashtable<AlarmKey,AlarmValue> alarmList;  // current alarm list - omit cleared and acknowledged alarms
	private int send_status = SENT;
	private boolean init = false;
	private String rebuilt_message="not set";
	
	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.ejb.ConnectionManager#reset_list(java.lang.String)
	 */ 
	/** {@inheritDoc} */
        @Override
	public void reset_list(String _rebuilt_message)
	{
		this.rebuilt_message = _rebuilt_message;
		send_status = REBUILD;
		interrupt();
	}
	
	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.ejb.ConnectionManager#send(java.util.Hashtable)
	 */
	/** {@inheritDoc} */
        @Override
	public void send(Hashtable<AlarmKey, AlarmValue> alarmList)
	{
		this.alarmList = alarmList;
		send_status = SEND;
		interrupt();
	}
	
	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.ejb.ConnectionManager#run()
	 */
	/**
	 * <p>run</p>
	 *
	 * @throws java.lang.IllegalStateException if any.
	 */
        @Override
	public void run() throws IllegalStateException
	{
		//LOG.info("Thread started");
		/* if the init variable is false then the thread has not been initialised
		 * yet so throw an IllegalStateException to indicate this.
		 */
		if(!init)
			throw new IllegalStateException("AlarmListJ2eeConnectionManagerThread - You must call init() before calling run()");
		
		while(true)
		{
			//LOG.debug("Status = {}", status);
			//LOG.debug("Send_status = {}", send_status);
			
			/* If we are connected to the bean and we need to 
			 * send some alarms then try to send them. If the
			 * connection has failed, reconnect. 
			 */
			if((status == CONNECTED) && ((send_status == SEND) || (send_status == REBUILD)) )
			{
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() Sending alarms");
				try
				{
					if(alarmInternals == null)
						LOG.error("AlarmListJ2eeConnectionManagerThread.run() alarmInternals is null");
					
					if (send_status == REBUILD) {
						if(alarmInternals == null) {
							LOG.error("AlarmListJ2eeConnectionManagerThread.run() rebuilt_message is null");
						}
						alarmInternals.rebuildAlarmList(rebuilt_message);
					}
					else {
						if(alarmList == null)
							LOG.error("AlarmListJ2eeConnectionManagerThread.run() alarmList is null");
						alarmInternals.updateAlarmList(alarmList);
					}
					send_status = SENT;
				}
				catch(RemoteException remote_ex)
				{
					LOG.error("Could not contact bean - reconnection attempt started");
					status = DISCONNECTED;
					send_status = SEND;
				}
				catch(NullPointerException np_ex)
				{
					LOG.error("alarmInternals is null, JBoss server may be down", np_ex);
					LOG.info("Attempting reconnect");
					status = DISCONNECTED;
					send_status = SEND;
				}
			}
			
			
			/* If we are connected to the bean and we don't
			 * need to send anything, then test that we are
			 * still connected. If not try to reestablish the 
			 * connection before we need to use it again.
			 */
			if(status == CONNECTED && send_status == SENT)
			{
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() - Polling connection");
				/* test if the connection has been lost
				 * by polling the getVersion method of the 
				 * bean periodically. If the call throws a
				 * remote exception then the bean connection
				 * has been lost. 
				 */
				try
				{
					alarmInternals.getVersion();
				}
				catch(RemoteException remote_ex)
				{
					LOG.error("AlarmListJ2eeConnectionManagerThread.run() Could not contact bean - reconnection attempt started");
					status = DISCONNECTED;
				}
			}
			
			/*If we have disconnected from the bean, reconnect
			 */			
			if(status == DISCONNECTED)
			{
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() Attempting Connecting to bean");
				try
				{
					lookupBean();
					status = CONNECTED;
					LOG.info("AlarmListJ2eeConnectionManagerThread.run() Connected to bean");
				}
				catch(NamingException name_ex)
				{
					status = DISCONNECTED;
					LOG.error("AlarmListJ2eeConnectionManagerThread.run() NamingException caught, Could not connect to bean", name_ex);
				}
				catch(RemoteException remote_ex)
				{
					LOG.error("AlarmListJ2eeConnectionManagerThread.run() RemoteException caught, cannot connect to bean", remote_ex);
					status = DISCONNECTED;
					
				}
				
			}
			
			/*routine to halt thread excecution*/ 
			if(status == STOP)
			{
				LOG.info("AlarmListJ2eeConnectionManagerThread.run() Stopping thread");
				cleanUp();			
				return;
			}
			
			/* Wait for 1 minute between connection/polling attempts
			 * when interrupted excecution can continue for urgent
			 * requests such as sending the alarm list.
			 */
			try
			{
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() AlarmListJ2eeConnectionManagerThread.run() Going to sleep for 1 minute");
				sleep(60000);	//wait for 1 minute before trying to reconnect;
			}
			catch(InterruptedException int_ex)
			{
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() Thread interrupted");
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() Waking up");
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() Connection State = {}", status);
				LOG.debug("AlarmListJ2eeConnectionManagerThread.run() send_status = {}", send_status);
			}
			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.ejb.ConnectionManager#init(org.openoss.opennms.spring.qosd.PropertiesLoader, java.util.Properties)
	 */
	/** {@inheritDoc} */
        @Override
	public void init(PropertiesLoader props, Properties env)
	{
		this.props = props;
		this.env = env;
		init = true;		//inform the thread that it has been initialised 
		//and can execute the run() method.
	}
	
	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.ejb.ConnectionManager#kill()
	 */
	/* Thread.stop() is unsafe so ending run method by changing
	 * the status variable that tells the run method to return
	 * and end execution.
	 */
	/**
	 * <p>kill</p>
	 */
        @Override
	public void kill()
	{
		status = STOP;
		interrupt();
	}
	
	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.ejb.ConnectionManager#getStatus()
	 */
	/**
	 * <p>Getter for the field <code>status</code>.</p>
	 *
	 * @return a int.
	 */
        @Override
	public int getStatus()
	{
		return status;
	}
	
	
	/**
	 *  Method to find and connect to the remote bean.
	 */
	private void lookupBean() throws NamingException, RemoteException
	{
		
		/* Create a new InitialContext with the properties paramters - 
		 * The starting point of naming resolution
		 */
		LOG.info("AlarmListJ2eeConnectionManagerThread.lookupBean() Looking up QoS bean");
		InitialContext ic = new InitialContext(env);
		LOG.info("AlarmListJ2eeConnectionManagerThread.lookupBean() InitialContext created");
		try
		{
			ref = ic.lookup(props.getProperty("org.openoss.opennms.spring.qosd.jvthome"));
			LOG.info("AlarmListJ2eeConnectionManagerThread.lookupBean() QoS Bean found");
			home = (JVTAlarmMonitorHome) PortableRemoteObject.narrow( ref, 
					JVTAlarmMonitorHome.class );
			
			LOG.debug("AlarmListJ2eeConnectionManagerThread.lookupBean() home initialised");
			
			session = home.create();
			
			LOG.debug("AlarmListJ2eeConnectionManagerThread.lookupBean() Session created");
			
			alarmInternals = (AlarmMonitor) PortableRemoteObject.narrow(
					session.getHandle().getEJBObject(), AlarmMonitor.class );
			if(alarmInternals == null)
				LOG.error("AlarmListJ2eeConnectionManagerThread.lookupBean() AlarmMonitor alarmInternals is null line 244");
		}
		catch(IllegalArgumentException iae_ex)
		{
			LOG.error("AlarmListJ2eeConnectionManagerThread.lookupBean() jvthome property does not exist", iae_ex);
		}
		/*catch(RemoteException remote_ex)
		 {
		 log.error("Cannot connect to bean", remote_ex);
		 status = DISCONNECTED;
		 
		 }*/
		catch(CreateException create_ex)
		{
			LOG.error("AlarmListJ2eeConnectionManagerThread.lookupBean() Cannot create new session", create_ex);
		}
		catch(NullPointerException np_ex)
		{
			LOG.error("AlarmListJ2eeConnectionManagerThread.lookupBean() NullPointerException caught", np_ex);
		}
		finally
		{
			ic.close();
		}
		
		LOG.info("AlarmListJ2eeConnectionManagerThread.lookupBean() New bean session started");
	}
	
	/**
	 * Private method to finally clean up the connections
	 */
	private void cleanUp()
	{
		try
		{
			session.remove();
		}
		catch(RemoveException remove_ex)
		{
			LOG.error("AlarmListJ2eeConnectionManagerThread.lookupBean() Cannot remove session", remove_ex);
		}
		catch(RemoteException remote_ex)
		{
			LOG.error("AlarmListJ2eeConnectionManagerThread.lookupBean() Connection to bean lost", remote_ex);
		}
	}

	
	/**
	 * Makes a new empty alarm value object
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession.makeAlarmValue()
	 *
	 * @return a javax$oss$fm$monitor$AlarmValue object.
	 */
        @Override
	public  javax.oss.fm.monitor.AlarmValue makeAlarmValue(){
		return new OOSSAlarmValue();
	}

	/**
	 * Makes a new alarm value object pre-populated with internal objects
	 * which have been made from a local invarient specification.
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession
	 *
	 * @return a javax$oss$fm$monitor$AlarmValue object.
	 */
        @Override
	public javax.oss.fm.monitor.AlarmValue makeAlarmValueFromSpec() {
		javax.oss.fm.monitor.AlarmValue alarmValueSpecification = (javax.oss.fm.monitor.AlarmValue)m_context.getBean("alarmValueSpecification");
			return alarmValueSpecification;
		}
	
	// SPRING DAO SETTERS
	
	/**
	 * used to hold a local reference to the application context from which this bean was started
	 */
	private ClassPathXmlApplicationContext m_context=null; // used to passapplication context to OssBeans

	/**
	 * {@inheritDoc}
	 *
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 */
        @Override
	public  void setApplicationContext(ClassPathXmlApplicationContext m_context){
		this.m_context = m_context;
	}
	
}



