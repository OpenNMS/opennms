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
 * @since 1.8.1
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
        @Override
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
