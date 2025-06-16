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
package org.opennms.netmgt.syslogd;

/**
 * An individual stage of the token parser. A parser is composed of
 * a sequence of {@link ParserStage} objects.
 */
public interface ParserStage {

	public static enum AcceptResult {
		/**
		 * Continue the parsing process.
		 */
		CONTINUE,
		/**
		 * Complete the parsing stage and consider the current
		 * character as already consumed.
		 */
		COMPLETE_AFTER_CONSUMING,
		/**
		 * Complete the parsing stage and reset the position of the
		 * buffer so that the next stage can consume the current
		 * character.
		 */
		COMPLETE_WITHOUT_CONSUMING,
		/**
		 * Cancel the parsing process due to a failure to parse
		 * based on the current rules for this stage.
		 */
		CANCEL
	}

	/**
	 * Mark the stage as optional.
	 * 
	 * @param optional
	 */
	void setOptional(boolean optional);

	/**
	 * Mark the stage as terminal, ie. it handles a buffer
	 * underflow as successful completion instead of failure.
	 * 
	 * @param terminal
	 */
	void setTerminal(boolean terminal);

	/**
	 * Process the state for this stage and return it so
	 * that the next stage can continue processing.
	 */
	ParserState apply(ParserState state);
}