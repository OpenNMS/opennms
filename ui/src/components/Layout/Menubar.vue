  
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
      <FeatherButton primary @click="returnHandler">Return to previous UI</FeatherButton>
    </div>
  </div>
</template>
    
<script setup lang="ts">
  import { FeatherButton } from '@featherds/button'
  import Logo from '@/assets/OpenNMS_Horizontal-Logo_Dark-BG.svg'
  import Search from './Search.vue'
  import { useRouter } from 'vue-router'

  const router = useRouter()
  const returnHandler = () => window.location.href = '/opennms/admin/index.jsp'
  const returnHomeHandler = () => router.push('/')
</script>
    
<style lang="scss">
@import "../Common/common.scss";
$menuColor: #0a0c1b;
.menubar {
  display: flex;
  justify-content: space-between;
  background: $menuColor;
  height: 65px;
  padding: 12px;
}
</style>
  