/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

public abstract class PollerFrontEndState {

    public void checkIn() {
        // a pollerCheckingIn in any state that doesn't respond just does nothing
    }

    protected IllegalStateException illegalState(final String msg) {
        return new IllegalStateException(msg + " State: " + this);
    }

    public void initialize() {
        throw illegalState("Initialize called on invalid state.");
    }

    public boolean isRegistered() {
        return true;
    }

    public boolean isStarted() {
        return false;
    }

    public boolean isPaused() {
        return false;
    }

    public boolean isDisconnected() {
        return false;
    }

    public boolean isExitNecessary() {
        return false;
    }

    public void pollService(final Integer serviceId) {
        throw illegalState("Cannot poll from this state.");
    }

    public void register(final String location) {
        throw illegalState("Cannot register from this state.");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}