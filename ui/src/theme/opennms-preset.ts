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

import { definePreset } from '@primevue/themes'
import Aura from '@primevue/themes/aura'

const OpenNMSPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#ecedf5',
      100: '#c5c8e0',
      200: '#9ea3cb',
      300: '#7780b6',
      400: '#505da1',
      500: '#273180',
      600: '#202867',
      700: '#191f4e',
      800: '#131736',
      900: '#0a0c1b'
    },
    colorScheme: {
      light: {
        primary: {
          color: '#273180',
          contrastColor: '#ffffff',
          hoverColor: '#202867',
          activeColor: '#191f4e'
        },
        surface: {
          0: '#ffffff',
          100: '#f4f7fc',
          200: '#e8edf5',
          300: '#d1d5e0'
        }
      },
      dark: {
        primary: {
          color: '#00BFCB',
          contrastColor: '#ffffff',
          hoverColor: '#00a8b3',
          activeColor: '#009199'
        },
        surface: {
          0: '#15182B'
        }
      }
    }
  }
})

export default OpenNMSPreset
