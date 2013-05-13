/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;

/**
 * <p>ScheduledOutagesDaoImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ScheduledOutagesDaoImpl implements ScheduledOutagesDao {
	
	/**
	 * <p>Constructor for ScheduledOutagesDaoImpl.</p>
	 */
	public ScheduledOutagesDaoImpl() {
		loadScheduledOutagesConfigFactory();
	}
	
	private ThreadCategory log() {
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

	/** {@inheritDoc} */
        @Override
	public OnmsOutageCalendar get(String outageName) {
		return new OnmsOutageCalendar();
	}
	
	
	

}
