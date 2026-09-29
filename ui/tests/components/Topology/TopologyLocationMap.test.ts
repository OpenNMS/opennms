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

import { flushPromises, mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, expect, it, vi } from 'vitest'
import TopologyLocationMap from '@/components/Topology/TopologyLocationMap.vue'
import { useAppStore } from '@/stores/appStore'
import { getGeolocationConfig } from '@/services/geolocationService'

vi.mock('@/services/geolocationService', () => ({
  getGeolocationConfig: vi.fn()
}))

// Leaflet needs real layout, which happy-dom has none of; what is under test is
// which url and theme the map is handed, not its rendering.
vi.mock('@vue-leaflet/vue-leaflet', () => ({
  LMap: { name: 'LMap', template: '<div><slot /></div>' },
  LTileLayer: { name: 'LTileLayer', props: ['url', 'attribution'], template: '<div />' },
  LMarker: { name: 'LMarker', props: ['latLng'], template: '<div />' }
}))
vi.mock('leaflet/dist/leaflet.css', () => ({}))

const mountMap = async (theme?: string) => {
  const wrapper = mount(TopologyLocationMap, {
    props: { lat: 35.7796, lon: -78.6382 },
    global: { plugins: [createTestingPinia({ stubActions: false })] }
  })
  if (theme) {
    useAppStore().theme = theme as never
  }
  await flushPromises()
  return wrapper
}

describe('TopologyLocationMap', () => {
  it('draws the operator-configured tiles', async () => {
    vi.mocked(getGeolocationConfig).mockResolvedValue({
      tileServerUrl: 'https://tiles.opennms.org/{z}/{x}/{y}.png',
      options: { attribution: 'OSM' }
    } as never)

    const wrapper = await mountMap()
    const layer = wrapper.findComponent({ name: 'LTileLayer' })
    expect(layer.props('url')).toBe('https://tiles.opennms.org/{z}/{x}/{y}.png')
    expect(layer.props('attribution')).toBe('OSM')
  })

  // No dark basemap exists to switch to, so the light tiles are inverted in CSS.
  it('inverts the tiles in dark mode only', async () => {
    vi.mocked(getGeolocationConfig).mockResolvedValue({
      tileServerUrl: 'https://tiles.example/{z}/{x}/{y}.png'
    } as never)

    const light = await mountMap('open-light')
    expect(light.find('.tlm').classes()).not.toContain('tlm-dark')

    const dark = await mountMap('open-dark')
    expect(dark.find('.tlm').classes()).toContain('tlm-dark')
  })

  it('says so rather than drawing an empty box when no tile server is set', async () => {
    vi.mocked(getGeolocationConfig).mockResolvedValue(false as never)

    const wrapper = await mountMap()
    expect(wrapper.find('.tlm').exists()).toBe(false)
    expect(wrapper.text()).toContain('No tile server is configured')
  })

  it('treats a config with no url as no tile server', async () => {
    vi.mocked(getGeolocationConfig).mockResolvedValue({ options: {}} as never)

    const wrapper = await mountMap()
    expect(wrapper.text()).toContain('No tile server is configured')
  })
})
