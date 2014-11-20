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

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         Validates a String against a regexp.
 */
public class StringRegexpValidator implements Validator {
	private AssetPageConstants con = GWT.create(AssetPageConstants.class);
	private String regexp;

	public StringRegexpValidator(String regexp) {
		this.regexp = regexp;
	}

	/**
	 * The regexp used to validate.
	 * 
	 * @return String regexp
	 */
	public String getRegexp() {
		return regexp;
	}

	/**
	 * Validates length of a String against a maximum. Validation will run
	 * against <code>(String) object</code> so make sure to use a proper object.
	 * 
	 * @param object
	 */
	@Override
	public String validate(Object object) {
		if (!((String) object).matches(regexp)) {
			return con.stringNotMatchingRegexpError() + " " + regexp;
		}
		return "";
	}
}
