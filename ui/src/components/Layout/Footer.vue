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

<style lang="scss" scoped>
.footer {
  display: block;
  text-align: center;
  margin-left: -1rem;
  margin-right: -1rem;
  padding: 0.5rem 0.5rem;
  border-top: 1px solid rgba(0, 0, 0, .125);
  background-color: var(--p-content-background);
  border-top-color: var(--p-content-border-color);
}
</style>
