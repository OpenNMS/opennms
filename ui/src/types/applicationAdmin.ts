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

// Wire shapes of the /api/v2/applications admin operations (summaries, members,
// service-candidates). Applications are identified by their database id.

export interface ApplicationSummary {
  id: number
  name: string
  serviceCount: number
  perspectiveLocations: string[]
}

export interface ApplicationService {
  id: number
  nodeId: number | null
  nodeLabel: string | null
  ipInterfaceId: number | null
  ipAddress: string | null
  serviceName: string | null
}

export interface ApplicationMembers {
  id: number
  name: string
  services: ApplicationService[]
  perspectiveLocations: string[]
}

// a list left out keeps the current members of that kind
export interface ApplicationMembersUpdate {
  serviceIds?: number[]
  perspectiveLocations?: string[]
}

export interface ServiceCandidatePage {
  totalCount: number
  services: ApplicationService[]
}
