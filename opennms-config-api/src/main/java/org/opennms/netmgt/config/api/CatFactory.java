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
package org.opennms.netmgt.config.api;

import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.Catinfo;

/**
 * <p>CatFactory interface.</p>
 *
 * @author jsartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface CatFactory {

	/**
	 * <p>getConfig</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.categories.Catinfo} object.
	 */
	public Catinfo getConfig();
	
	/**
	 * <p>getCategory</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.config.categories.Category} object.
	 */
	public Category getCategory(String name);
	
	/**
	 * <p>getEffectiveRule</p>
	 *
	 * @param catLabel a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getEffectiveRule(String catLabel);
	
	/**
	 * <p>getNormal</p>
	 *
	 * @param catlabel a {@link java.lang.String} object.
	 * @return a double.
	 */
	public double getNormal(String catlabel);
	
	/**
	 * <p>getWarning</p>
	 *
	 * @param catlabel a {@link java.lang.String} object.
	 * @return a double.
	 */
	public double getWarning(String catlabel);

    public Lock getReadLock();
    
    public Lock getWriteLock();
	
}
