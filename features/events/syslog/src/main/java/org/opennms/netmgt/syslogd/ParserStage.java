/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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