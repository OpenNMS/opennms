<template>
  <nav
    ref="navRef"
    id="opennms-sidemenu-vue-container"
    class="onms-side-menu"
    :class="{ 'onms-side-menu--open': isOpen }"
    @mouseenter="onRailEnter"
    @mouseleave="onRailLeave"
  >
    <button
      type="button"
      class="onms-side-menu__toggle"
      :title="isPinned ? 'Collapse menu' : 'Expand menu'"
      :aria-label="isPinned ? 'Collapse menu' : 'Expand menu'"
      :aria-expanded="isPinned"
      @click="togglePinned"
    >
      <i class="pi" :class="isPinned ? 'pi-angle-double-left' : 'pi-angle-double-right'" aria-hidden="true" />
    </button>

    <TieredMenu
      ref="tieredMenuRef"
      :model="topPanels"
      class="onms-side-menu__menu"
      breakpoint="0px"
    >
      <template #item="{ item, props, hasSubmenu }">
        <a
          class="onms-side-menu__link"
          v-bind="props.action"
          :href="item.url || undefined"
          :target="item.target || undefined"
        >
          <span class="onms-side-menu__icon">
            <component :is="item.iconComponent" v-if="item.iconComponent" aria-hidden="true" />
          </span>
          <span class="onms-side-menu__label">{{ item.label }}</span>
          <i v-if="hasSubmenu" class="pi pi-angle-right onms-side-menu__chevron" aria-hidden="true" />
        </a>
      </template>
    </TieredMenu>
  </nav>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

import TieredMenu from 'primevue/tieredmenu'
import type { MenuItem } from 'primevue/menuitem'
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

const menuStore = useMenuStore()
const pluginStore = usePluginStore()
const { getIcon } = useMenuIcons()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const plugins = computed<Plugin[]>(() => pluginStore.plugins)

// isPinned is the persisted expanded/collapsed state (toggle button).
// isHovering expands the rail transiently (open-on-hover) without persisting.
const isPinned = ref<boolean>(menuStore.sideMenuExpanded() ?? false)
const isHovering = ref<boolean>(false)
const isOpen = computed<boolean>(() => isPinned.value || isHovering.value)

const tieredMenuRef = ref<any>(null)

const onRailEnter = () => {
  isHovering.value = true

  // PrimeVue's TieredMenu only opens submenus on hover once it is "dirty"
  // (after an initial click / keyboard interaction). Enable that flag on entry
  // so the rail behaves as open-on-hover from the first interaction, matching
  // the previous FeatherSidenav. Guarded so it degrades to click-to-open if the
  // internal flag ever changes.
  if (tieredMenuRef.value) {
    tieredMenuRef.value.dirty = true
  }
}

const onRailLeave = () => {
  isHovering.value = false

  // The flyout submenus are position:fixed (see positionFlyouts) so they are
  // visually detached from the rail. When the pointer leaves the rail+flyout,
  // close any open flyout via TieredMenu's hide() (clears activeItemPath) so it
  // doesn't linger orphaned after the rail collapses. hide() also resets the
  // internal `dirty` flag, which onRailEnter re-enables on the next hover.
  tieredMenuRef.value?.hide?.()
}

const onPerformLogout = async () => {
  await performLogout()
}

const topPanels = computed<MenuItem[]>(() => {
  // If user not logged in, don't display any menus
  if (!mainMenu.value.username) {
    return []
  }

  const allMenus = updateWithPluginsMenuItems(mainMenu.value.menus ?? [], plugins.value)

  return createPrimeMenuModel(allMenus, mainMenu.value.baseHref, getIcon, onPerformLogout)
})

const togglePinned = () => {
  isPinned.value = !isPinned.value
  menuStore.setSideMenuExpanded(isPinned.value)
}

// Push the main content aside for the pinned (persisted) expanded rail only;
// hover-expansion overlays the content instead of pushing it, to avoid
// reflowing the whole page on every hover. The COLLAPSED/base left offset is
// owned by each host's stylesheet (JSP #content, SPA .app-layout) so the two
// contexts can differ; when not pinned we clear the inline padding-left so that
// base applies. This keeps the collapsed gap "guaranteed" (JS never clobbers
// it) while still widening the push when the rail is pinned open.
const getPushedElement = (): HTMLElement | null => {
  try {
    return document.querySelector<HTMLElement>(props.pushedSelector)
  } catch {
    return null
  }
}

const applyPush = () => {
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

  flyoutObserver?.disconnect()
  cancelAnimationFrame(flyoutRaf)

  const nav = navRef.value

  if (nav) {
    nav.removeEventListener('scroll', scheduleFlyoutPosition, true)
  }

  window.removeEventListener('resize', scheduleFlyoutPosition)
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

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
  background-color: var(--feather-surface-dark);
  color: var(--feather-state-text-color-on-surface-dark);
  transition: width 0.1s linear;
  // The nav itself does not scroll (the inner .p-tieredmenu list does); keep it
  // visible so the toggle button and rail chrome are never clipped.
  overflow: visible;

  // Force the TieredMenu (and its flyout submenus, which are descendants of
  // this nav) onto the dark surface via PrimeVue design tokens.
  --p-tieredmenu-background: var(--feather-surface-dark);
  --p-tieredmenu-color: var(--feather-state-text-color-on-surface-dark);
  --p-tieredmenu-border-color: transparent;
  --p-tieredmenu-border-radius: 0;
  --p-tieredmenu-item-color: var(--feather-state-text-color-on-surface-dark);
  --p-tieredmenu-item-icon-color: var(--feather-state-text-color-on-surface-dark);
  --p-tieredmenu-item-focus-color: #fff;
  --p-tieredmenu-item-icon-focus-color: #fff;
  --p-tieredmenu-item-focus-background: rgba(255, 255, 255, 0.12);
  --p-tieredmenu-submenu-icon-color: var(--feather-state-text-color-on-surface-dark);
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
    outline: 2px solid var(--feather-primary);
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
