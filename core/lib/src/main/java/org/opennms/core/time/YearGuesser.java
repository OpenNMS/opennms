/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.core.time;

import java.time.Duration;
import java.time.LocalDateTime;

public class YearGuesser {

    /**
     * We try to guess the missing year for the given dateWithoutYear by assigning the year which brings the
     * dateWithoutYear closest (smallest time gap) to the referenceDateTime.
     * Assumptions:
     * - dateTime and referenceDateTime have the same time zone.
     * - dateTime.getYear() == 0 || dateTime.getYear() == 1970, otherwise we won't do anything
     * @return the LocalDateTime with the best guessed year
     */
    public static LocalDateTime guessYearForDate(LocalDateTime dateWithoutYear, LocalDateTime referenceDateTime) {

        // SimpleDateFormat sets year to 1970 if no year was given
        if(dateWithoutYear.getYear() != 0 && dateWithoutYear.getYear() != 1970) {
            // nothing to guess, the year is already set => will return as is.
            return dateWithoutYear;
        }

        // try same year
        LocalDateTime dateGuess = dateWithoutYear.withYear(referenceDateTime.getYear());
        Duration smallestDuration = Duration.between(dateGuess, referenceDateTime);
        int guessedYear = referenceDateTime.getYear();

        // try plus one year
        dateGuess = dateWithoutYear.withYear(referenceDateTime.getYear() + 1);
        Duration timeDifference = Duration.between(dateGuess, referenceDateTime);
        if(timeDifference.abs().toMillis() < smallestDuration.abs().toMillis()){
            guessedYear = referenceDateTime.getYear() + 1;
            smallestDuration = timeDifference;
        }

        // try minus one year
        dateGuess = dateWithoutYear.withYear(referenceDateTime.getYear() -1);
        timeDifference = Duration.between(dateGuess, referenceDateTime);
        if(timeDifference.abs().toMillis() < smallestDuration.abs().toMillis()){
            guessedYear = referenceDateTime.getYear() -1;
        }

        return dateWithoutYear.withYear(guessedYear);
    }

    public static LocalDateTime guessYearForDate(LocalDateTime dateWithoutYear) {
        return guessYearForDate(dateWithoutYear, LocalDateTime.now());
    }
}
