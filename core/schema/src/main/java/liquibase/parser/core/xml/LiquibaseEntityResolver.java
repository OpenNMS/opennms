/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
// TODO: Patrick: remove license, just needed for our license checker

// TODO: Patrick: remove when new liquibase version (4.4.4) is released.
//  Copied from: https://github.com/liquibase/liquibase/commit/aeb34c9c91748926b207da18eeed14dca971b73a
// See also: https://github.com/liquibase/liquibase/issues/1815
package liquibase.parser.core.xml;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;

/**
 * Finds the Liquibase schema from the classpath rather than fetching it over the Internet.
 * Also resolve external entities using a resourceAccessor if it's provided
 */
public class LiquibaseEntityResolver implements EntityResolver2 {

    private static ClassLoaderResourceAccessor fallbackResourceAccessor;

    @Override
    @java.lang.SuppressWarnings("squid:S2095")
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        log.fine("Resolving XML entity name='" + name + "', publicId='" + publicId + "', baseURI='" + baseURI + "', systemId='" + systemId + "'");

        if (systemId == null) {
            log.fine("Cannot determine systemId for name=" + name + ", publicId=" + publicId + ". Will load from network.");
            return null;
        }

        String path = systemId.toLowerCase()
                .replace("http://www.liquibase.org/xml/ns/migrator/", "http://www.liquibase.org/xml/ns/dbchangelog/")
                .replaceFirst("https?://", "");


        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        InputStreamList streams = resourceAccessor.openStreams(null, path);
        if (streams.isEmpty()) {
            streams = getFallbackResourceAccessor().openStreams(null, path);

            if (streams.isEmpty()) {
                log.fine("Unable to resolve XML entity locally. Will load from network.");
                return null;
            }
        } else if (streams.size() == 1) {
            log.fine("Found XML entity at " + streams.getURIs().get(0));
        } else if (streams.size() > 1) {
            log.warning("Found " + streams.size() + " copies of " + systemId + ". Using " + streams.getURIs().get(0));
        }
        InputStream stream = streams.iterator().next();

        org.xml.sax.InputSource source = new org.xml.sax.InputSource(stream);
        source.setPublicId(publicId);
        source.setSystemId(systemId);

        return source;

    }

    /**
     * ResourceAccessor to use if the standard one does not have the XSD files in it.
     * Returns a ClassLoaderResourceAccessor that checks the system classloader which should include the liquibase.jar.
     */
    protected ResourceAccessor getFallbackResourceAccessor() {
        if (fallbackResourceAccessor == null) {
            fallbackResourceAccessor = new ClassLoaderResourceAccessor();
        }
        return fallbackResourceAccessor;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        Scope.getCurrentScope().getLog(getClass()).warning("Current XML parsers seems to not support EntityResolver2. External entities won't be correctly loaded");
        return resolveEntity(null, publicId, null, systemId);
    }
}
