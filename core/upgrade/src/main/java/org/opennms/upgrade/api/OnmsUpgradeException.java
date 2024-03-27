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
package org.opennms.upgrade.api;

/**
 * The Class OnmsUpgradeException.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@SuppressWarnings("serial")
public class OnmsUpgradeException extends Exception {

    /**
     * Instantiates a new OpenNMS upgrade exception.
     */
    public OnmsUpgradeException() {
        super();
    }

    /**
     * Instantiates a new OpenNMS upgrade exception.
     *
     * @param msg the message
     * @param t the exception causing the problem.
     */
    public OnmsUpgradeException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Instantiates a new OpenNMS upgrade exception.
     *
     * @param msg the message
     */
    public OnmsUpgradeException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new OpenNMS upgrade exception.
     *
     * @param t the exception causing the problem.
     */
    public OnmsUpgradeException(Throwable t) {
        super(t);
    }

}