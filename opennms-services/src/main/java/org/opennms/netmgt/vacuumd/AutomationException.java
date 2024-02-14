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
package org.opennms.netmgt.vacuumd;

/**
 * <p>AutomationException class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class AutomationException extends RuntimeException {

    private static final long serialVersionUID = -8873671974245928627L;

    /**
	 * <p>Constructor for AutomationException.</p>
	 *
	 * @param arg0 a {@link java.lang.String} object.
	 */
	public AutomationException(String arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for AutomationException.</p>
	 *
	 * @param arg0 a {@link java.lang.Throwable} object.
	 */
	public AutomationException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * <p>Constructor for AutomationException.</p>
	 *
	 * @param arg0 a {@link java.lang.String} object.
	 * @param arg1 a {@link java.lang.Throwable} object.
	 */
	public AutomationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
