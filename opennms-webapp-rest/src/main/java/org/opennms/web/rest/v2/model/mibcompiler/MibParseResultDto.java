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
package org.opennms.web.rest.v2.model.mibcompiler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Common result shape for MIB compiler operations that parse a MIB:
 * on failure {@code errors} carries the parser's formatted error output and
 * {@code missingDependencies} the modules that could not be resolved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MibParseResultDto {

    private boolean success;
    private String mibName;
    private String errors;
    private List<String> missingDependencies;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMibName() {
        return mibName;
    }

    public void setMibName(String mibName) {
        this.mibName = mibName;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public void setMissingDependencies(List<String> missingDependencies) {
        this.missingDependencies = missingDependencies;
    }
}
