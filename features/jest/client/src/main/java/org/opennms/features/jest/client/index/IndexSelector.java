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
package org.opennms.features.jest.client.index;

import static org.opennms.features.jest.client.index.IndexStrategy.DAILY;
import static org.opennms.features.jest.client.index.IndexStrategy.HOURLY;
import static org.opennms.features.jest.client.index.IndexStrategy.MONTHLY;
import static org.opennms.features.jest.client.index.IndexStrategy.YEARLY;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import org.opennms.features.jest.client.template.IndexSettings;

import com.google.common.base.Strings;

public class IndexSelector {

    private final static ZoneId UTC = TimeZone.getTimeZone("UTC").toZoneId();
    private final static Map<IndexStrategy, TemporalUnit> UNIT_MAP;

    static {
        UNIT_MAP = new HashMap<>();
        UNIT_MAP.put(YEARLY, ChronoUnit.YEARS);
        UNIT_MAP.put(MONTHLY, ChronoUnit.MONTHS);
        UNIT_MAP.put(DAILY, ChronoUnit.DAYS);
        UNIT_MAP.put(HOURLY, ChronoUnit.HOURS);

    }

    private final IndexSettings indexSettings;
    private final String prefix;
    private final IndexStrategy strategy;
    private final TemporalUnit unit;
    private final long expandTimeRangeInMs;

    public IndexSelector(IndexSettings indexSettings, String prefix, IndexStrategy strategy, long expandTimeRangeInMs) {
        this.indexSettings = Objects.requireNonNull(indexSettings);
        this.prefix = prefix;
        this.strategy = strategy;
        this.unit = UNIT_MAP.get(strategy);
        if (unit == null) {
            // should never happen
            throw new UnsupportedOperationException("This is a programming mistake, please check the mapping for strategy="
                    + strategy.name());
        }
        this.expandTimeRangeInMs = expandTimeRangeInMs;
    }

    /**
     * We assume that timeRange is valid:
     * - not Null
     * - start and end date is positive
     * - start date <= end date
     * <p>
     * We need to be aware that the indexes are part of the GET URL and therefor we can't have too many, it will result
     * in  a HTTP 400 from elasticsearch. Therefor we collapse the indexes with wildcards where applicable</p>
     */
    public List<String> getIndexNames(long start, long end) {
        List<String> all = new ArrayList<>();
        // we expand the time range by a bit in order to be sure to find all relevant events:
        Instant endDate = adjustEndTime(new Date(end + expandTimeRangeInMs));
        Instant startDate = Instant.ofEpochMilli(start - expandTimeRangeInMs);
        Instant currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            String index = strategy.getIndex(indexSettings, prefix, currentDate);
            all.add(index);
            currentDate = plusOne(currentDate);
        }

        // collapse the indexes in order to reduce the length of the URL:
        String elementAfterSequence = strategy.getIndex(indexSettings, prefix, currentDate);
        String elementBeforeSequence = strategy.getIndex(indexSettings, prefix, minusOne(startDate));

        return collapseList(all, elementBeforeSequence, elementAfterSequence, 0);
    }

    private Instant minusOne(Instant date){
        LocalDateTime current = LocalDateTime.ofInstant(date, UTC)
                .minus(1L, unit).withMinute(0).withSecond(0);
        return current.atZone(UTC).toInstant();
    }

    private Instant plusOne(Instant date){
        LocalDateTime current = LocalDateTime.ofInstant(date, UTC)
                .plus(1L, unit).withMinute(0).withSecond(0);
        return current.atZone(UTC).toInstant();
    }

    private List<String> collapseList(final List<String> orgList,
                                      String elementBeforeSequence,
                                      String elementAfterSequence,
                                      int offset) {
        if (orgList.size() < 2) {
            // nothing to do
            return orgList;
        }
        int indexPrefixLen = Strings.isNullOrEmpty(indexSettings.getIndexPrefix()) ? 0 : indexSettings.getIndexPrefix().length();
        int collapseAfter = indexPrefixLen + prefix.length() + strategy.getPattern().length() - 1 + offset;
        boolean beginningIsSameAsEnd = orgList.get(0).substring(0, collapseAfter)
                .equals(orgList.get(orgList.size() - 1).substring(0, collapseAfter));
        boolean doCollapsingAtBeginning = !elementBeforeSequence.startsWith(orgList.get(0).substring(0, collapseAfter));
        boolean doCollapsingAtEnd = !elementAfterSequence.startsWith(orgList.get(orgList.size() - 1).substring(0, collapseAfter));
        doCollapsingAtBeginning = (doCollapsingAtBeginning && doCollapsingAtEnd) || (doCollapsingAtBeginning && !beginningIsSameAsEnd);

        return collapseList(orgList,
                doCollapsingAtBeginning,
                doCollapsingAtEnd, 0);
    }

    private List<String> collapseList(final List<String> orgList,
                                      boolean doCollapsingAtBeginning,
                                      boolean doCollapsingAtEnd,
                                      int offset) {
        if (orgList.size() < 2) {
            // nothing to do
            return orgList;
        }

        List<String> collapsedList = orgList;
        int indexPrefixLen = Strings.isNullOrEmpty(indexSettings.getIndexPrefix()) ? 0 : indexSettings.getIndexPrefix().length();
        int collapseAfter = indexPrefixLen + prefix.length() + strategy.getPattern().length() - 1 + offset;

        collapsedList = StringCollapser
                .forList(collapsedList).collapseAfterChars(collapseAfter)
                .replaceCollapsedCharsWith("*")
                .doCollapsingAtBeginning(doCollapsingAtBeginning)
                .doCollapsingAtEnd(doCollapsingAtEnd)
                .collapse();

        if (collapseAfter > prefix.length() + IndexStrategy.YEARLY.getPattern().length()) {
            collapsedList = collapseList(collapsedList,
                    false,
                    false,
                    offset - 3);
        }

        return collapsedList;
    }

    private Instant adjustEndTime(Date date){
        LocalDateTime current = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()),
                UTC).withMinute(59).withSecond(59).withNano(999999999);
        if(this.strategy == IndexStrategy.YEARLY){
            current = current.withMonth(12);
        }
        if(this.strategy == IndexStrategy.MONTHLY
                || this.strategy == IndexStrategy.YEARLY){
            current = current.with(TemporalAdjusters.lastDayOfMonth());
        }
        if(this.strategy == IndexStrategy.DAILY
                || this.strategy == IndexStrategy.MONTHLY
                || this.strategy == IndexStrategy.YEARLY){
            current = current.withHour(23);
        }
        return current.atZone(UTC).toInstant();
    }
}
