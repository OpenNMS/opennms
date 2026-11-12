<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  Configurable system-wide dashboard (NMS-19851). Mounted at /dashboard during
  development; the cutover (milestone 7) repoints the home route here and retires
  the JSP index page.
-->
<template>
  <div
    id="dashboard-root"
    class="dashboard-container"
  >
    <DashboardToolbar />
    <DashboardGrid />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import DashboardToolbar from '@/components/Dashboard/DashboardToolbar.vue'
import DashboardGrid from '@/components/Dashboard/DashboardGrid.vue'
import { useDashboardRefresh } from '@/composables/useDashboardRefresh'
import { useDashboardStore } from '@/stores/dashboardStore'

const store = useDashboardStore()

// Single global refresh timer for the whole dashboard.
useDashboardRefresh()

onMounted(() => {
  // re-entering the route must not blow away unsaved edits held in the
  // app-lifetime store; only (re)load when there is nothing to lose
  if (!store.isDirty) {
    store.load()
  }
})
</script>

<style scoped lang="scss">
.dashboard-container {
  // fill the app's main content area and scroll INTERNALLY so a tall dashboard
  // never overflows past the app footer/copyright bar.
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--p-surface-ground, var(--p-content-background, #f3f5f7));

  &:fullscreen {
    overflow: hidden;
  }

  // CSS "maximize" fallback when the Fullscreen API is blocked
  &.dashboard-maximized {
    position: fixed;
    inset: 0;
    z-index: 2000;
  }
}
</style>

<!-- Global (non-scoped) rules. -->
<style lang="scss">
// PrimeVue overlays teleport to <body>, outside the app font scope, so they fall
// back to the serif default. Keep them sans-serif.
.p-select-overlay,
.p-select-list,
.p-multiselect-overlay,
.p-multiselect-list,
.p-popover,
.p-popover * {
  font-family: 'OpenSans', Helvetica, Arial, sans-serif;
}

// Only when the dashboard is the active page: give the app content chain a
// definite height so the dashboard can scroll internally (and not push past the
// footer). Scoped via :has() so other pages keep their normal auto height.
.app-content-container:has(#dashboard-root),
.main-content:has(#dashboard-root) {
  height: 100%;
  min-height: 0;
}
</style>
