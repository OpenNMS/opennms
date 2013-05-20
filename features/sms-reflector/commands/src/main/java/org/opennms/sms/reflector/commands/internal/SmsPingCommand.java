/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector.commands.internal;

import java.io.PrintStream;
import org.opennms.sms.ping.SmsPinger;
import org.apache.felix.shell.Command;

/**
 * <p>SmsPingCommand class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsPingCommand implements Command {

	/** {@inheritDoc} */
        @Override
	public void execute(String s, PrintStream out, PrintStream err) {
		try {
			String[] command = s.split("\\s");
			String phoneNumber = null;
			if(command.length > 1){
				phoneNumber = command[1];
			}else{
				throw new IllegalArgumentException("You need to have a phone number to ping. Usage smsPing <phoneNumber>");
			}
            Long latency = SmsPinger.ping(phoneNumber);
            
            if(latency == null){
            	out.println("Ping Timedout");
            }else{
            	out.println("Ping roundtrip time: " + latency);
            }
            
        } catch (Throwable e) {
        	err.println(e);
        }
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getName() {
		return "smsPing";
	}

	/**
	 * <p>getShortDescription</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getShortDescription() {
		return "Initiates an smsPing to the desired phonenumber";
	}

	/**
	 * <p>getUsage</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getUsage() {
		return "smsPing <phoneNumber>";
	}

}
