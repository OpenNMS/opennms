/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.util.ilr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Main {

	/**
	 * @param args
	 * @throws IOException  	
	 */
	public static void main(String[] args) throws IOException {
		main(args, System.out);
	}
	public static void main(String[] args, OutputStream out) throws IOException {
		Collector c = new Collector();
		for(int i=0 ; i < args.length ; i++){
			c.readLogMessagesFromFile(args[i]);
		}

		PrintWriter writer = new PrintWriter(out,true);
		
		c.printReport(writer);

	}

}
