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
package org.opennms.netmgt.surveillance.views;

import java.util.List;

/**
 * Persistence for surveillance view definitions. A small, purpose-built CRUD
 * contract (not the generic {@code OnmsDao}) so it can be backed by the
 * generic JSON key-value store rather than Hibernate.
 */
public interface SurveillanceViewDao {

    /** All views, in no particular order. */
    List<SurveillanceView> findAll();

    /** A view by id, or {@code null} if none exists. */
    SurveillanceView get(String id);

    /** A view by its unique name, or {@code null} if none exists. */
    SurveillanceView findByName(String name);

    /**
     * Persist a new view, assigning and returning its generated id. The view's
     * {@code id} is ignored on input.
     */
    String save(SurveillanceView view);

    /** Persist changes to an existing view (identified by its {@code id}). */
    void update(SurveillanceView view);

    /** Remove a view (identified by its {@code id}). */
    void delete(SurveillanceView view);

    /**
     * The id of the view to fall back to when neither the username nor any of
     * the user's group names matches a view name, or {@code null} if no
     * default is configured.
     */
    String getDefaultViewId();

    /** Set (or, with {@code null}, clear) the default view id. */
    void setDefaultViewId(String viewId);
}
