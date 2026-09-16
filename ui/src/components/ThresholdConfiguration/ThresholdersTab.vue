<template>
  <div class="thresholders-tab">
    <p class="intro">
      A thresholder binds a service name to the class that evaluates its thresholds. Most systems never need
      to change this.
    </p>

    <TableCard>
      <div class="table-header">
        <h3>Thresholders</h3>
        <OnmsButton data-test="thresholder-create" @click="store.openThresholderDrawer(CreateEditMode.Create)">
          New thresholder
        </OnmsButton>
      </div>

      <OnmsTable :value="store.config.thresholder" data-test="thresholders-table">
        <OnmsColumn header="Service" field="service" />
        <OnmsColumn header="Class name" field="className" />
        <OnmsColumn header="Parameters">
          <template #body="{ data }">{{ data.parameters?.length || 0 }}</template>
        </OnmsColumn>
        <OnmsColumn header="Actions">
          <template #body="{ index }">
            <div class="actions">
              <OnmsIconButton
                :icon="EditIcon"
                tooltip="Edit thresholder"
                aria-label="Edit thresholder"
                data-test="thresholder-edit"
                @click="store.openThresholderDrawer(CreateEditMode.Edit, index)"
              />
              <OnmsIconButton
                :icon="DeleteIcon"
                severity="danger"
                tooltip="Delete thresholder"
                aria-label="Delete thresholder"
                data-test="thresholder-delete"
                @click="pendingDeleteIndex = index"
              />
            </div>
          </template>
        </OnmsColumn>
      </OnmsTable>

      <EmptyList
        v-if="!store.config.thresholder.length"
        :content="{ msg: 'No thresholders configured.' }"
        data-test="thresholders-empty"
      />
    </TableCard>

    <OnmsConfirmationDialog
      v-if="pendingDeleteIndex >= 0"
      :visible="true"
      title="Delete this thresholder?"
      actionButtonText="Delete"
      data-test="thresholder-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDeleteIndex = -1"
    >
      <template #content>
        <p>Services of this type will no longer be thresholded.</p>
      </template>
    </OnmsConfirmationDialog>

    <ThresholderDrawer />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ThresholderDrawer from '@/components/ThresholdConfiguration/ThresholderDrawer.vue'
import useSnackbar from '@/composables/useSnackbar'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { CreateEditMode } from '@/types'
import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'

const store = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

const pendingDeleteIndex = ref(-1)

const onConfirmDelete = async () => {
  const index = pendingDeleteIndex.value
  pendingDeleteIndex.value = -1

  const result = await store.removeThresholder(index)

  showSnackBar({ msg: result.success ? 'Thresholder deleted.' : result.message, error: !result.success })
}
</script>

<style lang="scss" scoped>
.thresholders-tab {
  .intro {
    margin: 0 0 1em 0;
  }

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
}
</style>
