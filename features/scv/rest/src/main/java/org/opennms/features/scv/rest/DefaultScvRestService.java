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
package org.opennms.features.scv.rest;

import com.google.common.base.Strings;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.text.Collator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class DefaultScvRestService implements ScvRestService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultScvRestService.class);

    private final SecureCredentialsVault scv;
    private final Pattern pattern = Pattern.compile("\\*{2,}");
    private final String MASKED_PASSWORD = "******";

    public DefaultScvRestService(SecureCredentialsVault scv) {
        this.scv = scv;
    }

    @Override
    public Response getCredentials(String alias) {
        try {
            Credentials credentials = scv.getCredentials(alias);
            if (credentials == null) {
                return Response.noContent().build();
            }
            if (credentials.getUsername() != null && credentials.getPassword() != null) {
                // Mask password.
                CredentialsDTO dto = new CredentialsDTO(alias, credentials.getUsername(), MASKED_PASSWORD);
                // Mask values in attributes.
                credentials.getAttributes().forEach((key, value) -> dto.getAttributes().put(key, MASKED_PASSWORD));
                return Response.ok(dto).build();
            }
        } catch (Exception e) {
            LOG.error("Exception while adding credentials with alias {} ", alias, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.noContent().build();
    }

    @Override
    public Response addCredentials(CredentialsDTO credentialsDTO) {
        if (credentialsDTO != null && !Strings.isNullOrEmpty(credentialsDTO.getAlias())) {
            Credentials existingCredentials = scv.getCredentials(credentialsDTO.getAlias());
            if (existingCredentials != null) {
                Response.status(Response.Status.BAD_REQUEST).entity("alias already exists, use PUT to update").build();
            }
            Credentials credentials = new Credentials(credentialsDTO.getUsername(),
                    credentialsDTO.getPassword(), credentialsDTO.getAttributes());
            try {
                scv.setCredentials(credentialsDTO.getAlias(), credentials);
                return Response.accepted().build();
            } catch (Exception e) {
                LOG.error("Exception while adding credentials with alias {}", credentialsDTO.getAlias(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Credentials").build();
        }
    }

    @Override
    public Response editCredentials(String alias, CredentialsDTO credentialsDTO) {

        if (credentialsDTO != null && !Strings.isNullOrEmpty(alias) && alias.equals(credentialsDTO.getAlias())) {
            Credentials credentials = scv.getCredentials(alias);
            Map<String, String> attributes = new HashMap<>(credentialsDTO.getAttributes());
            Map<String, String> existingAttributes = credentials != null ? new HashMap<>(credentials.getAttributes()) : new HashMap<>();
            credentialsDTO.getAttributes().forEach((key, value) -> {
                // If the value is masked, retrieve that value from existing credentials
                if (!pattern.matcher(value).matches()) {
                    attributes.put(key, value);
                } else {
                    attributes.put(key, existingAttributes.get(key));
                }
            });
            // If password is masked, we are using username and password from existing credentials.
            if (pattern.matcher(credentialsDTO.getPassword()).matches()) {
                String username = credentials != null ? credentials.getUsername() : null;
                String password = credentials != null ? credentials.getPassword() : null;
                credentials = new Credentials(username, password, attributes);
            } else {
                credentials = new Credentials(credentialsDTO.getUsername(),
                        credentialsDTO.getPassword(), attributes);
            }
            try {
                scv.setCredentials(alias, credentials);
                return Response.accepted().build();
            } catch (Exception e) {
                LOG.error("Exception while adding credentials with alias {}", credentialsDTO.getAlias(), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Credentials").build();
        }
    }

    @Override
    public Response deleteCredentials(final String alias) {
        try {
            scv.deleteCredentials(alias);
        } catch (Exception e) {
            LOG.error("Exception while deleting credentials with alias {} ", alias, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.noContent().build();
    }

    @Override
    public Response getAliases() {
        Set<String> aliasSet = scv.getAliases();
        var aliases = new TreeSet<String>(Collator.getInstance());
        aliases.addAll(aliasSet);
        return Response.ok(aliases).build();
    }
}
