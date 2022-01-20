/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.core.schema.migrator;

import org.opennms.core.schema.MigrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class SpringContextBasedMigratorResourceProvider implements MigratorResourceProvider {
    public static final String LIQUIBASE_CHANGELOG_FILENAME = "changelog.xml";
    public static final String LIQUIBASE_CHANGELOG_LOCATION_PATTERN = "classpath*:/" + LIQUIBASE_CHANGELOG_FILENAME;

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(SpringContextBasedMigratorResourceProvider.class);

    private Logger log = DEFAULT_LOGGER;

    private final ApplicationContext m_context;

    private Predicate<Resource> m_liquibaseChangelogFilter = createProductionLiquibaseChangelogFilter();


//========================================
// Constructors
//========================================

    public SpringContextBasedMigratorResourceProvider(ApplicationContext m_context) {
        this.m_context = m_context;
    }


//========================================
// Getters and Setters
//========================================

    public Predicate<Resource> getLliquibaseChangelogFilter() {
        return m_liquibaseChangelogFilter;
    }

    public void setLiquibaseChangelogFilter(Predicate<Resource> m_liquibaseChangelogFilter) {
        this.m_liquibaseChangelogFilter = m_liquibaseChangelogFilter;
    }


//========================================
// Interface
//========================================

    @Override
    public Collection<String> getLiquibaseChangelogs(boolean required) throws Exception {
        List<String> filtered = new LinkedList<>();

        for (Resource resource : m_context.getResources(LIQUIBASE_CHANGELOG_LOCATION_PATTERN)) {
            if ((m_liquibaseChangelogFilter == null) || (m_liquibaseChangelogFilter.test(resource))) {
                filtered.add(resource.getURI().toString());
            } else {
                this.log.debug("Skipping Liquibase changelog that doesn't pass filter: {}", resource);
            }
        }

        if (required && filtered.size() == 0) {
            throw new MigrationException(
                    "Could not find any '" +
                    LIQUIBASE_CHANGELOG_FILENAME +
                    "' files in our classpath using '" +
                    LIQUIBASE_CHANGELOG_LOCATION_PATTERN +
                    "'. Combined ClassPath:" +
                    getContextClassLoaderUrls() +
                    "\nAnd system class loader for fun:" +
                    getSystemClassLoaderUrls()
            );
        }

        return filtered;
    }

//========================================
// Internals
//========================================

    /**
     * Default production filter for selecting changelogs.  Limits changelogs to those with core/schema in the path,
     * or in a jar file with core.schema in the name.
     *
     * @return true => if the changelog is valid for production use; false => otherwise.
     */
    private static Predicate<Resource> createProductionLiquibaseChangelogFilter() {
        return resource -> {
            try {
                URI uri = resource.getURI();
                return (uri.getScheme().equals("file") && uri.toString().contains("core/schema")) ||
                       (uri.getScheme().equals("jar")  && uri.toString().contains("core.schema"));
            } catch (IOException e) {
                return false;
            }
        };
    }

    private String getContextClassLoaderUrls() {
        StringBuilder urls = new StringBuilder();
        for (ApplicationContext c = m_context; c != null; c = c.getParent()) {
            for (ClassLoader cl = c.getClassLoader(); cl != null; cl = cl.getParent()) {
                if (cl instanceof URLClassLoader) {
                    for (URL url : ((URLClassLoader) cl).getURLs()) {
                        urls.append("\n\t");
                        urls.append(url);
                    }
                } else {
                    urls.append("** Could not get URLs from this ClassLoader: ").append(cl);
                }
            }
        }
        return urls.toString();
    }

    private String getSystemClassLoaderUrls() {
        return getClassLoaderUrls(ClassLoader.getSystemClassLoader());
    }

    private String getClassLoaderUrls(ClassLoader classLoader) {
        StringBuilder urls = new StringBuilder();
        for (ClassLoader cl = classLoader; cl != null; cl = cl.getParent()) {
            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    urls.append("\n\t");
                    urls.append(url);
                }
            } else {
                urls.append("** Could not get URLs from this ClassLoader: ").append(cl);
            }
        }
        return urls.toString();
    }}
