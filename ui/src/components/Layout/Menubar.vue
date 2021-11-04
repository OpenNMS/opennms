  
<template>
  <FeatherAppBar :labels="{ skip: 'main' }" content="app">
    <template v-slot:left>
      <FeatherAppBarLink class="logo" :icon="logo" title="Home" type="home" url="/" />
    </template>

    <template v-slot:right>
      <Search />
      <FeatherButton @click="returnHandler" class="return-btn">Return to previous UI</FeatherButton>
      <FeatherIcon
        :icon="lightDark"
        class="pointer light-dark"
        @click.native="toggleDarkLightMode(null)"
      />
    </template>
  </FeatherAppBar>
</template>
    
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FeatherAppBar, FeatherAppBarLink } from '@featherds/app-bar'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import LightDark from '@/assets/LightDark.vue'
import Logo from '@/assets/Logo.vue'
import Search from './Search.vue'

const returnHandler = () => window.location.href = '/opennms/'
const logo = Logo
const lightDark = LightDark
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
}
onMounted(async () => {
  const savedTheme = localStorage.getItem('theme')
  toggleDarkLightMode(savedTheme)
})
</script>

<style lang="scss" scoped>
.return-btn {
  background: var(--feather-secondary-variant);
  color: var(--feather-primary-text-on-color);
  margin-right: 20px;
}
</style>

<style lang="scss">
@import "@featherds/styles/themes/open-mixins";
body {
  background: var(--feather-background);
}
.open-light {
  @include open-light();
}
.open-dark {
  @include open-dark();
}
.light-dark {
  margin-right: 10px;
  margin-bottom: 6px;
}
</style>
  