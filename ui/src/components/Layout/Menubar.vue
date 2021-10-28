  
<template>
  <div class="menubar">
    <div>
      <img
        @click="returnHomeHandler"
        :src="Logo"
        style="width: 8em; height: 2em; margin-left: 10px; cursor: pointer;"
      />
    </div>
    <div>
      <Search />
    </div>
    <div>
      <Button @click="returnHandler" class="p-button-md p-button-primary">Return to previous UI</Button>
    </div>
  </div>
</template>
    
<script setup lang="ts">
  import Button from 'primevue/button'
  import Logo from '@/assets/OpenNMS_Horizontal-Logo_Dark-BG.svg'
  import Search from './Search.vue'
  import { useRouter } from 'vue-router'

  const router = useRouter()
  const returnHandler = () => window.location.href = '/opennms/admin/index.jsp'
  const returnHomeHandler = () => router.push('/')
</script>
    
<style lang="scss">
$menuColor: #0a0c1b;
.menubar {
  display: flex;
  justify-content: space-between;
  background: $menuColor;
  height: 65px;
  padding: 12px;
}
</style>
  