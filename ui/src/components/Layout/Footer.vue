<template>
  <div style="height: 1rem"></div>
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

<style lang="scss" scoped>
.footer {
  display: block;
  text-align: center;
  margin-left: -15px;
  margin-right: -15px;
  padding: 0.25rem 0.42rem;
  background-color: #e9ecef;
  border-top: 1px solid rgba(0, 0, 0, .125);
}

// The hardcoded light gray above stays light in dark mode; use the (dark)
// content background and a theme-aware border so the footer reads correctly on
// the Vue SPA pages (JSP pages are unaffected).
.open-dark .footer {
  background-color: var(--p-content-background);
  border-top-color: var(--p-content-border-color);
}
</style>
