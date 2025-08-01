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