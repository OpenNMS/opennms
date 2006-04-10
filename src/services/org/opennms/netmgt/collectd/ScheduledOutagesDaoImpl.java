package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;

public class ScheduledOutagesDaoImpl implements ScheduledOutagesDao {
	
	public ScheduledOutagesDaoImpl() {
		loadScheduledOutagesConfigFactory();
	}
	
	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private void loadScheduledOutagesConfigFactory() {
	    // Load up the configuration for the scheduled outages.
	    try {
	        PollOutagesConfigFactory.reload();
	    } catch (MarshalException ex) {
	        log().fatal("init: Failed to load poll-outage configuration", ex);
	        throw new UndeclaredThrowableException(ex);
	    } catch (ValidationException ex) {
	        log().fatal("init: Failed to load poll-outage configuration", ex);
	        throw new UndeclaredThrowableException(ex);
	    } catch (IOException ex) {
	        log().fatal("init: Failed to load poll-outage configuration", ex);
	        throw new UndeclaredThrowableException(ex);
	    }
	}
	

}
