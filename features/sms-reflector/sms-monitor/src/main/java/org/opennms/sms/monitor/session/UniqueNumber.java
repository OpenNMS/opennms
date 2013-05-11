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

package org.opennms.sms.monitor.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>UniqueNumber class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class UniqueNumber extends BaseSessionVariableGenerator {
	private int min = 0;
	private int max = 1000;
	private static Set<Integer> m_used = new HashSet<Integer>();

	/**
	 * <p>Constructor for UniqueNumber.</p>
	 */
	public UniqueNumber() {
		super();
	}
	
	/**
	 * <p>Constructor for UniqueNumber.</p>
	 *
	 * @param parameters a {@link java.util.Map} object.
	 */
	public UniqueNumber(Map<String,String> parameters) {
		super(parameters);

		if (parameters.containsKey("min")) {
			min = Integer.valueOf(parameters.get("min"));
		}
		if (parameters.containsKey("max")) {
			max = Integer.valueOf(parameters.get("max"));
		}
}
	
	/** {@inheritDoc} */
        @Override
	public void checkIn(String variable) {
		m_used.remove(Integer.valueOf(variable));
	}

	/**
	 * <p>checkOut</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String checkOut() {
		for (int i = min; i < max; i++) {
			if (!m_used.contains(i)) {
				m_used.add(i);
				return String.valueOf(i);
			}
		}
		return null;
	}
}
