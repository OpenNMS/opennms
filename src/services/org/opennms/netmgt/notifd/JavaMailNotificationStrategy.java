/*
 * Created on Sep 8, 2004
 * Copyright (C) 2004, Blast Consulting Company.
 * 
 */
package org.opennms.netmgt.notifd;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.JavaMailer;
import org.opennms.core.utils.JavaMailerException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotificationFactory;


/**Implements NotificationStragey pattern used to send notifications
 * via the Java Mail API.
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace</A>
 *  
 */
public class JavaMailNotificationStrategy implements NotificationStrategy {
	
	Category log = null;

	/**
	 *  
	 */
	public JavaMailNotificationStrategy() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
	 */
	public int send(List arguments) {
		
		log = ThreadCategory.getInstance(getClass());
		log.debug("In the JavaMailNotification class.");
				
		JavaMailer jm = buildMessage(arguments);
		
		try {
			jm.mailSend();
		} catch (JavaMailerException e) {
			return 1;
		}
		return 0;
	}

	/**
	 * This method extracts the to, subject, and message text from
	 * the parameters passed in the notification.
	 * @param arguments
	 */
	private JavaMailer buildMessage(List arguments) {

		JavaMailer jm = new JavaMailer();
				
		for (int i = 0; i < arguments.size(); i++) {

			Argument arg = (Argument) arguments.get(i);
			log.debug("Current arg switch: " + i + " of " + arguments.size() +" is: " + arg.getSwitch());
			log.debug("Current arg  value: " + i + " of " + arguments.size() +" is: " + arg.getValue());
			
			/*
			 * Note: The recipient gets set by whichever of the two switches:
			 * (PARAM_EMAIL or PARAM_PAGER_EMAIL) are
			 * specified last in the notificationCommands.xml file
			 */
			if (NotificationFactory.PARAM_EMAIL.equals(arg.getSwitch())) {
				log.debug("Found: PARAM_EMAIL");
				jm.setTo(arg.getValue());
			} else if (NotificationFactory.PARAM_PAGER_EMAIL.equals(arg.getSwitch())) {
				log.debug("Found: PARAM_PAGER_EMAIL");
				jm.setTo(arg.getValue());
			} else if (NotificationFactory.PARAM_SUBJECT.equals(arg.getSwitch())) {
				log.debug("Found: PARAM_SUBJECT");
				jm.setSubject(arg.getValue());
			} else if (NotificationFactory.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
				log.debug("Found: PARAM_TEXT_MSG");
				jm.setMessageText(arg.getValue());
			}
		}
		
		return jm;
	}

}