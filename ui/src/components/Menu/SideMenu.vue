<template>
  <nav
    ref="navRef"
    id="opennms-sidemenu-vue-container"
    class="onms-side-menu"
    :class="{ 'onms-side-menu--open': isPinned }"
    @mouseover="onRailMouseOver"
    @mouseleave="onRailMouseLeave"
  >
    <button
      type="button"
      class="onms-side-menu__toggle"
      v-onms-tooltip="{ value: isPinned ? 'Collapse menu (Ctrl+\\)' : 'Expand menu (Ctrl+\\)', showDelay: 300 }"
      :aria-label="isPinned ? 'Collapse menu' : 'Expand menu'"
      :aria-expanded="isPinned"
      aria-keyshortcuts="Control+\"
      @click="togglePinned"
    >
      <OnmsIcon :icon="isPinned ? ChevronLeft : ChevronRight" />
    </button>

    <TieredMenu
      ref="tieredMenuRef"
      :model="topPanels as never"
      class="onms-side-menu__menu"
      breakpoint="0px"
    >
      <template #item="{ item, props, hasSubmenu }">
        <a
          class="onms-side-menu__link"
          v-bind="props.action"
          v-onms-tooltip="{ value: item.label, disabled: isPinned || !item.topLevel || hasSubmenu, showDelay: HOVER_OPEN_DELAY_MS }"
          :href="item.url || undefined"
          :target="item.target || undefined"
        >
          <span class="onms-side-menu__icon">
            <component :is="item.iconComponent" v-if="item.iconComponent" aria-hidden="true" />
          </span>
          <span class="onms-side-menu__label">{{ item.label }}</span>
          <OnmsIcon v-if="hasSubmenu" :icon="ChevronRight" class="onms-side-menu__chevron" />
        </a>
      </template>
    </TieredMenu>
  </nav>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

// We are using PrimeVue TieredMenu directly here, rather than wrapping it in an OnmsUI component.
// We are reaching into TieredMenu internals (DOM queries in positionFlyouts, hide() in togglePinned,
// the `dirty` hover flag in the collapsed-rail hover handlers below), and also not using this
// anywhere else in the app, so we don't want to add a new OnmsUI component for it.
// If we ever need to use TieredMenu elsewhere, we can wrap it in an OnmsUI component at that time.
// eslint-disable-next-line no-restricted-imports
import TieredMenu from 'primevue/tieredmenu'
import { OnmsIcon, OnmsMenuItem } from '@opennms/onms-ui'
import ChevronLeft from '@opennms/onms-ui/icons/navigation/ChevronLeft.vue'
import ChevronRight from '@opennms/onms-ui/icons/navigation/ChevronRight.vue'
import { performLogout } from '@/services/logoutService'
import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'
import { Plugin } from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { createPrimeMenuModel, updateWithPluginsMenuItems } from './utils'
import useMenuIcons from './useMenuIcons'

const props = defineProps({
  pushedSelector: {
    type: String,
    required: true
  }
})

// Rail widths — keep in sync with the CSS custom properties below.
const RAIL_EXPANDED = '20rem'
// Small breathing room so pushed content isn't flush against the rail edge.
const RAIL_GAP = '0.25rem'
// What legacy stylesheets already use for the collapsed rail
// (--onms-legacy-content-offset in opennms-theme.scss).
const RAIL_COLLAPSED_OFFSET = 'calc(var(--onms-header-height, 3.75rem) + 0.25rem)'
// Matches the padding-left/margin-left transitions below.
const RAIL_TRANSITION_MS = 100

const menuStore = useMenuStore()
const pluginStore = usePluginStore()
const { getIcon } = useMenuIcons()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)

// isPinned is the persisted expanded/collapsed state (toggle button), and the
// only thing that sets the rail's width. The rail itself never resizes on
// hover (NMS-20167) — hover only opens the flyouts, in either state; see the
// hover handlers below.
const isPinned = ref<boolean>(menuStore.sideMenuExpanded() ?? false)

const onPerformLogout = async () => {
  await performLogout()
}

// `as never` at the :model binding above: PrimeVue's own MenuItem.label
// accepts a render function in addition to string, which our narrower
// OnmsMenuItem.label doesn't — TS rejects the plain assignment even though
// this is structurally what TieredMenu expects at runtime.
const topPanels = computed<OnmsMenuItem[]>(() => {
  // If user not logged in, don't display any menus
  if (!mainMenu.value.username) {
    return []
  }

  const allMenus = updateWithPluginsMenuItems(mainMenu.value.menus ?? [], plugins.value)

  return createPrimeMenuModel(allMenus, mainMenu.value.baseHref, getIcon, onPerformLogout)
})

