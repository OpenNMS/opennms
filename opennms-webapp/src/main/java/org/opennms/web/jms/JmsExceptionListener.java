package org.opennms.web.jms;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Neumann
 */
public class JmsExceptionListener implements ExceptionListener {
    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + JmsExceptionListener.class);
    
    @Override
    public void onException(final JMSException e) {
        logger.error("JmsException '{}'", e.getMessage());
    }
}
