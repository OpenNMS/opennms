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
package org.opennms.netmgt.model.events;

import java.util.Date;


public class EventConfSourceMetadataDto {
    private String filename;
    private int eventCount;
    private int fileOrder;
    private String username;
    private Date now;
    private String vendor;
    private String description;
    private Boolean fileOverride;

    // Private constructor to enforce use of builder
    private EventConfSourceMetadataDto(Builder builder) {
        this.filename = builder.filename;
        this.eventCount = builder.eventCount;
        this.fileOrder = builder.fileOrder;
        this.username = builder.username;
        this.now = builder.now;
        this.vendor = builder.vendor;
        this.description = builder.description;
    }

    // Getters
    public String getFilename() {
        return filename;
    }
    public int getEventCount() {
        return eventCount;
    }
    public int getFileOrder() {
        return fileOrder;
    }
    public String getUsername() {
        return username;
    }
    public Date getNow() {
        return now;
    }
    public String getVendor() {
        return vendor;
    }
    public String getDescription() {
        return description;
    }
    public Boolean getFileOverride(){
        return fileOverride;
    }


    // Builder class
    public static class Builder {
        private String filename;
        private int eventCount;
        private int fileOrder;
        private String username;
        private Date now;
        private String vendor;
        private String description;
        private Boolean fileOverride;

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder eventCount(int eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public Builder fileOrder(int fileOrder) {
            this.fileOrder = fileOrder;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder now(Date now) {
            this.now = now;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder fileOverride(Boolean fileOverride) {
            this.fileOverride = fileOverride;
            return this;
        }

        public EventConfSourceMetadataDto build() {
            return new EventConfSourceMetadataDto(this);
        }
    }
}
