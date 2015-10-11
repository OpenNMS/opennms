/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.commands;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.FilterCriteria;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.MBeanServerQuery;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.MBeanServerQueryException;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.QueryResult;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;
import java.util.List;

/**
 * Implements the "query" command functionality.
 */
public class QueryCommand extends JmxCommand {
    @Option(name="--show-empty",
            usage = "Includes MBeans, even if they do not have Attributes." +
            " Either due to the <filter criteria> or while there are none.")
    private boolean all;

    @Option(name="--ignore",
            usage = "Set <filter criteria> to ignore while running.")
    private List<String> ignoreFilter;

    @Argument(metaVar = "<filter criteria>",
            usage="A filter criteria to query the MBeanServer for. " +
            "The format is <objectname>[:attribute name]. " +
            "The <objectname> accepts the default JMX object name pattern to identify the MBeans to be retrieved. " +
            "If null all domains are shown. If no key properties are specified, the domain's MBeans are retrieved. " +
            "To execute for certain attributes, you have to add \":<attribute name>\". The <attribute name> accepts " +
            "regular expressions. When multiple <filter criteria> are provided they are OR concatenated. ")
    private List<String> filter;

    @Option(name="--ids-only", usage="Only show the ids of the attributes.")
    private boolean idOnlyFlag;

    @Option(name="--include-values", usage="Include attribute values.", required = false)
    private boolean includeValues = false;

    @Option(name="--show-domains", usage="Only lists the available domains.")
    private boolean domainOnlyFlag = true;

    @Override
    protected void execute(MBeanServerConnection mbeanServerConnection) throws MBeanServerQueryException, IOException {
        if (domainOnlyFlag && (filter == null || filter.isEmpty())) {
            for (String eachDomain : mbeanServerConnection.getDomains()) {
                LOG.info(eachDomain);
            }
            return;
        }

        MBeanServerQuery queryBuilder = new MBeanServerQuery()
                .withFilters(filter)
                .withIgnoresFilter(ignoreFilter)
                .fetchValues(includeValues)
                .showMBeansWithoutAttributes(all)
                .sort(true);

        QueryResult result = queryBuilder.execute(mbeanServerConnection);
        if (idOnlyFlag) {
            for (QueryResult.MBeanResult eachResult : result.getMBeanResults()) {
                for (MBeanAttributeInfo eachAttributeInfo : eachResult.attributeResult.attributes) {
                    LOG.info(toAttributeId(eachResult.objectName, eachAttributeInfo));
                }
            }
        } else {
            prettyPrint(result);
        }
    }

    private void prettyPrint(QueryResult result) {
        for (QueryResult.MBeanResult eachMBeanResult : result.getMBeanResults()) {
            MBeanInfo mbeanInfo = eachMBeanResult.mbeanInfo;
            ObjectName objectName = eachMBeanResult.objectName;
            QueryResult.AttributeResult attributeResult = eachMBeanResult.attributeResult;

            LOG.info(String.format("%s", objectName));
            LOG.info(String.format("\tdescription: %s", toString(mbeanInfo.getDescription())));
            LOG.info(String.format("\tclass name: %s", mbeanInfo.getClassName()));
            LOG.info(String.format(
                    "\tattributes: (%d/%d)",
                    attributeResult.attributes.size(),
                    attributeResult.totalCount));

            for (MBeanAttributeInfo eachAttribute : attributeResult.attributes) {
                LOG.info(String.format("\t\t%s", eachAttribute.getName()));
                LOG.info(String.format("\t\t\tid: %s", toAttributeId(objectName, eachAttribute)));
                LOG.info(String.format("\t\t\tdescription: %s", toString(eachAttribute.getDescription())));
                LOG.info(String.format("\t\t\ttype: %s", eachAttribute.getType()));
                LOG.info(String.format("\t\t\tisReadable: %s", eachAttribute.isReadable()));
                LOG.info(String.format("\t\t\tisWritable: %s", eachAttribute.isWritable()));
                LOG.info(String.format("\t\t\tisIs: %s", eachAttribute.isIs()));
                if (includeValues) {
                    LOG.info(String.format("\t\t\tvalue: %s", toString(attributeResult.getValue(eachAttribute))));
                }
            }
        }

        if (filter != null && !filter.isEmpty()) {
            LOG.info(String.format("Your query '%s' shows %d/%d MBeans.",
                    filter,
                    result.getMBeanResults().size(),
                    result.getTotalMBeanCount()));
        } else {
            LOG.info(String.format("There are %d registered MBeans", result.getTotalMBeanCount()));
        }

        if (ignoreFilter != null && !ignoreFilter.isEmpty()) {
            LOG.info(String.format("While querying, the following query was used to exclude MBeans/Attributes: '%s'", ignoreFilter));
        }
    }

    @Override
    public void printUsage() {
        super.printUsage();
        LOG.info("");
        LOG.info("Examples:");
        LOG.info(" Querying: java-jar JmxConfigGenerator.jar query --host localhost --port 7199 [--ids-only] [--show-domains] [--ignore <filter criteria>] [--include-values] <filter criteria>");
        LOG.info(" Show domains: java-jar JmxConfigGenerator.jar query --host localhost --port 7199 [--show-domains]");
        LOG.info(" Show all MBeans/Attributes of a domain: java-jar JmxConfigGenerator.jar query --host localhost --port 7199 org.opennms.domain1:*");
        LOG.info(" Show all MBeans/Attributes of a domain, but exclude MBeans/Attributes of another: java-jar JmxConfigGenerator.jar query --host localhost --port 7199 org.opennms.domain1:* --ignore java.lang*:*");
        LOG.info(" Only show ids: java -jar JmxConfigGenerator.jar query --host localhost --port 7199 --ids-only");
        LOG.info(" Query for certain Attributes: java -jar JmxConfigGenerator.jar query --host localhost --port 7199 org.opennms.domain1:Status.*");
    }

    @Override
    protected String getDescription() {
        return "Enables querying the MBeanServer for certain MBeans/attributes.";
    }

    private String cut(String input, int length) {
        if (input.length() > length) {
            return input.substring(0, length);
        }
        return input;
    }

    // Some Values contain line breaks, we remove them and cut the length
    private String toString(Object input) {
        if (input != null && input instanceof String) {
            return cut(input.toString().replaceAll("\n", ""), 200);
        }
        return input == null ? null : input.toString();
    }

    private String toAttributeId(ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        return new FilterCriteria(objectName.toString(), attributeInfo.getName()).toString();
    }
}
