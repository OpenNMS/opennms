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
