/*
 * Created on Mar 7, 2005
 * Copyright (C) 2005, The OpenNMS Group, Inc..
 * 
 */

package org.opennms.netmgt.notifd;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.NotificationManager;

/**
 * Implements NotificationStrategy pattern used to send notifications using the
 * Growl message protocol.  This is basically a clone of the XMPP implementation.
 * 
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 * 
 */
public class GrowlNotificationStrategy implements NotificationStrategy {

	/**
	 * Text of Growl Message to be sent.
	 */
	private static final int Growl_MESSAGE;
	private static final int Growl_UEI;
	private static final int Growl_SUBJECT;

	/**
	 * The value of this constant indicates the number of
	 * Growl constants defined.
	 */

	private static final int Growl_MAX;
	
	/**
	 * Mapping of index values to meaningful strings.
	 */
	private static final String[] INDEX_TO_NAME;

    private Properties props = new Properties();
	private Category log     = null;

	// Initialize constant class data

	static {

    	Growl_MESSAGE = 0;
		Growl_UEI     = 1;
		Growl_SUBJECT = 2;
    	Growl_MAX     = 3;

    	INDEX_TO_NAME = new String[Growl_MAX];

    	INDEX_TO_NAME[Growl_MESSAGE] = "Message";
    	
    }

    /**
     * 
     */
    public GrowlNotificationStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    public int send(List arguments) {

        try {

			log = ThreadCategory.getInstance(getClass());
      	log.debug("In the GrowlNotification class.");

        	String[] parsedArgs = this.parseArguments(arguments);
         String[] notificationTypes = { parsedArgs[Growl_UEI] };

         Growl theGrowl = new Growl("OpenNMS", notificationTypes, notificationTypes);
         theGrowl.register();
         theGrowl.notifyGrowlOf( parsedArgs[Growl_UEI], parsedArgs[Growl_SUBJECT],  parsedArgs[Growl_MESSAGE]);

        } catch (Exception e) {
        	ThreadCategory.getInstance(getClass()).error(e.getMessage());
        	return 1;
        }

        return 0;

    }

    /**
     * This method extracts the message text from the
     * parameters passed in the notification.
     * 
     * @param arguments
     * @return String[]
     * @throws Exception
     */

    private String[] parseArguments(List arguments) throws Exception {

    	String[] parsedArgs = new String[Growl_MAX];

    	for (int i = 0; i < arguments.size(); i++) {

            Argument arg = (Argument) arguments.get(i);

            if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                parsedArgs[Growl_MESSAGE] = arg.getValue();
            }
            if (arg.getSwitch().equals("eventUEI")) {
                parsedArgs[Growl_UEI] = "OpenNMS." + arg.getValue();
            }
            if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) {
                parsedArgs[Growl_SUBJECT] = arg.getValue();
            }

    	}

    	for (int i = 0; i < Growl_MAX; ++i) {
    		if (parsedArgs[i] == null) {
    			throw( new Exception("Incomplete argument set, missing argument: " + INDEX_TO_NAME[i] ) );
    		}
    	}
    	
    	return parsedArgs;
    	
    }

}
