<template>
  <div class="card">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="onms-row">
      <div class="onms-col-12">
        <div class="dcb-container">
          <div class="table-container">
            <div class="title-search">
              <span class="title">Device Configuration</span>
              <DCBSearch class="dcb-search" />
            </div>
            <DCBTable />
          </div>
          <div v-if="false" class="filters-container">
            <DCBGroupFilters />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import DCBTable from '@/components/Device/DCBTable.vue'
import DCBGroupFilters from '@/components/Device/DCBGroupFilters.vue'
import DCBSearch from '@/components/Device/DCBSearch.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const deviceStore = useDeviceStore()
const menuStore = useMenuStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Device Config Backup', to: '#', position: 'last' }
  ]
})

onMounted(() => deviceStore.getDeviceConfigBackups(true))
</script>

<style scoped lang="scss">
@import '@/styles/onms-typography';

@mixin status-bar($color) {
  background: $color;
  background: linear-gradient(90deg, $color 1%, rgba(255, 255, 255, 0) 9%);
}
:deep(.success) {
  @include status-bar(var(--p-green-500));
}
:deep(.failed) {
  @include status-bar(var(--p-red-500));
}
:deep(.none) {
  @include status-bar(var(--p-datatable-header-cell-background));
}

.card {
  background: var(--p-content-background);
  padding: 0px 20px 20px 20px;

  .dcb-container {
    display: flex;

    .table-container {
      width: 35rem;
      flex: auto;

      .title-search {
        display: flex;
        justify-content: space-between;

        .title {
          @include onms-headline1;
          margin: 24px 0px 24px 19px;
          display: block;
        }

        .dcb-search {
          width: 30rem;
          margin-top: 16px;
        }
      }
    }

    .filters-container {
      width: 15rem;
    }
  }
}
</style>
