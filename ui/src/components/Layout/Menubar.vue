  
<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink :icon="logo" title="Home" type="home" url="/" />
    </template>

    <template v-slot:right>
      <Search v-if="!route.fullPath.includes('/map')" />
      <FeatherButton @click="returnHandler" class="return-btn">Return to previous UI</FeatherButton>
      <FeatherIcon
        :icon="LightDarkMode"
        class="pointer light-dark"
        @click="toggleDarkLightMode(null)"
      />
    </template>
  </FeatherAppBar>
</template>
    
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import LightDarkMode from '@featherds/icon/action/LightDarkMode'
import Logo from '@/assets/Logo.vue'
import Search from './Search.vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'

const store = useStore()
const route = useRoute()
const returnHandler = () => window.location.href = '/opennms/'
const logo = Logo
const theme = ref('')
const light = 'open-light'
const dark = 'open-dark'

const toggleDarkLightMode = (savedTheme: string | null) => {
  const el = document.body
  const newTheme = theme.value === light ? dark : light

  if (savedTheme && (savedTheme === light || savedTheme === dark)) {
    theme.value = savedTheme
    el.classList.add(savedTheme)
    return
  }

  // set the new theme on the body
  el.classList.add(newTheme)

  // remove the current theme
  if (theme.value) {
    el.classList.remove(theme.value)
  }

  // save the new theme in data and localStorage
  theme.value = newTheme
  localStorage.setItem('theme', theme.value)
  store.dispatch('appModule/setTheme', theme.value)
}
onMounted(async () => {
  const savedTheme = localStorage.getItem('theme')
  toggleDarkLightMode(savedTheme)
})
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
.return-btn {
  background: var($secondary-variant);
  color: var($primary-text-on-color);
  margin-right: 20px;
}
</style>

<style lang="scss">
@import "@featherds/styles/themes/open-mixins";
body {
  background: var($background);
}
.open-light {
  @include open-light;
}
.open-dark {
  @include open-dark;
}
.light-dark {
  font-size: 24px;
  margin-top: 2px;
}
</style>
  