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
package org.opennms.features.datachoices.internal.userdatacollection;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataCollectionServiceImpl implements UserDataCollectionService {
    private static final Logger LOG = LoggerFactory.getLogger(UserDataCollectionServiceImpl.class);

    private UserDataCollectionSubmissionClient client;

    /**
     * The form with user data collection info has been received; validate and send to
     * OpenNMS Stats endpoint for further processing.
     * Does not update User Data Collection status in Config Management, client will make
     * a separate call for that.
     */
    public void submit(UserDataCollectionFormData data) throws Exception, IOException {
        var submissionData = createSubmissionData(data);
        String json = jsonSerialize(submissionData);

        try {
            client.postForm(json);
        } catch (Exception e) {
            throw e;
        }
    }

    private UserDataCollectionSubmissionData createSubmissionData(UserDataCollectionFormData data) {
        var submissionData = new UserDataCollectionSubmissionData();
        submissionData.consent = true;
        submissionData.firstName = data.firstName;
        submissionData.lastName = data.lastName;
        submissionData.email = data.email;
        submissionData.company = data.company;
        submissionData.product = "Horizon";
        submissionData.systemId = "";

        return submissionData;
    }

    private String jsonSerialize(UserDataCollectionSubmissionData data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY);
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);

        try {
            return mapper.writeValueAsString(data);
        } catch (IOException e) {
            LOG.error("Error serializing submission Json data", e);
            throw e;
        }
    }

    public void setClient(UserDataCollectionSubmissionClient client) {
        this.client = client;
    }
}
