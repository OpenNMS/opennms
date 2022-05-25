/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
    public Response getAliases() {
        Set<String> aliasSet = scv.getAliases();
        var aliases = new TreeSet<String>(Collator.getInstance());
        aliases.addAll(aliasSet);
        return Response.ok(aliases).build();
    }
}
