<template>
  <TableCard>
    <div class="table-header">
      <h3>Services</h3>
      <OnmsButton data-test="package-service-create" @click="store.openServiceDrawer(CreateEditMode.Create)">
        New service
      </OnmsButton>
    </div>

    <OnmsTable :value="services" data-test="package-services-table">
      <OnmsColumn header="Service" field="name" />
      <OnmsColumn header="Interval (ms)" field="interval" />
      <OnmsColumn header="Status">
        <template #body="{ data }">{{ data.status || 'on' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Threshold group">
        <template #body="{ data }">{{ thresholdingGroupOf(data) }}</template>
      </OnmsColumn>
      <OnmsColumn header="User defined">
        <template #body="{ data }">{{ data.userDefined ? 'Yes' : 'No' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ index }">
          <div class="actions">
            <OnmsIconButton
              :icon="EditIcon"
              tooltip="Edit service"
              aria-label="Edit service"
              data-test="package-service-edit"
              @click="store.openServiceDrawer(CreateEditMode.Edit, index)"
            />
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Delete service"
              aria-label="Delete service"
              data-test="package-service-delete"
              @click="pendingDeleteIndex = index"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-if="!services.length"
      :content="{ msg: 'This package thresholds nothing until it has a service.' }"
      data-test="package-services-empty"
    />

    <OnmsConfirmationDialog
      v-if="pendingDeleteIndex >= 0"
      :visible="true"
      title="Delete this service?"
      actionButtonText="Delete"
      data-test="package-service-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDeleteIndex = -1"
    >
      <template #content>
        <p>Thresholding stops for this service on every interface the package selects.</p>
      </template>
    </OnmsConfirmationDialog>

    <PackageServiceDrawer />
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import PackageServiceDrawer from '@/components/ThresholdConfiguration/Package/PackageServiceDrawer.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLDING_GROUP_PARAMETER } from '@/lib/thresholdValidator'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { CreateEditMode } from '@/types'
import type { ThreshdService } from '@/types/thresholdConfig'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'

const store = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

const pendingDeleteIndex = ref(-1)

const services = computed(() => store.currentPackage?.services ?? [])

const thresholdingGroupOf = (service: ThreshdService) =>
  service.parameters?.find(parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER)?.value || '--'

const onConfirmDelete = async () => {
  const index = pendingDeleteIndex.value
  pendingDeleteIndex.value = -1

  store.removeService(index)

  const result = await store.saveCurrentPackage()

  showSnackBar({ msg: result.success ? 'Service deleted.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1em 0.75em 1em;

  h3 {
    margin: 0;
  }
}

.actions {
  display: flex;
  align-items: center;
  gap: 0.25em;
}
</style>
