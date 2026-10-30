<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="help-page">
    <header class="page-header">
      <h1 class="page-title">{{ title }}</h1>
      <p class="page-subtitle">{{ subtitle }}</p>
    </header>

    <slot />

    <section v-for="section in sections" :key="section.title" class="help-section" data-test="help-section">
      <h2 class="section-title">{{ section.title }}</h2>
      <div class="tile-grid">
        <ResourceTile v-for="link in section.links" :key="link.href" :link="link" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import ResourceTile from '@/components/Help/ResourceTile.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { HelpSection } from '@/types/help'

const props = defineProps<{
  title: string
  subtitle: string
  route: string
  sections: HelpSection[]
}>()

const menuStore = useMenuStore()

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: menuStore.mainMenu?.homeUrl, isAbsoluteLink: true },
  { label: props.title, to: props.route, position: 'last' }
])
</script>

<style scoped lang="scss">
.help-page {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 0.5rem 2px 2rem 2px;
  max-width: 72rem;
}
.page-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
}
.page-subtitle {
  margin: 0.35rem 0 0 0;
  color: var(--p-text-muted-color);
  max-width: 48rem;
}
.section-title {
  margin: 0 0 0.75rem 0;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--p-text-muted-color);
}
.tile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(18rem, 1fr));
  gap: 1rem;
}
</style>
