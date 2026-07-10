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

/**
 * OpenNMS PrimeVue preset (based on Aura).
 *
 * Goal: PrimeVue components match the existing FeatherDS look in BOTH light and
 * dark mode out-of-the-box, so individual components do not need per-component
 * `:deep(.p-*)` overrides.
 *
 * The surface / content / text / form-field / overlay token values below are the
 * literal FeatherDS theme colors (from @featherds/styles/themes/open-light.css and
 * open-dark.css). They are intentionally NOT expressed as `var(--feather-*)`:
 * PrimeVue declares its `--p-*` variables on `:root` (html), whereas FeatherDS
 * declares `--feather-*` on the `body.open-light` / `body.open-dark` class. A
 * `var(--feather-*)` referenced from a `:root` declaration cannot resolve (the
 * variable is defined on a descendant), so the token would compute to empty.
 * Literal values avoid that and let PrimeVue's `darkModeSelector` switch schemes.
 *
 * When FeatherDS is removed (migration Phase 6) these values become the canonical
 * OpenNMS palette — no further change required here.
 */

// FeatherDS primary brand color, referenced for form-field focus rings etc.
// (PrimeVue token refs like `{primary.color}` resolve within PrimeVue's own
// :root variables, so they are safe to use here.)
const PRIMARY_FOCUS = '{primary.color}'

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
    // Semantic status colors. Components must reference these (var(--p-success-color)
    // etc.) for anything with status meaning, rather than the raw palette primitives
    // (--p-green-500 …), so a theme can remap status colors in one place. Names mirror
    // PrimeVue's Toast/Message severities (success / info / warn / error) so the same
    // vocabulary applies to component CSS and to severity props. These replace the old
    // FeatherDS $success / $error / $warning vars.
    success: { color: '{green.500}' },
    info: { color: '{blue.500}' },
    warn: { color: '{yellow.500}' },
    error: { color: '{red.500}' },
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
        },
        text: {
          color: 'rgba(10, 12, 27, 0.9)',
          hoverColor: 'rgba(10, 12, 27, 0.9)',
          mutedColor: 'rgba(10, 12, 27, 0.7)',
          hoverMutedColor: 'rgba(10, 12, 27, 0.7)'
        },
        content: {
          background: '#ffffff',
          hoverBackground: '#f4f7fc',
          borderColor: 'rgba(10, 12, 27, 0.12)',
          color: 'rgba(10, 12, 27, 0.9)',
          hoverColor: 'rgba(10, 12, 27, 0.9)'
        },
        formField: {
          background: '#ffffff',
          borderColor: 'rgba(10, 12, 27, 0.12)',
          hoverBorderColor: 'rgba(10, 12, 27, 0.12)',
          focusBorderColor: PRIMARY_FOCUS,
          color: 'rgba(10, 12, 27, 0.9)',
          placeholderColor: 'rgba(10, 12, 27, 0.7)',
          iconColor: 'rgba(10, 12, 27, 0.7)',
          disabledBackground: '#f4f7fc',
          disabledColor: 'rgba(10, 12, 27, 0.4)'
        },
        overlay: {
          select: { background: '#ffffff', borderColor: 'rgba(10, 12, 27, 0.12)', color: 'rgba(10, 12, 27, 0.9)' },
          popover: { background: '#ffffff', borderColor: 'rgba(10, 12, 27, 0.12)', color: 'rgba(10, 12, 27, 0.9)' },
          modal: { background: '#ffffff', borderColor: 'rgba(10, 12, 27, 0.12)', color: 'rgba(10, 12, 27, 0.9)' }
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
        text: {
          color: 'rgb(255, 255, 255)',
          hoverColor: 'rgb(255, 255, 255)',
          mutedColor: 'rgba(255, 255, 255, 0.78)',
          hoverMutedColor: 'rgba(255, 255, 255, 0.78)'
        },
        content: {
          background: '#15182B',
          hoverBackground: 'rgba(255, 255, 255, 0.06)',
          borderColor: 'rgba(255, 255, 255, 0.24)',
          color: 'rgb(255, 255, 255)',
          hoverColor: 'rgb(255, 255, 255)'
        },
        formField: {
          background: '#15182B',
          borderColor: 'rgba(255, 255, 255, 0.24)',
          hoverBorderColor: 'rgba(255, 255, 255, 0.38)',
          focusBorderColor: PRIMARY_FOCUS,
          color: 'rgb(255, 255, 255)',
          placeholderColor: 'rgba(255, 255, 255, 0.5)',
          iconColor: 'rgba(255, 255, 255, 0.5)',
          disabledBackground: '#0a0c1b',
          disabledColor: 'rgba(255, 255, 255, 0.5)'
        },
        overlay: {
          select: { background: '#15182B', borderColor: 'rgba(255, 255, 255, 0.24)', color: 'rgb(255, 255, 255)' },
          popover: { background: '#15182B', borderColor: 'rgba(255, 255, 255, 0.24)', color: 'rgb(255, 255, 255)' },
          modal: { background: '#15182B', borderColor: 'rgba(255, 255, 255, 0.24)', color: 'rgb(255, 255, 255)' }
        },
        // Dropdown / autocomplete option hover + selected backgrounds. Aura's dark
        // defaults derive from the neutral surface scale (off the OpenNMS navy
        // palette); use a subtle white overlay like the FeatherDS dropdowns. Light
        // mode keeps Aura's defaults, which already match.
        list: {
          option: {
            focusBackground: 'rgba(255, 255, 255, 0.06)',
            selectedBackground: 'rgba(255, 255, 255, 0.06)',
            selectedFocusBackground: 'rgba(255, 255, 255, 0.1)'
          }
        }
      }
    }
  },
  components: {
    // IftaLabel (in-field top-aligned label). Aura's label font is 0.75rem which
    // reads too small next to the input value; bump to 1rem and add a little more
    // input top padding so the value clears the larger label.
    iftalabel: {
      root: { fontSize: '0.9rem' },
      input: { paddingTop: '1.75rem' }
    },
    // DataTable rows/body inherit the bridged `content.*` tokens. Headers in the
    // FeatherDS look use the (muted) background + secondary text, and the border
    // color is set explicitly because Aura's dark scheme hardcodes it to a
    // neutral surface shade.
    datatable: {
      colorScheme: {
        light: {
          root: { borderColor: 'rgba(10, 12, 27, 0.12)' },
          headerCell: { background: '#f4f7fc', color: 'rgba(10, 12, 27, 0.7)' }
        },
        dark: {
          root: { borderColor: 'rgba(255, 255, 255, 0.24)' },
          headerCell: { background: '#0a0c1b', color: 'rgba(255, 255, 255, 0.78)' }
        }
      }
    },
    // Chip label/icon default to a surface-scale color; align with body text.
    chip: {
      colorScheme: {
        light: {
          root: { color: 'rgba(10, 12, 27, 0.9)' },
          icon: { color: 'rgba(10, 12, 27, 0.9)' },
          removeIcon: { color: 'rgba(10, 12, 27, 0.9)' }
        },
        dark: {
          root: { color: 'rgb(255, 255, 255)' },
          icon: { color: 'rgb(255, 255, 255)' },
          removeIcon: { color: 'rgb(255, 255, 255)' }
        }
      }
    },
    // Toast (used by the Snackbar wrapper). Aura's dark scheme sets each severity's
    // detail text to {surface.0}, expecting it to be a light surface — but our
    // preset maps surface.0 to the dark navy (#15182B), which is unreadable on the
    // translucent toast background. Force a light detail color in dark mode.
    toast: {
      colorScheme: {
        dark: {
          info: { detailColor: 'rgba(255, 255, 255, 0.9)' },
          success: { detailColor: 'rgba(255, 255, 255, 0.9)' },
          warn: { detailColor: 'rgba(255, 255, 255, 0.9)' },
          error: { detailColor: 'rgba(255, 255, 255, 0.9)' },
          secondary: { detailColor: 'rgba(255, 255, 255, 0.9)' },
          contrast: { detailColor: 'rgba(255, 255, 255, 0.9)' }
        }
      }
    },
    // Tooltip. Aura's dark scheme sets the tooltip text color to {surface.0},
    // expecting a light surface — but our preset maps surface.0 to the dark navy
    // (#15182B), which is unreadable on the dark-gray tooltip background. Force a
    // light color in dark mode.
    tooltip: {
      colorScheme: {
        dark: { root: { color: 'rgba(255, 255, 255, 0.9)' }}
      }
    },
    // Outlined buttons: Aura draws the border from a faint primary shade
    // (primary.200 in light, primary.700 in dark) which is low-contrast against
    // the surface. Use the button's own (primary) text color so the outline is as
    // distinct as the label. Border width is bumped to 2px in primevue-overrides.scss.
    button: {
      colorScheme: {
        light: { outlined: { primary: { borderColor: '{primary.color}' }}},
        dark: { outlined: { primary: { borderColor: '{primary.color}' }}}
      }
    }
  }
})

export default OpenNMSPreset
