///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { rest } from './axiosInstances'

/**
 * Whether a node is in its requisition, as the legacy node page's `existsInRequisition` decides
 * it (NetworkElementFactory.nodeExistsInRequisition): the pending requisition for the foreign
 * source, or the deployed one when there is none pending, lists the foreign id. v1 answers that
 * lookup with 200 or 404; there is no v2 requisitions API.
 *
 * null when the check itself failed, so a caller can tell "not in a requisition" from "could not
 * tell".
 */
const nodeExistsInRequisition = async (foreignSource: string, foreignId: string): Promise<boolean | null> => {
  try {
    await rest.get(`/requisitions/${encodeURIComponent(foreignSource)}/nodes/${encodeURIComponent(foreignId)}`)

    return true
  } catch (err: any) {
    return err?.response?.status === 404 ? false : null
  }
}

export { nodeExistsInRequisition }
