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
package org.opennms.web.servlet;

import java.util.Arrays;

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
        this.requiredParameters = Arrays.copyOf(requiredParameters, requiredParameters.length);
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
        final StringBuilder b = new StringBuilder();

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
