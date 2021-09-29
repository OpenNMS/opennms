  
<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="logo" title="Home" type="home" url="/" />
    </template>


    <template v-slot:right>
      <Search />
      <FeatherButton @click="returnHandler">Return to previous UI</FeatherButton>
    </template>
  </FeatherAppBar>
</template>
    
<script setup lang="ts">
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import Logo from '@/assets/Logo.vue'
import Search from './Search.vue'
const returnHandler = () => window.location.href = '/opennms/'
const logo = Logo
</script>
  