import { describe, expect, it } from 'vitest'
import { getPanelDefinition, listPanelDefinitions, panelRegistry } from '@/components/Dashboard/registry'
import { createDefaultLayout } from '@/components/Dashboard/defaultLayout'

describe('panel registry', () => {
  it('every definition is complete and self-consistent', () => {
    for (const [type, def] of Object.entries(panelRegistry)) {
      expect(def.type, type).toBe(type)
      expect(def.title, type).toBeTruthy()
      expect(def.component, type).toBeTruthy()
      expect(def.defaultSize.w, type).toBeGreaterThan(0)
      expect(def.defaultSize.w, type).toBeLessThanOrEqual(12)
      expect(def.defaultSize.h, type).toBeGreaterThan(0)
    }
  })

  it('hidden panels stay registered but are not offered in the picker', () => {
    const listed = listPanelDefinitions().map(d => d.type)
    for (const [type, def] of Object.entries(panelRegistry)) {
      if (def.hidden) {
        expect(listed, type).not.toContain(type)
        expect(getPanelDefinition(type)).toBeDefined()
      } else {
        expect(listed, type).toContain(type)
      }
    }
  })

  it('the default layout references only registered panel types with unique ids', () => {
    const layout = createDefaultLayout()
    const ids = new Set<string>()
    for (const panel of layout.panels) {
      expect(getPanelDefinition(panel.type), panel.type).toBeDefined()
      expect(ids.has(panel.id), panel.id).toBe(false)
      ids.add(panel.id)
      expect(panel.x).toBeGreaterThanOrEqual(0)
      expect(panel.x + panel.w).toBeLessThanOrEqual(12)
      expect(panel.y).toBeGreaterThanOrEqual(0)
      expect(panel.h).toBeGreaterThan(0)
    }
  })

  it('registers the collection search box under the Graph Collections name, not KSC', () => {
    // the product name is Graph Collections now; only the legacy URLs keep "KSC"
    expect(getPanelDefinition('graph-collections')?.title).toBe('Graph Collections')
    expect(getPanelDefinition('ksc-reports')).toBeUndefined()
  })
})
