<template>
  <TableCard class="mib-files-table">
    <div class="header">
      <div class="section-left">
        <h3 data-test="table-title">{{ title }}</h3>
      </div>
      <div class="section-right">
        <div class="search-container">
          <FormField>
            <OnmsSearchInput
              :input-id="searchId"
              :modelValue="searchTerm"
              @update:modelValue="onChangeSearchTerm"
              data-test="search-input"
              :placeholder="`Search ${title}`"
              :aria-label="`Search ${title}`"
            />
          </FormField>
        </div>
      </div>
    </div>

    <OnmsTable
      v-if="visibleFiles.length"
      :value="visibleFiles"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      sortField="name"
      :sortOrder="1"
      class="data-table"
      :data-test="`${location}-mib-files-table`"
      :aria-label="`${title} Table`"
    >
      <OnmsColumn
        field="name"
        header="File Name"
        sortable
      >
        <template #body="{ data }">
          <div
            class="hyperlink"
            data-test="file-name"
            @click="emit('view', data)"
          >
            {{ data.name }}
          </div>
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="size"
        header="Size"
        sortable
      >
        <template #body="{ data }">
          {{ formatSize(data.size) }}
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="lastModified"
        header="Last Modified"
        sortable
      >
        <template #body="{ data }">
          {{ formatDate(data.lastModified) }}
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :title="`View ${data.name}`"
              data-test="view-button"
              :icon="ViewDetails"
              @click="emit('view', data)"
            />
            <OnmsIconButton
              v-if="location === 'pending'"
              :title="`Edit ${data.name}`"
              data-test="edit-button"
              :icon="EditIcon"
              @click="emit('edit', data)"
            />
            <OnmsIconButton
              v-if="location === 'pending'"
              :title="`Compile ${data.name}`"
              data-test="compile-button"
              :icon="BuildIcon"
              @click="emit('compile', data)"
            />
            <OnmsIconButton
              v-if="location === 'compiled'"
              aria-haspopup="true"
              :aria-controls="`${location}-row-menu`"
              :title="`Generate configuration from ${data.name}`"
              data-test="row-menu-button"
              :icon="MenuIcon"
              @click="toggleRowMenu($event, data)"
            />
            <OnmsIconButton
              :title="`Delete ${data.name}`"
              data-test="delete-button"
              :icon="DeleteIcon"
              @click="emit('delete', data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <OnmsMenu
      v-if="location === 'compiled'"
      :id="`${location}-row-menu`"
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <div v-if="!visibleFiles.length">
      <EmptyList
        :content="{ msg: `No ${title} found.` }"
        data-test="empty-list"
      />
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { computed, ref, useId } from 'vue'
import {
  OnmsColumn,
  OnmsIconButton,
  OnmsMenu,
  OnmsMenuItem,
  OnmsSearchInput,
  OnmsTable
} from '@opennms/onms-ui'
import { format as fnsFormat } from 'date-fns'
import BuildIcon from '@opennms/onms-ui/icons/action/Build.vue'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'
import MenuIcon from '@opennms/onms-ui/icons/navigation/MoreHoriz.vue'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import { MibDirectory, MibFileInfo } from '@/types/mibCompiler'

const props = defineProps<{
  location: MibDirectory
  title: string
  files: MibFileInfo[]
}>()

const emit = defineEmits<{
  view: [file: MibFileInfo]
  edit: [file: MibFileInfo]
  compile: [file: MibFileInfo]
  delete: [file: MibFileInfo]
  generateEvents: [file: MibFileInfo]
  generateDataCollection: [file: MibFileInfo]
  generateGraphs: [file: MibFileInfo]
}>()

const searchId = useId()
const searchTerm = ref('')

const visibleFiles = computed<MibFileInfo[]>(() => {
  const query = searchTerm.value.trim().toLowerCase()
  if (!query) {
    return props.files
  }
  return props.files.filter(file => file.name.toLowerCase().includes(query))
})

const onChangeSearchTerm = (value: string | undefined) => {
  searchTerm.value = value ?? ''
}

const rowMenu = ref()
const rowMenuTarget = ref<MibFileInfo | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    {
      label: 'Generate Events',
      command: () => emit('generateEvents', target)
    },
    {
      label: 'Generate Data Collection',
      command: () => emit('generateDataCollection', target)
    },
    {
      label: 'Generate Graph Templates',
      command: () => emit('generateGraphs', target)
    }
  ]
})

const toggleRowMenu = (event: Event, file: MibFileInfo) => {
  rowMenuTarget.value = file
  rowMenu.value?.toggle(event)
}

const formatSize = (size: number): string => {
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(2)} MB`
  }
  if (size >= 1024) {
    return `${(size / 1024).toFixed(2)} KB`
  }
  return `${size} B`
}

const formatDate = (timestamp: number): string => {
  return timestamp ? fnsFormat(new Date(timestamp), 'yyyy-MM-dd HH:mm:ss') : ''
}
</script>

<style lang="scss" scoped>
.mib-files-table {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .section-left {
      h3 {
        margin: 0;
      }
    }

    .section-right {
      .search-container {
        width: 400px;
      }
    }
  }

  .hyperlink {
    color: var(--p-primary-color);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
  }
}
</style>