const tieredMenuRef = ref<any>(null)

// Close any open flyout and drop out of hover mode, so the next hover has to
// dwell again.
//
// With nothing open, clearing `dirty` is the whole job (hide() would have done
// it as a side effect): TieredMenu leaves it set after hovering an entry that
// has no submenu, so skipping this would keep the rail in hover mode with no
// flyout to show for it, and the next hover would open instantly with no dwell.
//
// hide() also resets focusedItemInfo to index -1. That is right for a flyout
// the pointer merely hovered open — hover never focuses the menubar, so there
// is no position to lose — but not for one the user clicked or tabbed into,
// where it would discard their place in the menu. So when the menu actually
// holds focus, put the focused index back on the root entry that was open,
// which is where TieredMenu itself lands after closing a submenu (see its
// onItemClick). A deeper position is not restored as-is: it points into a
// submenu that is now closed, and TieredMenu would resolve the stale index
// against the root list.
const closeFlyouts = () => {
  const menu = tieredMenuRef.value

  if (!menu) {
    return
  }

  if (!menu.activeItemPath?.length) {
    menu.dirty = false
    return
  }

  const rootItem = menu.activeItemPath.find((item: { parentKey: string }) => item.parentKey === '')
  const keepFocus = menu.focused && rootItem

  menu.hide?.()

  if (keepFocus) {
    menu.focusedItemInfo = { index: rootItem.index, level: 0, parentKey: '' }
  }
}

const togglePinned = () => {
  isPinned.value = !isPinned.value
  menuStore.setSideMenuExpanded(isPinned.value)

  // The rail is mid-resize and any open flyout is about to be closed below, so
  // a pending dwell or close timer has nothing left to act on. Dropping
  // hoveredItem also means the next mouseover re-arms the dwell from scratch.
  clearHoverTimers()
  hoveredItem = null

  // Clicking the toggle button already closes the flyout via TieredMenu's
  // outside-click listener; this makes the keyboard shortcut behave the same —
  // otherwise the flyout would be repositioned mid-way through the rail's 0.1s
  // width transition and end up with a stale left offset once the transition
  // finishes.
  closeFlyouts()
}

// --- Hover-to-open flyouts (NMS-20273) -----------------------------------
//
// Hovering a top-level entry flies its submenu out, in either rail state. What
// the rail never does on hover is change its own width: an earlier iteration
// expanded it from under the pointer, and that — rather than the flyouts — was
// the jarring part. While collapsed, entries that are direct links (Topology,
// the maps) have no submenu, so they get a label tooltip on the same beat
// instead; see the v-onms-tooltip binding in the template, which shares this
// delay. Expanded, their labels are already visible and it stays suppressed.
//
// TieredMenu already opens submenus on hover, but only once its internal
// `dirty` flag is set — normally by a first click. Setting `dirty` the moment
// the pointer touches the rail makes flyouts fire at every entry the pointer
// merely passes over on its way elsewhere. So the FIRST entry has to be
// dwelled on: a delegated mouseover starts the timer, and when it elapses we
// set `dirty` and re-dispatch mouseenter on the item so PrimeVue's own
// onItemMouseEnter handler does the opening. After that the rail is in hover
// mode and TieredMenu switches between entries on its own, with no further
// dwell, until the pointer leaves the rail.
const HOVER_OPEN_DELAY_MS = 150
// Grace period on the way out: a flyout can be clamped away from its item's
// row (see positionFlyouts), so reaching it may take the pointer briefly
// outside the rail. Closing instantly would cut that travel off.
const HOVER_CLOSE_DELAY_MS = 200

let hoverOpenTimer = 0
let hoverCloseTimer = 0
let hoveredItem: HTMLElement | null = null

const clearHoverTimers = () => {
  window.clearTimeout(hoverOpenTimer)
  window.clearTimeout(hoverCloseTimer)
  hoverOpenTimer = 0
  hoverCloseTimer = 0
}

