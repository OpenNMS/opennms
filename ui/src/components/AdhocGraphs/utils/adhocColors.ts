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

import { AdhocSeriesStyle } from '@/types/adhocGraph'
import { DARK_THEME, Theme } from '@/services/themeService'

/**
 * Categorical series palette, in fixed slot order. The two columns are the same
 * eight hues stepped for their surface — not two unrelated palettes — and the
 * ORDER is the colorblind-safety mechanism: adjacent slots are the pairs most
 * likely to be compared on a line chart, and this ordering is the one that clears
 * the adjacent-pair separation gates in both modes. Do not re-order or extend it.
 *
 * Slots 1-8 are the validated base set. Slots 9-12 were added so that an ad-hoc
 * graph — which routinely carries more than eight series — gets twelve distinct
 * hues instead of a second, non-colour encoding channel. They were chosen by
 * search against the same validator and each sits at least ΔE 14.6 (normal
 * vision) from every earlier slot, so the gate numbers are unchanged from the
 * base eight: worst adjacent CVD ΔE 9.1 light / 8.4 dark, worst adjacent
 * normal-vision ΔE 19.6 light / 19.3 dark.
 *
 * Twelve is the ceiling, not an arbitrary stopping point: a fourteen-slot set
 * drops the worst adjacent CVD pair into the 6-8 band, which is only legal WITH a
 * secondary encoding — the very thing this palette exists to avoid.
 *
 * Three light slots sit below 3:1 against the light surface; the relief for that
 * is already built into the page — every series is named in the legend with a
 * colour swatch, and the Data tab shows the same numbers as a table.
 */
export const ADHOC_PALETTE_LIGHT: readonly string[] = [
  '#2a78d6', // blue
  '#eb6834', // orange
  '#1baf7a', // aqua
  '#eda100', // yellow
  '#e87ba4', // pink
  '#008300', // green
  '#4a3aa7', // indigo
  '#e34948', // red
  '#bd00cd', // magenta
  '#905c00', // brown
  '#7900fc', // violet
  '#b02c74' //  plum
]

export const ADHOC_PALETTE_DARK: readonly string[] = [
  '#3987e5',
  '#d95926',
  '#199e70',
  '#c98500',
  '#d55181',
  '#008300',
  '#9085e9',
  '#e66767',
  '#d800ea',
  '#a76c00',
  '#8547ff',
  '#c44085'
]

/**
 * Stroke weight per style, mirroring RRDtool's LINE1/LINE2/LINE3.
 *
 * Doubled from RRDtool's nominal 1/2/3 pixels: RRDtool renders to a bitmap at a
 * fixed size, whereas this canvas is laid out in CSS pixels on a display that is
 * usually 2x, where a 1px stroke all but disappears against the grid.
 *
 * Weight varies ONLY because the user asked for it on a specific series. It is
 * never applied automatically to tell two series apart — a thin or heavy line
 * reads as a different KIND of series (forecast, threshold, projection) rather
 * than simply another one. Past the twelfth series the hues repeat and the
 * rendering stays put; identity then comes from the legend, the hover readout and
 * the Data tab, and any series can be recoloured by hand.
 */
export const SERIES_STROKE_WIDTHS: Readonly<Record<AdhocSeriesStyle, number>> = {
  line: 2,
  line2: 4,
  line3: 6,
  area: 2,
  stack: 2
}

/** The stroke weight for a style; the outline is drawn even on a filled series. */
export const strokeWidthFor = (style: AdhocSeriesStyle): number => SERIES_STROKE_WIDTHS[style] ?? 2

const paletteFor = (theme: Theme): readonly string[] =>
  (theme === DARK_THEME ? ADHOC_PALETTE_DARK : ADHOC_PALETTE_LIGHT)

/** The default colour for the series at `index`, in the given theme. */
export const seriesColor = (index: number, theme: Theme): string => {
  const palette = paletteFor(theme)
  return palette[Math.abs(index) % palette.length]
}

/** How many series can be told apart by colour alone. */
export const ADHOC_PALETTE_SIZE = ADHOC_PALETTE_LIGHT.length

/**
 * Re-step a colour for the target theme.
 *
 * Colours are persisted as hex (so a colour the user picked by hand survives a
 * reload or a shared link), which means a config built in light mode carries the
 * light steps. Any colour still sitting on a palette slot is moved to that slot's
 * step for `theme`; anything else is a deliberate override and is left alone.
 */
export const restepColorForTheme = (color: string, theme: Theme): string => {
  const from = theme === DARK_THEME ? ADHOC_PALETTE_LIGHT : ADHOC_PALETTE_DARK
  const to = paletteFor(theme)
  const slot = from.indexOf(color.toLowerCase())
  return slot >= 0 ? to[slot] : color
}

/**
 * Chart ink (axes, grid, text) pulled from the live PrimeVue theme variables so the
 * canvas tracks the light/dark toggle. Falls back to readable defaults when the
 * variables are unavailable (happy-dom in unit tests resolves them to '').
 */
export const chartInk = (): { text: string, muted: string, grid: string } => {
  const styles = typeof window !== 'undefined' && document.documentElement ?
    window.getComputedStyle(document.documentElement) :
    null
  const read = (name: string, fallback: string) => styles?.getPropertyValue(name).trim() || fallback

  return {
    text: read('--p-text-color', '#0b0b0b'),
    muted: read('--p-text-muted-color', '#52514e'),
    grid: read('--p-content-border-color', '#d8d7d2')
  }
}
