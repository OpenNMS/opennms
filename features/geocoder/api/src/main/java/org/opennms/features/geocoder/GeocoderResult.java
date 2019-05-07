/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.geocoder;

/**
 * The resolution result of the {@link GeocoderService}.
 * This allows to distinguish between no and unsuccessful result.
 *
 * @author mvrueden
 */
public class GeocoderResult {
    private String address;
    private Coordinates coordinates;
    private Throwable throwable;

    private GeocoderResult() {
    }

    public boolean hasError() {
        return throwable != null;
    }

    public boolean isEmpty() {
        return coordinates == null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    private void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    private void setError(String errorMessage) {
        setError(new GeocoderException(errorMessage));
    }

    private void setError(Throwable throwable) {
        this.throwable = throwable;
    }


    public static GeocoderResult.Builder builder() {
        return new Builder();
    }

    public static GeocoderResult.Builder success(String address, double lng, double lat) {
        return new Builder().withAddress(address).withCoordinates(lng, lat);
    }

    public static GeocoderResult.Builder noResult(String address) {
        return new Builder().withAddress(address).noResult();
    }
    public static Builder error(String errorMessage) {
        return new Builder().withError(errorMessage);
    }

    public static Builder error(Exception exception) {
        return new Builder().withError(exception);
    }

    public static class Builder {
        final GeocoderResult result = new GeocoderResult();

        private Builder() {

        }

        public Builder noResult() {
            if (result.getAddress() != null) {
                result.setError(String.format("No results found for address '%s'", result.getAddress()));
            } else {
                result.setError("No results found for address");
            }
            return this;
        }

        public Builder withAddress(String address) {
            result.setAddress(address);
            return this;
        }

        public Builder withCoordinates(double lng, double lat) {
            result.setCoordinates(new Coordinates(lng, lat));
            return this;
        }

        public Builder withError(String errorMessage) {
            result.setError(errorMessage);
            return this;
        }

        public Builder withError(Throwable t) {
            result.setError(t);
            return this;
        }

        public GeocoderResult build() {
            return result;
        }

    }
}
