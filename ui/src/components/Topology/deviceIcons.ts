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
  | 'datacenter'
  | 'network'
  | 'datastore'
  | 'virtualMachine'

// Ported from etc/org.opennms.features.topology.app.icons.linkd.cfg. Values are
// normalized to the icon ids we actually render (server family collapses to
// 'server'); 'generic' entries are omitted -- an unresolved node keeps the plain
// circle rather than a generic glyph, so an icon always carries signal.
const ICON_MAP: Record<string, DeviceIconId> = {
  'linkd.group': 'cloud',
  'vmware.DATACENTER_ICON': 'datacenter',
  'vmware.NETWORK_ICON': 'network',
  'vmware.DATASTORE_ICON': 'datastore',
  'vmware.HOSTSYSTEM_ICON': 'server',
  'vmware.VIRTUALMACHINE_ICON': 'virtualMachine',
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
/**
 * Memoized because the node reducer calls this per node per frame while panning,
 * and a miss -- which is most vertices, since enlinkd sends the generic
 * `linkd.system` for nearly all of them -- linear-scans every key in ICON_MAP.
 * The map is a module constant, so the answer for a given key never changes.
 */
const resolved = new Map<string, DeviceIconId | null>()

export const resolveDeviceIcon = (iconKey?: string | null): DeviceIconId | null => {
  if (!iconKey) {
    return null
  }
  const cached = resolved.get(iconKey)
  if (cached !== undefined) {
    return cached
  }
  const answer = resolveUncached(iconKey)
  resolved.set(iconKey, answer)
  return answer
}

const resolveUncached = (iconKey: string): DeviceIconId | null => {
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
  cloud: `<path d="M7 18a4 4 0 0 1 0-8 5 5 0 0 1 9.6-1.4A3.5 3.5 0 0 1 17 18Z" ${stroke}/>`,
  // A VMware datacenter: a building, matching how vSphere draws the container
  // rather than anything network-shaped.
  datacenter: `<path d="M3 20.5h18M5.5 20.5V9.5L12 5.5l6.5 4v11" ${stroke}/><path d="M10 20.5v-4.5h4v4.5" ${stroke}/>`,
  // A port group: one shared segment with attached endpoints. Deliberately not
  // the switch chassis, which means a physical device.
  network: `<path d="M3 8h18M7 8v3.5M12 8v3.5M17 8v3.5" ${stroke}/><circle cx="7" cy="14" r="1.7" ${stroke}/><circle cx="12" cy="14" r="1.7" ${stroke}/><circle cx="17" cy="14" r="1.7" ${stroke}/>`,
  // A datastore: the conventional stacked-platter cylinder.
  datastore: `<ellipse cx="12" cy="6.5" rx="6.5" ry="2.6" ${stroke}/><path d="M5.5 6.5v11c0 1.44 2.91 2.6 6.5 2.6s6.5-1.16 6.5-2.6v-11" ${stroke}/><path d="M5.5 12c0 1.44 2.91 2.6 6.5 2.6s6.5-1.16 6.5-2.6" ${stroke}/>`,
  // A guest inside a host: a machine drawn within a machine. Distinct from the
  // tall rack that means a host system.
  virtualMachine: `<rect x="2.5" y="5" width="19" height="14" rx="2" ${stroke}/><rect x="7.5" y="9" width="9" height="6" rx="1" ${stroke}/>`
}

/**
 * Power state, which VMware spells into the icon key's suffix rather than
 * sending as its own field. Rendered as a badge so the glyph keeps meaning the
 * kind of thing and the disc keeps meaning alarm severity.
 */
export type PowerState = 'on' | 'off' | 'standby' | 'suspended'

const POWER_STATE_SUFFIX: Record<string, PowerState> = {
  _ON: 'on',
  _OFF: 'off',
  _STANDBY: 'standby',
  _SUSPENDED: 'suspended'
}

/** Null for an unknown or absent state, which draws no badge at all. */
export const powerStateForIconKey = (iconKey?: string | null): PowerState | null => {
  if (!iconKey) {
    return null
  }
  for (const [suffix, state] of Object.entries(POWER_STATE_SUFFIX)) {
    if (iconKey.endsWith(suffix)) {
      return state
    }
  }
  return null
}

// Ringed in white so the badge separates from the severity disc underneath,
// whatever color that is.
const BADGE_FILL: Record<PowerState, string> = {
  on: '#37d067',
  off: '#8a9099',
  standby: '#4a9df8',
  suspended: '#f5a623'
}

/**
 * The badge is deliberately unlabelled on the canvas, so the inspector names the
 * state next to the same color: that is where a reader learns what green means,
 * without a legend taking up room on every view.
 */
const POWER_STATE_LABEL: Record<PowerState, string> = {
  on: 'Powered on',
  off: 'Powered off',
  standby: 'Standby',
  suspended: 'Suspended'
}

export const powerStateLabel = (state: PowerState): string => POWER_STATE_LABEL[state]

export const powerStateColor = (state: PowerState): string => BADGE_FILL[state]

const badgeSvg = (state: PowerState): string =>
  `<circle cx="18.5" cy="18.5" r="4.6" fill="${BADGE_FILL[state]}" stroke="#ffffff" stroke-width="1.4"/>`

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
  cloud: toDataUrl(GLYPHS.cloud),
  datacenter: toDataUrl(GLYPHS.datacenter),
  network: toDataUrl(GLYPHS.network),
  datastore: toDataUrl(GLYPHS.datastore),
  virtualMachine: toDataUrl(GLYPHS.virtualMachine)
}

/**
 * The image for a glyph, with a power-state badge composed into the same
 * texture. Sigma renders one program per node type, so a separate badge pass
 * would mean a custom node program; baking it into the SVG is equivalent on
 * screen and leaves the renderer alone.
 *
 * Memoized: the loader rasterizes per distinct URL, and these are stable.
 */
const badgedIcons = new Map<string, string>()

export const deviceIconImage = (iconId: DeviceIconId, powerState?: PowerState | null): string => {
  if (!powerState) {
    return DEVICE_ICON_SVG[iconId]
  }
  const key = `${iconId}:${powerState}`
  const cached = badgedIcons.get(key)
  if (cached) {
    return cached
  }
  const url = toDataUrl(GLYPHS[iconId] + badgeSvg(powerState))
  badgedIcons.set(key, url)
  return url
}