// mouseover bubbles (mouseenter does not), so one listener on the nav covers
// every item. An item's open flyout is a DOM descendant of that item, so
// hovering into the flyout resolves back to the same root item and is a no-op.
const onRailMouseOver = (event: MouseEvent) => {
  // Back inside the rail: cancel any pending close.
  window.clearTimeout(hoverCloseTimer)

  const target = event.target as HTMLElement | null
  const item = target?.closest<HTMLElement>('.p-tieredmenu-root-list > .p-tieredmenu-item') ?? null

  if (item === hoveredItem) {
    return
  }

  // Also covers the pointer landing on the rail's own chrome — the toggle
  // button, the gap below the last entry — where item is null. The dwell has
  // to be cancelled there too: its only guard is `hoveredItem !== item`, so
  // leaving hoveredItem set would let it fire for an entry the pointer has
  // already left, opening a flyout under it (reaching for the toggle is the
  // common case). Clearing hoveredItem also re-arms the dwell if the pointer
  // comes back to that same entry.
  window.clearTimeout(hoverOpenTimer)
  hoveredItem = item

  if (!item) {
    return
  }

  // Already in hover mode: TieredMenu's own mouseenter handler switches
  // flyouts, so no dwell and nothing for us to do.
  if (tieredMenuRef.value?.dirty) {
    return
  }

  hoverOpenTimer = window.setTimeout(() => {
    // The pointer may have moved on, or left the rail, while the timer was
    // pending.
    if (!tieredMenuRef.value || hoveredItem !== item) {
      return
    }

    // TieredMenu binds @mouseenter on the item's content wrapper, not on the
    // <li>, and mouseenter does not bubble — so dispatch it there.
    const content = item.querySelector<HTMLElement>(':scope > .p-tieredmenu-item-content')

    if (!content) {
      return
    }

    tieredMenuRef.value.dirty = true
    content.dispatchEvent(new MouseEvent('mouseenter'))
  }, HOVER_OPEN_DELAY_MS)
}

const onRailMouseLeave = () => {
  clearHoverTimers()
  hoveredItem = null

  hoverCloseTimer = window.setTimeout(closeFlyouts, HOVER_CLOSE_DELAY_MS)
}

// Global shortcut: Ctrl+\ toggles the rail expand/collapse, so the menu can be
// pinned/unpinned without first tabbing to the toggle button. Ctrl+\ is free of
// browser-shortcut conflicts in all major browsers (unlike e.g. Ctrl+Shift+M:
// Firefox responsive design mode, Chrome/Edge profile switcher). Matched on
// e.code (physical key) so it works regardless of keyboard layout; the other
// modifiers are excluded so a larger chord doesn't also trigger it.
const onGlobalKeydown = (e: KeyboardEvent) => {
  if (e.code !== 'Backslash' || !e.ctrlKey || e.shiftKey || e.altKey || e.metaKey || e.repeat) {
    return
  }

  // Don't steal the shortcut while the user is typing in an editable element.
  const target = e.target as HTMLElement | null

  if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.tagName === 'SELECT' || target.isContentEditable)) {
    return
  }

  e.preventDefault()
  togglePinned()
}

// Push the main content aside when the rail is pinned (persisted) expanded.
// The COLLAPSED/base left offset is owned by each host's stylesheet (JSP
// #content, SPA .app-layout) so the two contexts can differ; when not pinned we
// clear the inline padding-left so that base applies. This keeps the collapsed
// gap "guaranteed" (JS never clobbers it) while still widening the push when
// the rail is pinned open.
const getPushedElement = (): HTMLElement | null => {
  try {
    return document.querySelector<HTMLElement>(props.pushedSelector)
  } catch {
    return null
  }
}

// Vaadin sizes its UI in pixels from JS and only re-measures on window resize,
// so a page it owns has to be told the rail moved or the map keeps its old width
// and hangs off the right edge. Fired after the transition, so the measurement
// sees the settled geometry.
let vaadinResizeTimer = 0

const notifyVaadinLayout = () => {
  if (!document.querySelector('.v-app')) {
    return
  }

  window.clearTimeout(vaadinResizeTimer)
  vaadinResizeTimer = window.setTimeout(() => window.dispatchEvent(new Event('resize')), RAIL_TRANSITION_MS + 20)
}

const applyPush = () => {
  const offset = isPinned.value ? `calc(${RAIL_EXPANDED} + ${RAIL_GAP})` : RAIL_COLLAPSED_OFFSET

  // Published for pages that can't be pushed by the query below. bootstrap.jsp
  // only emits #content on its non-Vaadin branch, so a Vaadin page (topology)
  // has nothing to push and the expanded rail painted straight over it; its own
  // stylesheet offsets the app root with a *static* margin sized for the
  // collapsed rail. Legacy CSS reads this to track the live width instead — see
  // styles/opennms-styles.scss.
  const root = document.documentElement
  const changed = root.style.getPropertyValue('--onms-side-menu-offset') !== offset

  root.style.setProperty('--onms-side-menu-offset', offset)

  if (changed) {
    notifyVaadinLayout()
  }

  const el = getPushedElement()

  if (!el) {
    return
  }

  el.style.transition = 'padding-left 0.1s linear'
  el.style.paddingLeft = isPinned.value ? `calc(${RAIL_EXPANDED} + ${RAIL_GAP})` : ''
}

