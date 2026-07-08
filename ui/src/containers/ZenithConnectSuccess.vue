<template>
  <div class="card">
    <div class="onms-row">
      <div class="onms-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="onms-row">
      <div class="onms-col-12">
        <div class="zc-container">
          <div class="content-container">
            <div class="title-search">
              <span class="title">Zenith Connect Success</span>
            </div>
            <div>
              <div>Connection response:</div>
              <div class="spacer-medium"></div>
              <PDataTable
                class="kv-table"
                :value="responseRows"
                stripedRows
                size="small"
              >
                <PColumn field="label" />
                <PColumn field="value" />
              </PDataTable>
              <div class="spacer-medium"></div>
              <div>
                <div>
                  Click to save values. You will then be redirected back to /zenith-connect to view your existing connections.
                </div>
                <PButton
                  :disabled="savedSuccess"
                  label="Save Values"
                  @click="onSaveValues"
                />
              </div>
              <div v-if="savedSuccess">
                <div>
                  Click to return to view connections. Eventually this will be automatic.
                </div>
                <PButton
                  :disabled="!savedSuccess"
                  label="View Connections"
                  @click="onViewConnections"
                />
              </div>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { v2 } from '@/services/axiosInstances'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { useRoute, useRouter } from 'vue-router'

const PButton = Button
const PDataTable = DataTable
const PColumn = Column

const menuStore = useMenuStore()
const route = useRoute()
const router = useRouter()

const accessToken = ref('')
const refreshToken = ref('')
const nmsUsername = ref('')
const nmsSystemId = ref('')
const nmsDisplayName = ref('')
const savedDisplay = ref('--')
const savedSuccess = ref(false)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const responseRows = computed(() => [
  { label: 'NMS Username', value: nmsUsername.value },
  { label: 'NMS System ID', value: nmsSystemId.value },
  { label: 'NMS Display Name', value: nmsDisplayName.value },
  { label: 'Access Token', value: accessToken.value },
  { label: 'Refresh Token', value: refreshToken.value },
  { label: 'Saved?', value: savedDisplay.value }
])

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Zenith Connect Success', to: '#', position: 'last' }
  ]
})

const onSaveValues = async () => {
  const requestData = {
    accessToken: accessToken.value,
    refreshToken: refreshToken.value,
    nmsUsername: nmsUsername.value,
    nmsSystemId: nmsSystemId.value,
    nmsDisplayName: nmsDisplayName.value,
    clientId: 'zenith'
  }

  savedDisplay.value = 'Connecting...'

  try {
    const resp = await v2.post('/zenith-connect/registration-success', requestData)

    if (resp.status === 200) {
      savedDisplay.value = 'Saved!'
    }

    savedDisplay.value = 'Did not save'
  } catch (err) {
    console.error('Error!: ', err)
    savedDisplay.value = `Error!: ${err}`
  }
}

const onViewConnections = () => {
  router.push('zenith-connect')
}

onMounted(() => {
  // TODO: actual implementation, don't allow duplicate registrations
  accessToken.value = route.query.accessToken as string ?? ''
  refreshToken.value = route.query.refreshToken as string ?? ''
  nmsUsername.value = route.query.nmsUsername as string ?? ''
  nmsSystemId.value = route.query.nmsSystemId as string ?? ''
  nmsDisplayName.value = route.query.nmsDisplayName as string ?? ''
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

.card {
  background: var(--p-content-background);
  padding: 0px 20px 20px 20px;

  .zc-container {
    display: flex;

    .content-container {
      width: 35rem;
      flex: auto;

      .title-search {
        display: flex;
        justify-content: space-between;

        .title {
          @include headline1;
          margin: 24px 0px 24px 19px;
          display: block;
        }
      }

      .input {
        width: 50%;
      }

      .spacer-medium {
        margin-bottom: 0.25rem;
      }
    }
  }
}

// Key/value table: hide the (empty) header row.
.kv-table {
  max-width: 40rem;

  :deep(.p-datatable-thead) {
    display: none;
  }
}
</style>
