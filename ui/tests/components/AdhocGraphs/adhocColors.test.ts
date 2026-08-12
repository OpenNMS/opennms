import { describe, expect, it } from 'vitest'

import {
  ADHOC_PALETTE_DARK,
  ADHOC_PALETTE_LIGHT,
  ADHOC_PALETTE_SIZE,
  restepColorForTheme,
  seriesColor,
  strokeWidthFor
} from '@/components/AdhocGraphs/utils/adhocColors'
import { DARK_THEME, LIGHT_THEME } from '@/services/themeService'

describe('seriesColor', () => {
  it('walks the palette in slot order', () => {
    expect(seriesColor(0, LIGHT_THEME)).toBe(ADHOC_PALETTE_LIGHT[0])
    expect(seriesColor(3, LIGHT_THEME)).toBe(ADHOC_PALETTE_LIGHT[3])
  })

  it('uses the dark steps in dark mode', () => {
    expect(seriesColor(0, DARK_THEME)).toBe(ADHOC_PALETTE_DARK[0])
  })

  it('repeats the hues rather than inventing new ones past the last slot', () => {
    expect(seriesColor(ADHOC_PALETTE_LIGHT.length, LIGHT_THEME)).toBe(ADHOC_PALETTE_LIGHT[0])
  })
})

describe('the palette itself', () => {
  it('carries twelve slots in both modes', () => {
    expect(ADHOC_PALETTE_LIGHT).toHaveLength(12)
    expect(ADHOC_PALETTE_DARK).toHaveLength(12)
    expect(ADHOC_PALETTE_SIZE).toBe(12)
  })

  // Slots 1-8 are the validated base set; the four added for ad-hoc graphs must
  // not disturb them, or every existing graph silently changes color.
  it('leaves the original eight slots untouched', () => {
    expect(ADHOC_PALETTE_LIGHT.slice(0, 8)).toEqual([
      '#2a78d6', '#eb6834', '#1baf7a', '#eda100', '#e87ba4', '#008300', '#4a3aa7', '#e34948'
    ])
    expect(ADHOC_PALETTE_DARK.slice(0, 8)).toEqual([
      '#3987e5', '#d95926', '#199e70', '#c98500', '#d55181', '#008300', '#9085e9', '#e66767'
    ])
  })

  it('has no duplicate slots, so no two series share a color by accident', () => {
    expect(new Set(ADHOC_PALETTE_LIGHT).size).toBe(ADHOC_PALETTE_LIGHT.length)
    expect(new Set(ADHOC_PALETTE_DARK).size).toBe(ADHOC_PALETTE_DARK.length)
  })

  it('is all six-digit lowercase hex, as restepColorForTheme assumes', () => {
    for (const color of [...ADHOC_PALETTE_LIGHT, ...ADHOC_PALETTE_DARK]) {
      expect(color).toMatch(/^#[0-9a-f]{6}$/)
    }
  })
})

// Weight follows the RRDtool LINE1/2/3 family the styles are named after, doubled
// because a 1px stroke disappears on a 2x display.
describe('strokeWidthFor', () => {
  it('steps the line styles 2 / 4 / 6', () => {
    expect(strokeWidthFor('line')).toBe(2)
    expect(strokeWidthFor('line2')).toBe(4)
    expect(strokeWidthFor('line3')).toBe(6)
  })

  it('outlines a filled series at the base weight', () => {
    expect(strokeWidthFor('area')).toBe(2)
    expect(strokeWidthFor('stack')).toBe(2)
  })

  // Nothing derives weight from the series index any more; only the chosen style.
  it('depends on the style alone', () => {
    expect(strokeWidthFor('line')).toBe(strokeWidthFor('line'))
    expect(new Set(['line', 'line2', 'line3'].map(s => strokeWidthFor(s as never))).size).toBe(3)
  })
})

describe('restepColorForTheme', () => {
  it('moves a palette color to the other mode\'s step', () => {
    expect(restepColorForTheme(ADHOC_PALETTE_LIGHT[1], DARK_THEME)).toBe(ADHOC_PALETTE_DARK[1])
    expect(restepColorForTheme(ADHOC_PALETTE_DARK[1], LIGHT_THEME)).toBe(ADHOC_PALETTE_LIGHT[1])
  })

  it('leaves a hand-picked color alone', () => {
    expect(restepColorForTheme('#123456', DARK_THEME)).toBe('#123456')
  })
})
