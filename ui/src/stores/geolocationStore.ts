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

import { defineStore } from 'pinia'
import API from '@/services'
import { TileProviderItem } from '@/types/index'

export const useGeolocationStore = defineStore('geolocationStore', () => {
  const userDefinedTileProvider = ref<TileProviderItem>()
  const tileProviders = ref<TileProviderItem[]>([])

  /**
   * Default tile providers for the Geographical Map.
   * These are the default ones, but user can add an additional one
   * in 'opennms/etc/opennms.properties' file, gwt.openlayers.url and gwt.openlayers.options.attribution
   *
   * If added, that one will become the default.
   *
   * Typically, `tiles.opennms.org` will be in the `opennms.properties` file and returned by the Geolocation Rest API.
   */
  const getDefaultTileProviders = (): TileProviderItem[] => {
    return [
      {
        name: 'OpenStreetMap',
        visible: true,
        attribution:
          '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors',
        url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
      },
      {
        name: 'OpenTopoMap',
        visible: false,
        url: 'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png',
        attribution:
          'Map data: &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)'
      }
    ]
  }

  /**
   * Fetch the user defined tile provider via the Geolocation Config Rest API.
   */
  const fetchUserDefinedTileProvider = async () => {
    const resp = await API.getGeolocationConfig()

    if (resp && resp.tileServerUrl) {
      const name = resp.tileServerUrl.includes('tiles.opennms.org') ? 'OpenNMS' : 'User Defined'

      userDefinedTileProvider.value = {
        name,
        url: resp.tileServerUrl,
        attribution: resp.options?.attribution ?? '',
        visible: true
      } as TileProviderItem
    }
  }

  const fetchTileProviders = async () => {
    // get the user-defined tile provider (from opennms.properties via the Geolocation Config Rest API) if it exists
    if (!userDefinedTileProvider.value) {
      await fetchUserDefinedTileProvider()
    }

    if (userDefinedTileProvider.value) {
      // set user defined as first and visible one,
      // set default tile providers as secondary and non-visible
      const defaultProviders = getDefaultTileProviders()
      const defaultProvidersUpdated = defaultProviders.map(p => {
        return {
          ...p,
          visible: false
        } as TileProviderItem
      })

      tileProviders.value = [
        userDefinedTileProvider.value,
        ...defaultProvidersUpdated
      ]
    } else {
      tileProviders.value = getDefaultTileProviders()
    }
  }

  return {
    fetchTileProviders,
    tileProviders
  }
})
