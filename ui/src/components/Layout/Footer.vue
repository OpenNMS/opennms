<template>
  <div class="footer">
    <p>
      OpenNMS <a href="../about/index.jsp">Copyright</a> © {{ mainMenu.copyrightDates || '--' }}
      <a href="http://www.opennms.com/">The OpenNMS Group, Inc.</a>
      OpenNMS&reg; is a registered trademark of
      <a href="http://www.opennms.com">The OpenNMS Group, Inc.</a>
      - Version: {{ mainMenu.version || '--' }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { useMenuStore } from '@/stores/menuStore'
import { MainMenu } from '@/types/mainMenu'

const menuStore = useMenuStore()

const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)

</script>

<style lang="scss">
// Shared footer band height, mirroring --onms-header-height in Menubar.vue. Pages
// that size themselves against the height the app shell leaves them subtract this
// (Map, Logs, SCV), so it lives here, next to the element it describes, instead of
// being repeated as a literal in each of them.
//
// It is the band this component actually draws: one 1.5rem line of text, 0.5rem of
// padding above and below, and the 1px top border. `.footer` takes its min-height
// from the token, so the two cannot drift apart. Note it is a *min*: at very narrow
// widths the copyright line wraps and the band grows past the token, which is the
// one case those page calculations still under-count.
:root {
  --onms-footer-height: calc(1.5rem + 1rem + 1px);
}
</style>

<style lang="scss" scoped>
.footer {
  display: block;
  text-align: center;
  min-height: var(--onms-footer-height);
  margin-left: -1rem;
  margin-right: -1rem;
  padding: 0.5rem 0.5rem;
  border-top: 1px solid rgba(0, 0, 0, .125);
  background-color: var(--p-content-background);
  border-top-color: var(--p-content-border-color);
}
</style>
