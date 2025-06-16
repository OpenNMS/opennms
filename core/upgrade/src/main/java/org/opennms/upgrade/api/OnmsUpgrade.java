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
 * The Interface OnmsUpgrade.
 * <p>All the post-processing scripts must implement this class.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public interface OnmsUpgrade {

    /**
     * Gets the order.
     * <p>In order to execute all the implementations of this interface on a specify order,
     * each implementation must return an integer for this purpose.</p>
     * 
     * @return the order
     */
    int getOrder();

    /**
     * Gets the id.
     * <p>This is for informational purposes, and will be used to store the execution status.</p>
     * <p>It is recommended to use the class-name, but any other text can be used.</p>
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the description.
     * <p>This is for informational purposes, and should contain a brief description about
     * what is the purpose of this upgrade class.</p>
     *
     * @return the description
     */
    String getDescription();

    /**
     * Pre-execute
     * <p>Runs some checks to ensure that what it would execute() would work.
     * If something is missing, an exception will be thrown<p>
     * <p>Execute backups of the JRBs/RRDs if they are going to be modified.</p>
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    void preExecute() throws OnmsUpgradeException;

    /**
     * Post execute.
     * <p>Runs some clean up tasks after executing the execute() method.
     * If something is wrong, an exception will be thrown<p>
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    void postExecute() throws OnmsUpgradeException;

    /**
     * Rollback.
     * <p>Restore the initial state of the OpenNMS files if something went wrong while running the execute() method.</p>
     * <p>Must restore the backups of the files if necessary.</p>
     *
     * @throws OnmsUpgradeException the onms upgrade exception
     */
    void rollback() throws OnmsUpgradeException;

    /**
     * Execute.
     * <p>This is the main method, and this is the one where all the upgrade code
     * must be placed.</p>
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    void execute() throws OnmsUpgradeException;

    /**
     * Requires OpenNMS running.
     *
     * @return true, if OpenNMS must be running to execute this upgrade or false
     * if OpenNMS must be stopped.
     */
    boolean requiresOnmsRunning();

    /**
     * Run only once and mark when successfully executed?
     *
     * @return true, whether this job should only be run once
     */
    default boolean runOnlyOnce() {
        return true;
    }
}


