/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools.validation;

import org.opennms.gwt.web.ui.asset.client.AssetPageConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus
 *         Neumann</a></br> Validates if the string representation of given
 *         object is parseable to an {@link Date}. The expected format is
 *         "yyyy-MM-dd".
 */
public class StringDateLocalValidator implements Validator {
	private AssetPageConstants con = GWT.create(AssetPageConstants.class);

	private final DateTimeFormat m_formater = DateTimeFormat.getFormat("yyyy-MM-dd");

	/**
	 * Validates if the string representation of given object is parseable to an
	 * {@link Date}. The expected format is "yyyy-MM-dd". The given object will
	 * be casted by <code>(String) object</code>.
	 * 
	 * @param object
	 */
	@Override
	public String validate(Object object) {

	    String dateString = "";
		try {
			dateString = (String) object;
		} catch (Exception e) {
			// GWT.LOG("DATETIMEFORMATVALIDATOR: CAN'T CAST OBJECT: " + OBJECT +
			// " TO STRING");
			// validator can't cast given object to string.
			// it's a STRING-validator... so nothing happens
		}

		if (dateString.equals("")) {
			return "";
		}

		try {
			// GWT.log("DateTimeFormatValidator: m_DateSting: " + m_dateString);
			m_formater.parseStrict(dateString);
		} catch (Exception e) {
			// GWT.log("DateTimeFormatValidator: m_DateSting: " + m_dateString +
			// " can't be formated by m_formater; " + "yyyy-MM-dd");
			return con.stringNotADate();
		}
		return "";
	}
}
