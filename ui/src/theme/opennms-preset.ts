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
        },
        // Aura dark uses {surface.0} as white for text.color, but we set surface.0
        // to our dark navy. Override text tokens here to keep text legible in dark mode.
        text: {
          color: 'rgba(255, 255, 255, 0.87)',
          hoverColor: 'rgba(255, 255, 255, 0.87)',
          mutedColor: 'rgba(255, 255, 255, 0.6)',
          hoverMutedColor: 'rgba(255, 255, 255, 0.78)'
        },
        // DataTable body rows and other content areas use {content.background}.
        // Aura dark maps that to {surface.900} (zinc.900 = #18181b), a neutral gray
        // that doesn't match the OpenNMS navy palette. Map it to surface.0 instead.
        content: {
          background: '{surface.0}',
          hoverBackground: 'rgba(255, 255, 255, 0.06)',
          borderColor: 'rgba(255, 255, 255, 0.12)',
          color: '{text.color}',
          hoverColor: '{text.hover.color}'
        },
        // Aura dark sets formField.color to {surface.0}, which is our dark navy (#15182B) —
        // unreadable text. Also align the form field background to the OpenNMS navy palette.
        formField: {
          background: '{surface.0}',
          color: '{text.color}',
          borderColor: 'rgba(255, 255, 255, 0.2)',
          hoverBorderColor: 'rgba(255, 255, 255, 0.38)',
          placeholderColor: 'rgba(255, 255, 255, 0.5)',
          iconColor: 'rgba(255, 255, 255, 0.5)',
          disabledBackground: 'rgba(255, 255, 255, 0.05)',
          disabledColor: 'rgba(255, 255, 255, 0.38)'
        },
        // Select/popover overlays are teleported to body; still set their tokens so the
        // CSS variables carry the right dark values everywhere they're inherited.
        overlay: {
          select: {
            background: '{surface.0}',
            borderColor: 'rgba(255, 255, 255, 0.12)',
            color: '{text.color}'
          },
          popover: {
            background: '{surface.0}',
            borderColor: 'rgba(255, 255, 255, 0.12)',
            color: '{text.color}'
          }
        }
      }
    }
  }
})

export default OpenNMSPreset
