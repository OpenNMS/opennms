/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

public enum ErrorStatus {
    NO_ERROR {
        @Override public boolean isFatal() {
            return false;
        }
        @Override public boolean retry() {
            return false;
        }
    },
    TOO_BIG {
        @Override public boolean isFatal() {
            return false;
        }
        @Override public boolean retry() {
            return true;
        }
    },
    NO_SUCH_NAME {
        @Override public boolean isFatal() {
            return false;
        }
        @Override public boolean retry() {
            return true;
        }
    },
    BAD_VALUE {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    READ_ONLY {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    GEN_ERR {
        @Override public boolean isFatal() {
            return false;
        }
        @Override public boolean retry() {
            return true;
        }
    },
    NO_ACCESS {
        @Override public boolean isFatal() {
            return false;
        }
        @Override public boolean retry() {
            return true;
        }
    },
    WRONG_TYPE {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    WRONG_LENGTH {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    WRONG_ENCODING {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    WRONG_VALUE {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    NO_CREATION {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    INCONSISTENT_VALUE {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    RESOURCE_UNAVAILABLE {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    COMMIT_FAILED {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    UNDO_FAILED {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    AUTHORIZATION_ERROR {
        @Override public boolean isFatal() {
            return false;
        }
        @Override public boolean retry() {
            return true;
        }
    },
    NOT_WRITABLE {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    },
    INCONSISTENT_NAME {
        @Override public boolean isFatal() {
            return false; // should be true (OpenNMS doesn't do sets), but fall through for bad agents
        }
        @Override public boolean retry() {
            return false;
        }
    };
    
    /**
     * Whether or not this error status should be fatal (ie, throw an exception).
     */
    public abstract boolean isFatal();
    
    /**
     * Whether or not a retry should be attempted upon receiving this error code.
     */
    public abstract boolean retry();

    public static ErrorStatus fromStatus(final int status) {
        return ErrorStatus.values()[status];
    }
}
