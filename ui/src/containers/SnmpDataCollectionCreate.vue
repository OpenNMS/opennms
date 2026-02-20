<template>
  <div class="snmp-data-collection-create-container">
    <div class="header">
      <div>
        <FeatherBackButton
          data-test="back-button"
          @click="handleCancel"
        >
          Go Back
        </FeatherBackButton>
      </div>
      <div>
        <h3>Create SNMP Data Collection Source</h3>
      </div>
    </div>
    <div class="content">
      <div class="system-defs">
        <SystemDefTable />
      </div>
      <div class="mib-groups"></div>
      <div class="resource-types"></div>
    </div>
    <div class="footer">
      <FeatherButton
        @click="handleCancel"
        data-test="cancel-button"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="create-button"
      >
        Create
      </FeatherButton>
    </div>
  </div>
</template>

<script lang="ts" setup>
import SystemDefTable from '@/components/SnmpDataCollectionCreate/SystemDefTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'

const store = useSnmpDataCollectionDetailStore()
const router = useRouter()

const handleCancel = () => {
  if (store.selectedCollectionSource) {
    router.push({ name: 'SNMP Data Collection Detail', params: { id: store.selectedCollectionSource.id } })
  } else {
    router.push({ name: 'SNMP Data Collection' })
  }
}

onMounted(async () => {
  await store.fetchMibGroupNames()
  await store.fetchResourceTypeNames()
})
</script>

<style lang="scss" scoped>
.snmp-data-collection-create-container {
  padding: 20px;

  .header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;

    h3 {
      margin: 0;
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;

    button {
      margin: 0;
    }
  }
}
</style>

