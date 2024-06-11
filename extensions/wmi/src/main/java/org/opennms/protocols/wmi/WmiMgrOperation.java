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
package org.opennms.protocols.wmi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>WmiMgrOperation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public enum WmiMgrOperation {
	EQ(0),
	NEQ(1),
	GT(2),
	LT(3);

	private int m_OperationType;

	WmiMgrOperation(final int opType) {
		m_OperationType = opType;
	}

	/**
	 * <p>getOpNumber</p>
	 *
	 * @return a int.
	 */
	public int getOpNumber() {
		return (m_OperationType);
	}

	/**
	 * <p>compareString</p>
	 *
	 * @param comp1 a {@link java.lang.Object} object.
	 * @param comp2 a {@link java.lang.String} object.
	 * @return a boolean.
	 * @throws org.opennms.protocols.wmi.WmiException if any.
	 */
	public boolean compareString(final Object comp1, final String comp2)
			throws WmiException {
		if (comp1 instanceof String) {

			try {
			    final DateFormat fmt2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				final Date date1 = WmiClient.convertWmiDate((String) comp1);
				final Date date2 = fmt2.parse(comp2);

				return compareToDate(date1, date2);
			} catch (ParseException e) {
				// ignore this exception and continue with string comparison.
			}
			return compareToString((String) comp1, comp2);
		} else if (comp1 instanceof Integer) {
		    final Integer compInt1 = (Integer) comp1;
			final Integer compInt2 = Integer.parseInt(comp2);

			return compareToInteger(compInt1, compInt2);
		} else if (comp1 instanceof Boolean) {
		    final Boolean bool1 = (Boolean) comp1;
			final Boolean bool2 = Boolean.parseBoolean(comp2);

			return compareToBoolean(bool1, bool2);
		} else if (comp1 instanceof Float) {
		    final Float fl1 = (Float) comp1;
			final Float fl2 = Float.parseFloat(comp2);

			return compareToFloat(fl1, fl2);
		} else if (comp1 instanceof Date) {
		    final DateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			final Date date1 = (Date) comp1;
			Date date2;
			try {
				date2 = fmt.parse(comp2);
			} catch (ParseException e) {
				throw new WmiException("Parsing date '" + comp2 + "' failed: " + e.getMessage(), e);
			}

			return compareToDate(date1, date2);

		}

		// No root type found. Return false. Maybe instead we should throw an exception.
		// A potential bug to fix?
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToString(final String comp1, final String comp2) {
		switch (this) {
		case EQ:
			return (comp2.equals(comp1));
		case NEQ:
			return !(comp2.equals(comp1));
		case GT:
			return (comp2.length() < ((String) comp1).length());
		case LT:
			return (comp2.length() > ((String) comp1).length());
		}

		// catch-all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToInteger(final Integer comp1, final Integer comp2) {
		switch (this) {
		case EQ:
			if (comp2.compareTo(comp1) == 0) {
				return true;
			} else {
				return false;
			}
		case NEQ:
			if (comp2.compareTo(comp1) != 0) {
				return true;
			} else {
				return false;
			}
		case GT:
			if (comp2.compareTo(comp1) < 0) {
				return true;
			} else {
				return false;
			}
		case LT:
			if (comp2.compareTo(comp1) > 0) {
				return true;
			} else {
				return false;
			}
		}

		// catch all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToBoolean(final Boolean bool1, final Boolean bool2) {
		switch (this) {
		case EQ:
			return bool1.equals(bool2);
		case NEQ:
		case GT:
		case LT:
			return !(bool1.equals(bool2));
		}
		
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToFloat(final Float comp1, final Float comp2) {
		switch (this) {
		case EQ:
			if (comp2.compareTo(comp1) == 0) {
				return true;
			} else {
				return false;
			}
		case NEQ:
			if (comp2.compareTo(comp1) != 0) {
				return true;
			} else {
				return false;
			}
		case GT:
			if (comp2.compareTo(comp1) < 0) {
				return true;
			} else {
				return false;
			}
		case LT:
			if (comp2.compareTo(comp1) > 0) {
				return true;
			} else {
				return false;
			}
		}

		// catch all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}

	private boolean compareToDate(final Date date1, final Date date2) {
		switch (this) {
		case EQ:
			if (date1.equals(date2)) {
				return true;
			}
		case NEQ:
			if (!(date1.equals(date2))) {
				return true;
			}
		case GT:
			if (date1.after(date2)) {
				return true;
			}
		case LT:
			if (date1.before(date2)) {
				return true;
			}
		}

		// catch-all
		// TODO maybe throwing an exception would be more useful?
		return false;
	}
}
