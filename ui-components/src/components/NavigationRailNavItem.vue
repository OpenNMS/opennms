<template>
  <router-link
    custom
    :to="href"
    v-slot="{ isActive, href: routerHref, navigate }"
  >
    <FeatherAppRailNavItem
      :icon="icon"
      :title="title"
      @click="navigate"
      :href="routerHref"
      :class="{ selected: isActive }"
    />
  </router-link>
</template>

<script setup lang="ts">
import { FeatherAppRailNavItem } from '@featherds/app-rail'


defineProps({
  icon: {
    type: Object,
    required: true
  },
  href: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  }
})
</script>