watch(isPinned, applyPush)

// The menu list scrolls vertically (see .p-tieredmenu overflow in the style
// block). That overflow would clip the flyout submenus, which open to the side
// (a scroll container clips absolutely-positioned descendants on BOTH axes,
// regardless of whether a scrollbar is showing). So promote each open
// FIRST-LEVEL flyout to position:fixed, anchored to its item's viewport rect —
// fixed elements escape the scroll container's clip. Nested submenus live inside
// the now-unclipped flyout and keep PrimeVue's own positioning.
const navRef = ref<HTMLElement | null>(null)
let flyoutRaf = 0
let flyoutObserver: MutationObserver | null = null

// Gap kept between a flyout and the viewport edges.
const FLYOUT_VIEWPORT_MARGIN = 8

const positionFlyouts = () => {
  const nav = navRef.value

  if (!nav) {
    return
  }

  const items = nav.querySelectorAll<HTMLElement>('.p-tieredmenu-root-list > .p-tieredmenu-item')
  // Keep flyouts within the rail's vertical band: never above the rail top (so
  // they don't overlap the fixed header) and never past the viewport bottom.
  const minTop = nav.getBoundingClientRect().top
  const maxBottom = window.innerHeight - FLYOUT_VIEWPORT_MARGIN
  const availableHeight = maxBottom - minTop

  items.forEach((item) => {
    const submenu = item.querySelector<HTMLElement>(':scope > .p-tieredmenu-submenu')

    if (!submenu || getComputedStyle(submenu).display === 'none') {
      return
    }

    const rect = item.getBoundingClientRect()

    submenu.style.setProperty('position', 'fixed', 'important')
    submenu.style.setProperty('inset-inline-start', 'auto', 'important')
    submenu.style.setProperty('left', `${rect.right}px`, 'important')

    // scrollHeight is the full content height regardless of any max-height
    // already applied, so it's a stable measure of the natural flyout height.
    if (submenu.scrollHeight <= availableHeight) {
      // Fits: align with the item, but nudge up if it would run off the bottom.
      let top = Math.max(minTop, rect.top)

      if (top + submenu.scrollHeight > maxBottom) {
        top = Math.max(minTop, maxBottom - submenu.scrollHeight)
      }

      submenu.style.setProperty('top', `${top}px`, 'important')
      submenu.style.removeProperty('max-height')
      submenu.style.removeProperty('overflow-y')
    } else {
      // Taller than the available band: pin to the top and scroll internally.
      submenu.style.setProperty('top', `${minTop}px`, 'important')
      submenu.style.setProperty('max-height', `${availableHeight}px`, 'important')
      submenu.style.setProperty('overflow-y', 'auto', 'important')
    }
  })
}

const scheduleFlyoutPosition = () => {
  cancelAnimationFrame(flyoutRaf)
  flyoutRaf = requestAnimationFrame(positionFlyouts)
}

onMounted(() => {
  applyPush()

  window.addEventListener('keydown', onGlobalKeydown)

  const nav = navRef.value

  if (nav) {
    // Watch class changes (PrimeVue toggles .p-tieredmenu-item-active when a
    // submenu opens, and .onms-side-menu--open when the rail expands) rather
    // than style, to avoid re-triggering on our own style writes.
    flyoutObserver = new MutationObserver(scheduleFlyoutPosition)
    flyoutObserver.observe(nav, { attributes: true, attributeFilter: ['class'], subtree: true })
    // scroll does not bubble; capture catches the inner list scrolling.
    nav.addEventListener('scroll', scheduleFlyoutPosition, true)
    window.addEventListener('resize', scheduleFlyoutPosition)
  }
})

onBeforeUnmount(() => {
  const el = getPushedElement()

  if (el) {
    el.style.paddingLeft = ''
  }

  window.clearTimeout(vaadinResizeTimer)
  clearHoverTimers()
  document.documentElement.style.removeProperty('--onms-side-menu-offset')

  flyoutObserver?.disconnect()
  cancelAnimationFrame(flyoutRaf)

  const nav = navRef.value

  if (nav) {
    nav.removeEventListener('scroll', scheduleFlyoutPosition, true)
  }

  window.removeEventListener('resize', scheduleFlyoutPosition)
  window.removeEventListener('keydown', onGlobalKeydown)
})
</script>

<style lang="scss" scoped>
@import "@/styles/onms-tokens";

