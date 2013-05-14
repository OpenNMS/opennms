/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.web.servlet;

/**
 * <p>MissingParameterException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MissingParameterException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -3100193382920197884L;

    protected String missingParameter;

    protected String[] requiredParameters;

    /**
     * <p>Constructor for MissingParameterException.</p>
     *
     * @param missingParameter a {@link java.lang.String} object.
     */
    public MissingParameterException(String missingParameter) {
        this(missingParameter, new String[] { missingParameter });
    }

    /**
     * <p>Constructor for MissingParameterException.</p>
     *
     * @param missingParameter a {@link java.lang.String} object.
     * @param requiredParameters an array of {@link java.lang.String} objects.
     */
    public MissingParameterException(String missingParameter, String[] requiredParameters) {
        if (missingParameter == null || requiredParameters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.missingParameter = missingParameter;
        this.requiredParameters = requiredParameters;
    }

    /**
     * <p>Getter for the field <code>missingParameter</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMissingParameter() {
        return (this.missingParameter);
    }

    /**
     * <p>Getter for the field <code>requiredParameters</code>.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRequiredParameters() {
        return (this.requiredParameters);
    }

    /**
     * <p>getMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getMessage() {
	StringBuffer b = new StringBuffer();

        b.append("Missing parameter \"" + getMissingParameter()
                 + "\" out of required parameters: ");

        String[] requiredParameters = getRequiredParameters();
	for (int i = 0; i < requiredParameters.length; i++) {
	    if (i != 0) {
                b.append(", ");
	    }
            b.append(requiredParameters[i]);
	}

	return b.toString();
    }
}
