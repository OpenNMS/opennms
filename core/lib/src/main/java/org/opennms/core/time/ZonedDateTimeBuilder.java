/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.time;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is basically a {@link Calendar.Builder} replacement that
 * builds Java 8 {@link ZonedDateTime} instances.
 * 
 * @author Seth
 */
public class ZonedDateTimeBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ZonedDateTimeBuilder.class);

    private static final Map<String,String> TIME_ZONE_MAPPINGS = new HashMap<>();

    static {
        // Add all of the standard (?) Java short IDs
        TIME_ZONE_MAPPINGS.putAll(ZoneId.SHORT_IDS);

        // Add all standard (?) UTC offset abbreviations.
        //
        // When there are duplicates, make a best effort
        // to choose the most popular variant.
        //
        // Note that most of the abbreviations map to a
        // specific UTC offset but a few do map to a zone.

        TIME_ZONE_MAPPINGS.put("A", "+01:00");
        TIME_ZONE_MAPPINGS.put("ACDT", "+10:30");
        TIME_ZONE_MAPPINGS.put("ACST", "+09:30");
        // Acre Time
        //TIME_ZONE_MAPPINGS.put("ACT", "-05:00");
        // Australian Central Time
        TIME_ZONE_MAPPINGS.put("ACT", "Australia/Darwin");
        TIME_ZONE_MAPPINGS.put("ACWST", "+08:45");
        // Arabia Daylight Time
        //TIME_ZONE_MAPPINGS.put("ADT", "+03:00");
        // Atlantic Daylight Time
        TIME_ZONE_MAPPINGS.put("ADT", "-03:00");
        TIME_ZONE_MAPPINGS.put("AEDT", "+11:00");
        TIME_ZONE_MAPPINGS.put("AEST", "+10:00");
        TIME_ZONE_MAPPINGS.put("AET", "Australia/Sydney");
        TIME_ZONE_MAPPINGS.put("AFT", "+04:30");
        TIME_ZONE_MAPPINGS.put("AKDT", "-08:00");
        TIME_ZONE_MAPPINGS.put("AKST", "-09:00");
        TIME_ZONE_MAPPINGS.put("ALMT", "+06:00");
        // Amazon Summer Time
        TIME_ZONE_MAPPINGS.put("AMST", "-03:00");
        // Armenia Summer Time (unused)
        //TIME_ZONE_MAPPINGS.put("AMST", "+05:00");
        // Amazon Time
        TIME_ZONE_MAPPINGS.put("AMT", "-04:00");
        // Armenia Time (unused)
        //TIME_ZONE_MAPPINGS.put("AMT", "+04:00");
        TIME_ZONE_MAPPINGS.put("ANAST", "+12:00");
        TIME_ZONE_MAPPINGS.put("ANAT", "+12:00");
        TIME_ZONE_MAPPINGS.put("AQTT", "+05:00");
        TIME_ZONE_MAPPINGS.put("ART", "-03:00");
        // Arabia Standard Time
        //TIME_ZONE_MAPPINGS.put("AST", "+03:00");
        // Atlantic Standard Time
        TIME_ZONE_MAPPINGS.put("AST", "-04:00");
        // I'm going to just ignore this since it is covered by AST/ADT
        //TIME_ZONE_MAPPINGS.put("AT", "-04:00 / -3:00");
        TIME_ZONE_MAPPINGS.put("AWDT", "+09:00");
        TIME_ZONE_MAPPINGS.put("AWST", "+08:00");
        TIME_ZONE_MAPPINGS.put("AZOST", "+00:00");
        TIME_ZONE_MAPPINGS.put("AZOT", "-01:00");
        TIME_ZONE_MAPPINGS.put("AZST", "+05:00");
        TIME_ZONE_MAPPINGS.put("AZT", "+04:00");
        TIME_ZONE_MAPPINGS.put("AoE", "-12:00");
        TIME_ZONE_MAPPINGS.put("B", "+02:00");
        TIME_ZONE_MAPPINGS.put("BNT", "+08:00");
        TIME_ZONE_MAPPINGS.put("BOT", "-04:00");
        TIME_ZONE_MAPPINGS.put("BRST", "-02:00");
        TIME_ZONE_MAPPINGS.put("BRT", "-03:00");
        // Bangladesh Standard Time
        //TIME_ZONE_MAPPINGS.put("BST", "+06:00");
        // Bougainville Standard Time
        //TIME_ZONE_MAPPINGS.put("BST", "+11:00");
        // British Summer Time
        TIME_ZONE_MAPPINGS.put("BST", "+01:00");
        TIME_ZONE_MAPPINGS.put("BTT", "+06:00");
        TIME_ZONE_MAPPINGS.put("C", "+03:00");
        TIME_ZONE_MAPPINGS.put("CAST", "+11:00");
        TIME_ZONE_MAPPINGS.put("CAT", "+02:00");
        TIME_ZONE_MAPPINGS.put("CCT", "+06:30");
        // Central Daylight Time
        TIME_ZONE_MAPPINGS.put("CDT", "-05:00");
        // Cuba Daylight Time
        //TIME_ZONE_MAPPINGS.put("CDT", "-04:00");
        TIME_ZONE_MAPPINGS.put("CEST", "+02:00");
        TIME_ZONE_MAPPINGS.put("CET", "+01:00");
        TIME_ZONE_MAPPINGS.put("CHADT", "+13:45");
        TIME_ZONE_MAPPINGS.put("CHAST", "+12:45");
        TIME_ZONE_MAPPINGS.put("CHOST", "+09:00");
        TIME_ZONE_MAPPINGS.put("CHOT", "+08:00");
        TIME_ZONE_MAPPINGS.put("CHUT", "+10:00");
        TIME_ZONE_MAPPINGS.put("CIDST", "-04:00");
        TIME_ZONE_MAPPINGS.put("CIST", "-05:00");
        TIME_ZONE_MAPPINGS.put("CKT", "-10:00");
        TIME_ZONE_MAPPINGS.put("CLST", "-03:00");
        TIME_ZONE_MAPPINGS.put("CLT", "-04:00");
        TIME_ZONE_MAPPINGS.put("COT", "-05:00");
        // Central Standard Time
        TIME_ZONE_MAPPINGS.put("CST", "-06:00");
        // China Standard Time
        //TIME_ZONE_MAPPINGS.put("CST", "+08:00");
        // Cuba Standard Time
        //TIME_ZONE_MAPPINGS.put("CST", "-05:00");
        TIME_ZONE_MAPPINGS.put("CT", "America/Chicago");
        TIME_ZONE_MAPPINGS.put("CVT", "-01:00");
        TIME_ZONE_MAPPINGS.put("CXT", "+07:00");
        TIME_ZONE_MAPPINGS.put("ChST", "+10:00");
        TIME_ZONE_MAPPINGS.put("D", "+04:00");
        TIME_ZONE_MAPPINGS.put("DAVT", "+07:00");
        TIME_ZONE_MAPPINGS.put("DDUT", "+10:00");
        TIME_ZONE_MAPPINGS.put("E", "+05:00");
        TIME_ZONE_MAPPINGS.put("EASST", "-05:00");
        TIME_ZONE_MAPPINGS.put("EAST", "-06:00");
        TIME_ZONE_MAPPINGS.put("EAT", "+03:00");
        TIME_ZONE_MAPPINGS.put("ECT", "-05:00");
        TIME_ZONE_MAPPINGS.put("EDT", "-04:00");
        TIME_ZONE_MAPPINGS.put("EEST", "+03:00");
        TIME_ZONE_MAPPINGS.put("EET", "+02:00");
        TIME_ZONE_MAPPINGS.put("EGST", "+00:00");
        TIME_ZONE_MAPPINGS.put("EGT", "-01:00");
        TIME_ZONE_MAPPINGS.put("EST", "-05:00");
        TIME_ZONE_MAPPINGS.put("ET", "America/New_York");
        TIME_ZONE_MAPPINGS.put("F", "+06:00");
        TIME_ZONE_MAPPINGS.put("FET", "+03:00");
        TIME_ZONE_MAPPINGS.put("FJST", "+13:00");
        TIME_ZONE_MAPPINGS.put("FJT", "+12:00");
        TIME_ZONE_MAPPINGS.put("FKST", "-03:00");
        TIME_ZONE_MAPPINGS.put("FKT", "-04:00");
        TIME_ZONE_MAPPINGS.put("FNT", "-02:00");
        TIME_ZONE_MAPPINGS.put("G", "+07:00");
        TIME_ZONE_MAPPINGS.put("GALT", "-06:00");
        TIME_ZONE_MAPPINGS.put("GAMT", "-09:00");
        TIME_ZONE_MAPPINGS.put("GET", "+04:00");
        TIME_ZONE_MAPPINGS.put("GFT", "-03:00");
        TIME_ZONE_MAPPINGS.put("GILT", "+12:00");
        TIME_ZONE_MAPPINGS.put("GMT", "+00:00");
        // Gulf Standard Time
        TIME_ZONE_MAPPINGS.put("GST", "+04:00");
        // South Georgia Time
        //TIME_ZONE_MAPPINGS.put("GST", "-02:00");
        TIME_ZONE_MAPPINGS.put("GYT", "-04:00");
        TIME_ZONE_MAPPINGS.put("H", "+08:00");
        TIME_ZONE_MAPPINGS.put("HADT", "-09:00");
        TIME_ZONE_MAPPINGS.put("HAST", "-10:00");
        TIME_ZONE_MAPPINGS.put("HKT", "+08:00");
        TIME_ZONE_MAPPINGS.put("HOVST", "+08:00");
        TIME_ZONE_MAPPINGS.put("HOVT", "+07:00");
        TIME_ZONE_MAPPINGS.put("I", "+09:00");
        TIME_ZONE_MAPPINGS.put("ICT", "+07:00");
        TIME_ZONE_MAPPINGS.put("IDT", "+03:00");
        TIME_ZONE_MAPPINGS.put("IOT", "+06:00");
        TIME_ZONE_MAPPINGS.put("IRDT", "+04:30");
        TIME_ZONE_MAPPINGS.put("IRKST", "+09:00");
        TIME_ZONE_MAPPINGS.put("IRKT", "+08:00");
        TIME_ZONE_MAPPINGS.put("IRST", "+03:30");
        // India Standard Time
        TIME_ZONE_MAPPINGS.put("IST", "+05:30");
        // Irish Standard Time
        //TIME_ZONE_MAPPINGS.put("IST", "+01:00");
        // Israel Standard Time
        //TIME_ZONE_MAPPINGS.put("IST", "+02:00");
        TIME_ZONE_MAPPINGS.put("JST", "+09:00");
        TIME_ZONE_MAPPINGS.put("K", "+10:00");
        TIME_ZONE_MAPPINGS.put("KGT", "+06:00");
        TIME_ZONE_MAPPINGS.put("KOST", "+11:00");
        TIME_ZONE_MAPPINGS.put("KRAST", "+08:00");
        TIME_ZONE_MAPPINGS.put("KRAT", "+07:00");
        TIME_ZONE_MAPPINGS.put("KST", "+09:00");
        TIME_ZONE_MAPPINGS.put("KUYT", "+04:00");
        TIME_ZONE_MAPPINGS.put("L", "+11:00");
        TIME_ZONE_MAPPINGS.put("LHDT", "+11:00");
        TIME_ZONE_MAPPINGS.put("LHST", "+10:30");
        TIME_ZONE_MAPPINGS.put("LINT", "+14:00");
        TIME_ZONE_MAPPINGS.put("M", "+12:00");
        TIME_ZONE_MAPPINGS.put("MAGST", "+12:00");
        TIME_ZONE_MAPPINGS.put("MAGT", "+11:00");
        TIME_ZONE_MAPPINGS.put("MART", "-09:30");
        TIME_ZONE_MAPPINGS.put("MAWT", "+05:00");
        TIME_ZONE_MAPPINGS.put("MDT", "-06:00");
        TIME_ZONE_MAPPINGS.put("MHT", "+12:00");
        TIME_ZONE_MAPPINGS.put("MMT", "+06:30");
        TIME_ZONE_MAPPINGS.put("MSD", "+04:00");
        TIME_ZONE_MAPPINGS.put("MSK", "+03:00");
        TIME_ZONE_MAPPINGS.put("MST", "-07:00");
        TIME_ZONE_MAPPINGS.put("MT", "America/Denver");
        TIME_ZONE_MAPPINGS.put("MUT", "+04:00");
        TIME_ZONE_MAPPINGS.put("MVT", "+05:00");
        TIME_ZONE_MAPPINGS.put("MYT", "+08:00");
        TIME_ZONE_MAPPINGS.put("N", "-01:00");
        TIME_ZONE_MAPPINGS.put("NCT", "+11:00");
        TIME_ZONE_MAPPINGS.put("NDT", "-02:30");
        TIME_ZONE_MAPPINGS.put("NFT", "+11:00");
        TIME_ZONE_MAPPINGS.put("NOVST", "+07:00");
        TIME_ZONE_MAPPINGS.put("NOVT", "+07:00");
        TIME_ZONE_MAPPINGS.put("NPT", "+05:45");
        TIME_ZONE_MAPPINGS.put("NRT", "+12:00");
        TIME_ZONE_MAPPINGS.put("NST", "-03:30");
        TIME_ZONE_MAPPINGS.put("NUT", "-11:00");
        TIME_ZONE_MAPPINGS.put("NZDT", "+13:00");
        TIME_ZONE_MAPPINGS.put("NZST", "+12:00");
        TIME_ZONE_MAPPINGS.put("O", "-02:00");
        TIME_ZONE_MAPPINGS.put("OMSST", "+07:00");
        TIME_ZONE_MAPPINGS.put("OMST", "+06:00");
        TIME_ZONE_MAPPINGS.put("ORAT", "+05:00");
        TIME_ZONE_MAPPINGS.put("P", "-03:00");
        TIME_ZONE_MAPPINGS.put("PDT", "-07:00");
        TIME_ZONE_MAPPINGS.put("PET", "-05:00");
        TIME_ZONE_MAPPINGS.put("PETST", "+12:00");
        TIME_ZONE_MAPPINGS.put("PETT", "+12:00");
        TIME_ZONE_MAPPINGS.put("PGT", "+10:00");
        TIME_ZONE_MAPPINGS.put("PHOT", "+13:00");
        TIME_ZONE_MAPPINGS.put("PHT", "+08:00");
        TIME_ZONE_MAPPINGS.put("PKT", "+05:00");
        TIME_ZONE_MAPPINGS.put("PMDT", "-02:00");
        TIME_ZONE_MAPPINGS.put("PMST", "-03:00");
        TIME_ZONE_MAPPINGS.put("PONT", "+11:00");
        // Pacific Standard Time -or- Pitcairn Standard Time
        TIME_ZONE_MAPPINGS.put("PST", "-08:00");
        TIME_ZONE_MAPPINGS.put("PT", "America/Los_Angeles");
        TIME_ZONE_MAPPINGS.put("PWT", "+09:00");
        TIME_ZONE_MAPPINGS.put("PYST", "-03:00");
        // Paraguay Time
        TIME_ZONE_MAPPINGS.put("PYT", "-04:00");
        // Pyongyang Time
        //TIME_ZONE_MAPPINGS.put("PYT", "+08:30");
        TIME_ZONE_MAPPINGS.put("Q", "-04:00");
        TIME_ZONE_MAPPINGS.put("QYZT", "+06:00");
        TIME_ZONE_MAPPINGS.put("R", "-05:00");
        TIME_ZONE_MAPPINGS.put("RET", "+04:00");
        TIME_ZONE_MAPPINGS.put("ROTT", "-03:00");
        TIME_ZONE_MAPPINGS.put("S", "-06:00");
        TIME_ZONE_MAPPINGS.put("SAKT", "+11:00");
        TIME_ZONE_MAPPINGS.put("SAMT", "+04:00");
        TIME_ZONE_MAPPINGS.put("SAST", "+02:00");
        TIME_ZONE_MAPPINGS.put("SBT", "+11:00");
        TIME_ZONE_MAPPINGS.put("SCT", "+04:00");
        TIME_ZONE_MAPPINGS.put("SGT", "+08:00");
        TIME_ZONE_MAPPINGS.put("SRET", "+11:00");
        TIME_ZONE_MAPPINGS.put("SRT", "-03:00");
        TIME_ZONE_MAPPINGS.put("SST", "-11:00");
        TIME_ZONE_MAPPINGS.put("SYOT", "+03:00");
        TIME_ZONE_MAPPINGS.put("T", "-07:00");
        TIME_ZONE_MAPPINGS.put("TAHT", "-10:00");
        TIME_ZONE_MAPPINGS.put("TFT", "+05:00");
        TIME_ZONE_MAPPINGS.put("TJT", "+05:00");
        TIME_ZONE_MAPPINGS.put("TKT", "+13:00");
        TIME_ZONE_MAPPINGS.put("TLT", "+09:00");
        TIME_ZONE_MAPPINGS.put("TMT", "+05:00");
        TIME_ZONE_MAPPINGS.put("TOST", "+14:00");
        TIME_ZONE_MAPPINGS.put("TOT", "+13:00");
        TIME_ZONE_MAPPINGS.put("TRT", "+03:00");
        TIME_ZONE_MAPPINGS.put("TVT", "+12:00");
        TIME_ZONE_MAPPINGS.put("U", "-08:00");
        TIME_ZONE_MAPPINGS.put("ULAST", "+09:00");
        TIME_ZONE_MAPPINGS.put("ULAT", "+08:00");
        TIME_ZONE_MAPPINGS.put("UYST", "-02:00");
        TIME_ZONE_MAPPINGS.put("UYT", "-03:00");
        TIME_ZONE_MAPPINGS.put("UZT", "+05:00");
        TIME_ZONE_MAPPINGS.put("V", "-09:00");
        TIME_ZONE_MAPPINGS.put("VET", "-04:00");
        TIME_ZONE_MAPPINGS.put("VLAST", "+11:00");
        TIME_ZONE_MAPPINGS.put("VLAT", "+10:00");
        TIME_ZONE_MAPPINGS.put("VOST", "+06:00");
        TIME_ZONE_MAPPINGS.put("VUT", "+11:00");
        TIME_ZONE_MAPPINGS.put("W", "-10:00");
        TIME_ZONE_MAPPINGS.put("WAKT", "+12:00");
        TIME_ZONE_MAPPINGS.put("WARST", "-03:00");
        TIME_ZONE_MAPPINGS.put("WAST", "+02:00");
        TIME_ZONE_MAPPINGS.put("WAT", "+01:00");
        TIME_ZONE_MAPPINGS.put("WEST", "+01:00");
        TIME_ZONE_MAPPINGS.put("WET", "+00:00");
        TIME_ZONE_MAPPINGS.put("WFT", "+12:00");
        TIME_ZONE_MAPPINGS.put("WGST", "-02:00");
        TIME_ZONE_MAPPINGS.put("WGT", "-03:00");
        TIME_ZONE_MAPPINGS.put("WIB", "+07:00");
        TIME_ZONE_MAPPINGS.put("WIT", "+09:00");
        TIME_ZONE_MAPPINGS.put("WITA", "+08:00");
        // West Samoa Time
        //TIME_ZONE_MAPPINGS.put("WST", "+14:00");
        // Western Sahara Summer Time
        TIME_ZONE_MAPPINGS.put("WST", "+01:00");
        TIME_ZONE_MAPPINGS.put("WT", "+00:00");
        TIME_ZONE_MAPPINGS.put("X", "-11:00");
        TIME_ZONE_MAPPINGS.put("Y", "-12:00");
        TIME_ZONE_MAPPINGS.put("YAKST", "+10:00");
        TIME_ZONE_MAPPINGS.put("YAKT", "+09:00");
        TIME_ZONE_MAPPINGS.put("YAPT", "+10:00");
        TIME_ZONE_MAPPINGS.put("YEKST", "+06:00");
        TIME_ZONE_MAPPINGS.put("YEKT", "+05:00");
        // Included in Java by default
        // TIME_ZONE_MAPPINGS.put("Z", "+00:00");
    }

    /**
     * Convert a time zone String into a ZoneId. This will work with
     * all standard ZoneId types, plus a mostly-exhaustive list of
     * 3-letter offset abbreviations.
     * 
     * @param timezone
     * @return
     */
    public static ZoneId parseZoneId(String value) {
        return ZoneId.of(value, TIME_ZONE_MAPPINGS);
    }

    private Integer m_year;
    private Integer m_month;
    private Integer m_dayOfMonth;
    private Integer m_hourOfDay;
    private Integer m_minute;
    private Integer m_second;
    private Integer m_nanosecond;
    private ZoneId m_zoneId;

    public Integer getYear() {
        return m_year;
    }

    public void setYear(Integer year) {
        m_year = year;
    }

    public Integer getMonth() {
        return m_month;
    }

    public void setMonth(Integer month) {
        m_month = month;
    }

    public Integer getDayOfMonth() {
        return m_dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        m_dayOfMonth = dayOfMonth;
    }

    public Integer getHourOfDay() {
        return m_hourOfDay;
    }

    public void setHourOfDay(Integer hourOfDay) {
        m_hourOfDay = hourOfDay;
    }

    public Integer getMinute() {
        return m_minute;
    }

    public void setMinute(Integer minute) {
        m_minute = minute;
    }

    public Integer getSecond() {
        return m_second;
    }

    public void setSecond(Integer second) {
        m_second = second;
    }

    public Integer getNanosecond() {
        return m_nanosecond;
    }

    public void setNanosecond(Integer nanosecond) {
        m_nanosecond = nanosecond;
    }

    public ZoneId getZoneId() {
        return m_zoneId;
    }

    public void setZoneId(ZoneId timeZone) {
        m_zoneId = timeZone;
    }

    /**
     * Build the {@link ZonedDateTime} instance. The following fields
     * are required to generate a datestamp:
     * 
     * <ul>
     * <li>month</li>
     * <li>dayOfMonth</li>
     * </ul>
     * 
     * <p>If missing:</p>
     * 
     * <ul>
     * <li>year will be assumed to be within the last 12 months
     * or slightly in the future (if the current month is December)</li>
     * <li>hourOfDay, minute, second and nanosecond will be assumed to be zero</li>
     * <li>time zone will be assumed to be the system time zone
     * ({@link ZoneId#systemDefault()})</li>
     * </ul>
     * 
     * @return
     */
    public ZonedDateTime build() {
        if (m_month != null && m_dayOfMonth != null) {
            if (m_hourOfDay != null) {
                if (m_minute != null) {
                    if (m_second != null) {
                        if (m_nanosecond != null) {
                            return ZonedDateTime.of(getBestYear(), m_month, m_dayOfMonth, m_hourOfDay == null ? 0 : m_hourOfDay, m_minute, m_second, m_nanosecond, getBestZoneId());
                        } else {
                            return ZonedDateTime.of(getBestYear(), m_month, m_dayOfMonth, m_hourOfDay, m_minute, m_second, 0, getBestZoneId());
                        }
                    } else {
                        return ZonedDateTime.of(getBestYear(), m_month, m_dayOfMonth, m_hourOfDay, m_minute, 0, 0, getBestZoneId());
                    }
                } else {
                    return ZonedDateTime.of(getBestYear(), m_month, m_dayOfMonth, m_hourOfDay, 0, 0, 0, getBestZoneId());
                }
            } else {
                return ZonedDateTime.of(getBestYear(), m_month, m_dayOfMonth, 0, 0, 0, 0, getBestZoneId());
            }
        } else {
            throw new DateTimeException("Insufficient fields to produce a ZonedDateTime: month and dayOfMonth are required");
        }
    }

    /**
     * <p>If some fields have not been set,
     * intelligently set them so that we generate a observed 
     * datestamp that is less than or slightly greater than 
     * {@link System#currentTimeMillis()} (due to clock skew). 
     * For instance, around midnight on Dec 31, 2017, we do not 
     * want to generate datestamps of Dec 31, 2018 at the instant 
     * that {@link LocalDateTime#now()} starts returning a 
     * January 1, 2018 datestamp.</p>
     * 
     * @return
     */
    protected int getBestYear() {
        if (m_year == null) {
            return getBestYearForMonth(m_month);
        } else {
            return m_year;
        }
    }

    public static int getBestYearForMonth(Integer month) {
        final LocalDateTime now = LocalDateTime.now();
        if (month == null) {
            return now.getYear();
        } else {
            if (month > now.getMonth().getValue()) {
                // If the month is larger than the current month,
                // than assume that it was during the previous year
                return now.getYear() - 1;
            } else if (month.intValue() == Month.JANUARY.getValue() && now.getMonth() == Month.DECEMBER) {
                // If the current month is December and the builder's
                // month is January, than assume that it's a datestamp
                // from slightly in the future and into the next year
                final int yearValue = now.getYear() + 1;
                LOG.warn("Received datestamp that is in January but our clock still says December; assigning future year {} to the datestamp", yearValue);
                return yearValue;
            } else {
                return now.getYear();
            }
        }
    }

    /**
     * Return the specified ZoneId or {@link ZoneId#systemDefault()}
     * if none has been specified.
     * 
     * @return
     */
    protected ZoneId getBestZoneId() {
        return m_zoneId == null ? ZoneId.systemDefault() : m_zoneId;
    }
}
