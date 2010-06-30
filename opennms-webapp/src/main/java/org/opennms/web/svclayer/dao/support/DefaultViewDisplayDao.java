/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 27, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.web.svclayer.dao.ViewDisplayDao;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * <p>DefaultViewDisplayDao class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class DefaultViewDisplayDao implements ViewDisplayDao {
	
	/**
	 * <p>Constructor for DefaultViewDisplayDao.</p>
	 */
	public DefaultViewDisplayDao() {
		try {
			ViewsDisplayFactory.init();
		} catch (MarshalException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (ValidationException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate viewsDisplaly file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load viewsDisplay file", e);
		}
	}
	
	/**
	 * <p>getView</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.viewsdisplay.View} object.
	 */
	public View getView() {
		try {
			return ViewsDisplayFactory.getInstance().getView("WebConsoleView");
		} catch (MarshalException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (ValidationException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate viewsDisplaly file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load viewsDisplay file", e);
		}
		
	}

}
