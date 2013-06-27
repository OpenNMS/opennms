/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.outage;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.extremecomponents.table.bean.Column;
import org.extremecomponents.table.core.TableModel;
import org.extremecomponents.util.ExtremeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

/**
 * Filter Predicate implementation which enable date comparison.
 *
 * Based on example code from: @author Nathan MA
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DateFilterPredicate implements Predicate
{
	
	private static final Logger LOG = LoggerFactory.getLogger(DateFilterPredicate.class);

    /** less than or equal. usage: <= 18-12-1997 */
    public static final String LESS_THAN_OR_EQUAL = "<=";

    /** greater than or equal. usage: >= 18-12-1997 */
    public static final String GREATER_THAN_OR_EQUAL = ">=";

    /** date between. usage: <> 18-12-1997 19-12-1997 */
    public static final String BETWEEN = "<>";

    /** date not equal. ussage: != 18-12-2004 */
    public static final String NOT_EQUAL = "!=";

    /** delimiters */
    public static final String DELIM = "\\s";
    
    private static final String asterisk = "*";
    private static final String emptyString = "";
    private TableModel model;

    /**
     * Creates a new DateFilterPredicate object.
     *
     * @param model table model
     */
    public DateFilterPredicate(TableModel model)
    {
    	this.model = model;
    }

    /**
     * {@inheritDoc}
     *
     * Use the filter parameters to filter out the table.
     */
    @Override
    public boolean evaluate(Object bean)
    {
        boolean match = false;

        try
        {
            @SuppressWarnings("unchecked")
            Iterator<Column> iter = model.getColumnHandler().getColumns().iterator();

            while (iter.hasNext())
            {
                Column column = iter.next();
                String alias = column.getAlias();
                String filterValue = model.getLimit().getFilterSet()
                                          .getFilterValue(alias);

                if (StringUtils.isEmpty(filterValue))
                {
                    continue;
                }

                String property = column.getProperty();
                Object value = PropertyUtils.getProperty(bean, property);

                if (value == null)
                {
                    continue;
                }

                if (column.isDate())
                {
                    Locale locale = model.getLocale();
                    value = ExtremeUtils.formatDate(column.getParse(),
                            column.getFormat(), value, locale);
                }
                else if (column.isCurrency())
                {
                    Locale locale = model.getLocale();
                    value = ExtremeUtils.formatNumber(column.getFormat(),
                            value, locale);
                }

                if (!isSearchMatch(value, filterValue, column.isDate(),
                            column.getFormat(), model.getLocale()))
                {
                    match = false; // as soon as fail just short circuit

                    break;
                }

                match = true;
            }
        }
        catch (Throwable e)
        {
            LOG.error("FilterPredicate.evaluate() had problems", e);
        }

        return match;
    }

    private boolean isSearchMatch(Object value, String search, boolean isDate,
        String format, Locale locale)
    {
        String valueStr = value.toString().toLowerCase().trim();
        search = search.toLowerCase().trim();

        if (search.startsWith(asterisk) &&
                valueStr.endsWith(StringUtils.replace(search, asterisk,
                        emptyString)))
        {
            return true;
        }
        else if (search.endsWith(asterisk) &&
                valueStr.startsWith(StringUtils.replace(search, asterisk,
                        emptyString)))
        {
            return true;
        }
        else if (isDate)
        {
            DateFormat dateFormat = new SimpleDateFormat(format, locale);

            Date dateToCompare = null;
            Date dateToCompare2 = null;

            try
            {
                Date dateValue = dateFormat.parse(value.toString());

                String[] result = search.split(DELIM);

                String operator = result[0];

                if (operator.equals(LESS_THAN_OR_EQUAL))
                {
                    dateToCompare = dateFormat.parse(result[1]);

                    return dateValue.getTime() <= dateToCompare.getTime();
                }
                else if (operator.equals(GREATER_THAN_OR_EQUAL))
                {
                    dateToCompare = dateFormat.parse(result[1]);

                    return dateValue.getTime() >= dateToCompare.getTime();
                }
                else if (operator.equals(BETWEEN))
                {
                    dateToCompare = dateFormat.parse(result[1]);
                    dateToCompare2 = dateFormat.parse(result[2]);

                    return (dateValue.getTime() >= dateToCompare.getTime()) &&
                    (dateValue.getTime() <= dateToCompare2.getTime());
                }
                else if (operator.equals(NOT_EQUAL))
                {
                    dateToCompare = dateFormat.parse(result[1]);

                    return dateValue.getTime() != dateToCompare.getTime();
                }
                else
                {
                    return StringUtils.contains(valueStr, search);
                }
            }
            catch (Throwable e)
            {
                LOG.error("The parse was incorrectly defined for date String [{}].", search);

                // date comparions failed. Campare it as normal string.
                return StringUtils.contains(valueStr, search);
            }
        }
        else if (StringUtils.contains(valueStr, search))
        {
            return true;
        }

        return false;
    }
}
