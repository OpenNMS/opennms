<template>
  <div class="event-config">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage Event Configurations</h1>
      </div>
      <div class="action">
        <OnmsButton
          variant="outlined"
          data-test="reorder-sources-button"
          @click="store.showReorderSourcesDrawer()"
        >
          <OnmsIcon
            :icon="SortIcon"
            aria-hidden="true"
            focusable="false"
          />
          Reorder Sources
        </OnmsButton>
        <OnmsButton
          aria-haspopup="true"
          aria-controls="create-event-config-menu"
          data-test="create-menu-button"
          @click="toggleCreateMenu"
        >
          Create
          <OnmsIcon
            :icon="ArrowDown"
            aria-hidden="true"
            focusable="false"
          />
        </OnmsButton>
        <OnmsMenu
          id="create-event-config-menu"
          ref="createMenu"
          :items="createMenuItems"
        />
      </div>
    </div>
    <div class="tabs">
      <EventConfigTabContainer />
    </div>
  </div>
  <CreateEventConfigurationDialog />
  <ReorderEventConfigSourcesDrawer />
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import ReorderEventConfigSourcesDrawer from '@/components/EventConfiguration/ReorderEventConfigSourcesDrawer.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { getDefaultEventConfigEvent } from '@/stores/eventConfigDetailStore'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb, CreateEditMode } from '@/types'
import { OnmsButton, OnmsIcon, OnmsMenu, OnmsMenuItem } from '@opennms/onms-ui'
import SortIcon from '@opennms/onms-ui/icons/action/Sort.vue'
import ArrowDown from '@opennms/onms-ui/icons/navigation/ArrowDropDown.vue'

const store = useEventConfigStore()
const router = useRouter()
const menuStore = useMenuStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const createMenu = ref()
const createMenuItems = computed<OnmsMenuItem[]>(() => [
  {
    label: 'New Event Source',
    command: () => store.showCreateEventConfigSourceDialog()
  },
  {
    label: 'New Event Config',
    command: () => goToCreateEventConfig()
  }
])

const toggleCreateMenu = (event: Event) => {
  createMenu.value?.toggle(event)
}

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Event Configurations', to: '#', position: 'last' }
  ]
})

const goToCreateEventConfig = () => {
  const modificationStore = useEventModificationStore()
  modificationStore.openCreateWithoutSource(CreateEditMode.Create, getDefaultEventConfigEvent())
  router.push({ name: 'Event Configuration Create' })
}
</script>

<style lang="scss" scoped>
.event-config {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .action {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
}
</style>
