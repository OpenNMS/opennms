/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.svclayer.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;

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
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate viewsDisplaly file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load viewsDisplay file", e);
		}
		
	}

}
