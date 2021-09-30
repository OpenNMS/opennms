/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
}


