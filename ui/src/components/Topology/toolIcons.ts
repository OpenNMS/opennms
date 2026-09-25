// Device icons for topology nodes — legacy-faithful.
//
// OpenNMS's Vaadin map derives a node's icon from its SNMP sysObjectId:
//   enlinkd Topology.getIconKey(node) -> "linkd.system" (no sysObjectId) or
//   "linkd.system.snmp.<sysObjectId>", which IconManager resolves to an SVG id
//   via a longest-prefix match against etc/org.opennms.features.topology.app.icons.linkd.cfg
//   (default "linkd.system" -> generic). We reproduce that here client-side:
//   the discovered Graph API already carries the computed iconKey on each
//   vertex, and for custom-view nodes we compute it from the node's sysObjectId.
//
// ROADMAP (overrides, not yet built): the legacy map also lets an operator
// override a vertex's icon manually (IconSelectionOperation -> per-vertex
// mapping). We should add, down the road: (1) icon overrides by node *category*
// (a more human-meaningful signal than raw OID), and (2) a custom per-node icon
// override persisted on the view. Both layer on top of this sysObjectId default.
// See topology_redesign/PARITY.md.

/** The recognized device-type icon ids we render a glyph for. */
export type DeviceIconId =
  | 'router'
  | 'switch'

import { defineComponent, h } from 'vue'

/**
 * Glyphs for the Edit tool strip that the shared icon set does not carry: a
 * pointer for the select tool and a rectangle for the box tool. Same shape as
 * the vendored icons, an inline 24-unit SVG filled with currentColor.
 */
const glyph = (name: string, paths: string[]) =>
  defineComponent({
    name,
    render() {
      return h(
        'svg',
        { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24' },
        paths.map(d => h('path', { d }))
      )
    }
  })

export const SelectToolIcon = glyph('SelectToolIcon', [
  'M6 3.5v14.2l3.9-3.2 2.4 5.5 2.2-1-2.4-5.4h5.1L6 3.5z'
])

export const BoxToolIcon = glyph('BoxToolIcon', [
  'M4 5h16v14H4V5zm2 2v10h12V7H6z'
])
