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
  | 'server'
  | 'wifiAccess'
  | 'printer'
  | 'cloud'

// Ported from etc/org.opennms.features.topology.app.icons.linkd.cfg. Values are
// normalized to the icon ids we actually render (server family collapses to
// 'server'); 'generic' entries are omitted -- an unresolved node keeps the plain
// circle rather than a generic glyph, so an icon always carries signal.
const ICON_MAP: Record<string, DeviceIconId> = {
  'linkd.group': 'cloud',
  'linkd.system.snmp.1.3.6.1.4.1.5813.1.13': 'server', // opennms_server
  'linkd.system.snmp.1.3.6.1.4.1.9.1.283': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.485': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.516': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.559': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.563': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.564': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.576': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.616': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.617': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.620': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.669': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.696': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.697': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.745': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.797': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.1021': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.1025': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.1034': 'wifiAccess',
  'linkd.system.snmp.1.3.6.1.4.1.9.1.1227': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.253.8.62.1.19.4.24.1': 'printer',
  'linkd.system.snmp.1.3.6.1.4.1.311.1.1.3.1.2': 'server',
  'linkd.system.snmp.1.3.6.1.4.1.311.1.1.3.1.3': 'server',
  'linkd.system.snmp.1.3.6.1.4.1.674.10895.3022': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.890.1.15': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.1916.2.71': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.2636.1.1.1.2.29': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.2636.1.1.1.2.39': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.3375.2.1.3.4.20': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.3375.2.1.3.4.43': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.4526.100.4.8': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.4526.100.10.7': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.4526.100.11.22': 'switch',
  'linkd.system.snmp.1.3.6.1.4.1.8072.3.2.10': 'server', // linux_file_server
  'linkd.system.snmp.1.3.6.1.4.1.8072.3.2.3': 'server',
  'linkd.system.snmp.1.3.6.1.4.1.8072.3.2.255': 'server',
  'linkd.system.snmp.1.3.6.1.4.1.22420.1.1': 'router',
  'linkd.system.snmp.1.3.6.1.4.1.30065.1.3011.7048.427.3648': 'switch'
}

/** enlinkd Topology.getIconKey: sysObjectId -> iconKey. */
export const iconKeyForSysObjectId = (sysObjectId?: string | null): string => {
  if (!sysObjectId) {
    return 'linkd.system'
  }
  return sysObjectId.startsWith('.')
    ? `linkd.system.snmp${sysObjectId}`
    : `linkd.system.snmp.${sysObjectId}`
}

/**
 * Resolve an iconKey to a device-type icon id via longest-prefix match (so the
 * most specific OID wins), or null when nothing matches (keep the plain circle).
 */
export const resolveDeviceIcon = (iconKey?: string | null): DeviceIconId | null => {
  if (!iconKey) {
    return null
  }
  if (ICON_MAP[iconKey]) {
    return ICON_MAP[iconKey]
  }
  let best: DeviceIconId | null = null
  let bestLen = -1
  for (const key of Object.keys(ICON_MAP)) {
    if (iconKey.startsWith(key) && key.length > bestLen) {
      best = ICON_MAP[key]
      bestLen = key.length
    }
  }
  return best
}

/** Convenience: device icon for a node's sysObjectId. */
export const deviceIconForSysObjectId = (sysObjectId?: string | null): DeviceIconId | null =>
  resolveDeviceIcon(iconKeyForSysObjectId(sysObjectId))

// --- SVG glyphs (white, transparent background) ---------------------------
// Drawn over the node's severity-colored disc by sigma's node-image program
// (drawingMode 'background'), so the glyph reads on any severity color.
const stroke =
  'fill="none" stroke="#ffffff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"'

const GLYPHS: Record<DeviceIconId, string> = {
  // Data-center router: central hub with four outward routing arrows
  // (the classic Cisco-style routing symbol, not a home/SOHO router).
  router: `<circle cx="12" cy="12" r="2.6" ${stroke}/><path d="M12 9.4V4M12 4l-1.7 1.7M12 4l1.7 1.7M12 14.6V20M12 20l-1.7-1.7M12 20l1.7-1.7M9.4 12H4M4 12l1.7-1.7M4 12l1.7 1.7M14.6 12H20M20 12l-1.7-1.7M20 12l-1.7 1.7" ${stroke}/>`,
  // Switch: a chassis with opposing (switching) arrows.
  switch: `<rect x="2" y="6" width="20" height="12" rx="2" ${stroke}/><path d="M6 9.8h9.5M15.5 9.8l-2-1.8M15.5 9.8l-2 1.8M18 14.2H8.5M8.5 14.2l2-1.8M8.5 14.2l2 1.8" ${stroke}/>`,
  // Server: a rack chassis of stacked units, each with a status LED.
  server: `<rect x="5" y="2.5" width="14" height="19" rx="1.5" ${stroke}/><path d="M5 8.5h14M5 14.5h14" ${stroke}/><circle cx="8" cy="5.5" r="0.9" fill="#ffffff"/><circle cx="8" cy="11.5" r="0.9" fill="#ffffff"/><circle cx="8" cy="17.5" r="0.9" fill="#ffffff"/>`,
  wifiAccess: `<path d="M4.5 12.5a10 10 0 0 1 15 0M8 16a5 5 0 0 1 8 0" ${stroke}/><circle cx="12" cy="19.5" r="1.2" fill="#ffffff"/>`,
  printer: `<path d="M6.5 9V3h11v6" ${stroke}/><rect x="3" y="9" width="18" height="8" rx="1.5" ${stroke}/><rect x="7" y="14.5" width="10" height="6" ${stroke}/>`,
  cloud: `<path d="M7 18a4 4 0 0 1 0-8 5 5 0 0 1 9.6-1.4A3.5 3.5 0 0 1 17 18Z" ${stroke}/>`
}

// Rasterize at high resolution (the loader uses the SVG's intrinsic width/height
// as the bitmap size, then it's scaled down onto the node — so a 24px source
// looks blurry on larger/retina nodes). We keep the 24-unit viewBox for the
// path coordinates but render at RENDER_PX so the texture stays crisp. base64
// avoids any data-URL escaping ambiguity.
const RENDER_PX = 128
const toDataUrl = (glyph: string): string => {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${RENDER_PX}" height="${RENDER_PX}" viewBox="0 0 24 24">${glyph}</svg>`
  return 'data:image/svg+xml;base64,' + btoa(svg)
}

/** iconId -> SVG data URL, ready for a sigma image node's `image` attribute. */
export const DEVICE_ICON_SVG: Record<DeviceIconId, string> = {
  router: toDataUrl(GLYPHS.router),
  switch: toDataUrl(GLYPHS.switch),
  server: toDataUrl(GLYPHS.server),
  wifiAccess: toDataUrl(GLYPHS.wifiAccess),
  printer: toDataUrl(GLYPHS.printer),
  cloud: toDataUrl(GLYPHS.cloud)
}