// Fixed, collapsible side menu rail. Replaces FeatherSidenav/FeatherDock.
// Pinned dark (independent of the active light/dark app theme) so it stays
// visually consistent with the top menu bar.
.onms-side-menu {
  position: fixed;
  top: var(--onms-header-height, 3.75rem);
  left: 0;
  bottom: 0;
  z-index: 2000; // over the geomap, matching the previous FeatherDock
  display: flex;
  flex-direction: column;
  width: var(--onms-side-menu-collapsed, 3.75rem);
  background-color: var(--onms-surface-dark);
  color: var(--onms-state-text-color-on-surface-dark);
  transition: width 0.1s linear;
  // The nav itself does not scroll (the inner .p-tieredmenu list does); keep it
  // visible so the toggle button and rail chrome are never clipped.
  overflow: visible;

  // Force the TieredMenu (and its flyout submenus, which are descendants of
  // this nav) onto the dark surface via PrimeVue design tokens.
  --p-tieredmenu-background: var(--onms-surface-dark);
  --p-tieredmenu-color: var(--onms-state-text-color-on-surface-dark);
  --p-tieredmenu-border-color: transparent;
  --p-tieredmenu-border-radius: 0;
  --p-tieredmenu-item-color: var(--onms-state-text-color-on-surface-dark);
  --p-tieredmenu-item-icon-color: var(--onms-state-text-color-on-surface-dark);
  --p-tieredmenu-item-focus-color: #fff;
  --p-tieredmenu-item-icon-focus-color: #fff;
  --p-tieredmenu-item-focus-background: rgba(255, 255, 255, 0.12);
  --p-tieredmenu-submenu-icon-color: var(--onms-state-text-color-on-surface-dark);
  --p-tieredmenu-submenu-icon-focus-color: #fff;
  --p-tieredmenu-separator-border-color: rgba(255, 255, 255, 0.2);
}

.onms-side-menu--open {
  width: var(--onms-side-menu-expanded, 20rem);
}

.onms-side-menu__toggle {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 2.75rem;
  padding: 0 1.15rem;
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  font-size: 1.1rem;

  &:hover {
    opacity: 0.85;
  }

  &:focus-visible {
    outline: 2px solid var(--onms-primary);
    outline-offset: -2px;
  }
}

// The TieredMenu component root does not receive this component's scope id, so
// it must be reached with :deep(). Override PrimeVue's default 12.5rem min-width
// so the menu fits the rail, and drop the panel chrome (border/padding/bg).
// This is the scroll region (the toggle button above stays pinned): when the
// item list is taller than the rail it scrolls vertically. min-height:0 lets
// the flex item shrink below its content height so scrolling actually engages.
// overflow-x is hidden (never a horizontal scrollbar); the side-opening flyout
// submenus would be clipped by this overflow, so they are promoted to
// position:fixed in script (positionFlyouts) to escape the clip.
.onms-side-menu :deep(.p-tieredmenu) {
  flex: 1 1 auto;
  width: 100%;
  min-width: 0;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  border: none;
  background: transparent;
  padding: 0;
}

// Flyout submenus should be comfortably wide (labels are hidden in the rail).
.onms-side-menu :deep(.p-tieredmenu-submenu) {
  min-width: 14rem;
}

// Slot content is compiled in this component's scope, so these classes match
// without :deep(). The interactive link also carries PrimeVue's own
// p-tieredmenu-item-link class (merged via props.action).
.onms-side-menu__link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
  color: inherit;
  text-decoration: none;
}

.onms-side-menu__icon {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  width: 1.4rem;
  height: 1.4rem;

  :deep(svg) {
    width: 1em;
    height: 1em;
    fill: currentColor;
  }
}

.onms-side-menu__label {
  flex: 1 1 auto;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.onms-side-menu__chevron {
  flex: 0 0 auto;
  margin-left: auto;
  font-size: 0.85rem;
}

// Collapsed rail: only the top-level (root list) items are icon-only. Flyout
// submenus always keep their labels, since they only appear on hover/expand.
.onms-side-menu:not(.onms-side-menu--open) :deep(.p-tieredmenu-root-list > .p-tieredmenu-item > .p-tieredmenu-item-content) {
  .onms-side-menu__label,
  .onms-side-menu__chevron {
    display: none;
  }

  .onms-side-menu__link {
    justify-content: center;
  }
}

// Flyout submenus are absolutely-positioned descendants of this nav; keep them
// above surrounding page content.
:deep(.p-tieredmenu-submenu) {
  z-index: 2001;
}
</style>
