//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 24: Add serialVersionUID. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web;

/**
 * <p>MissingParameterException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MissingParameterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

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
