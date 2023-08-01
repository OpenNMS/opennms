/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.container.jetty.extensions.noop;

import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.extensions.Extension;
import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.api.extensions.IncomingFrames;
import org.eclipse.jetty.websocket.api.extensions.OutgoingFrames;

public class NoopExtension implements Extension {

	@Override
	public void incomingFrame(final Frame frame) {
		// do nothing
	}

	@Override
	public void outgoingFrame(final Frame frame, final WriteCallback cb, final BatchMode mode) {
		// do nothing
	}

	@Override
	public ExtensionConfig getConfig() {
		return new ExtensionConfig("noop");
	}

	@Override
	public String getName() {
		return "noop";
	}

	@Override
	public boolean isRsv1User() {
		return false;
	}

	@Override
	public boolean isRsv2User() {
		return false;
	}

	@Override
	public boolean isRsv3User() {
		return false;
	}

	@Override
	public void setNextIncomingFrames(final IncomingFrames frames) {
		// do nothing
	}

	@Override
	public void setNextOutgoingFrames(final OutgoingFrames frames) {
		// do nothing
	}

}