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
