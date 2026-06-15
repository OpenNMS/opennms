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

import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

export const categories = [
  { authorizedGroups: [] as any[], id: 1, name: 'Routers' },
  { authorizedGroups: [] as any[], id: 2, name: 'Switches' },
  { authorizedGroups: [] as any[], id: 3, name: 'Servers' },
  { authorizedGroups: [] as any[], id: 4, name: 'Production' },
  { authorizedGroups: [] as any[], id: 5, name: 'Test' },
  { authorizedGroups: [] as any[], id: 6, name: 'Development' }
]

export const serviceTypes = [
  { id: 1, name: 'HTTP' },
  { id: 8, name: 'HTTPS' },
  { id: 3, name: 'ICMP' }
]

export const monitoringLocations = [
  {
    geolocation: null,
    longitude: -71.05888,
    latitude: 42.36008,
    tags: [
    ],
    priority: 100,
    'location-name': DEFAULT_MONITORING_LOCATION,
    'monitoring-area': 'localhost',
    name: DEFAULT_MONITORING_LOCATION,
    area: 'localhost'
  },
  {
    geolocation: null,
    longitude: 13.41144,
    latitude: 52.52343,
    tags: [
    ],
    priority: 80,
    'location-name': 'Loc0',
    'monitoring-area': 'localhost',
    name: 'Loc0',
    area: 'localhost'
  }
]
