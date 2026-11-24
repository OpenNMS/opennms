<template>
  <div class="jdbc-columns-editor">
    <table v-if="modelValue.length" class="columns" data-test="jdbc-columns">
      <thead>
        <tr>
          <th>
            Name <span class="required">*</span>
            <OnmsIcon v-onms-tooltip="JDBC_FIELD_HINTS.columnName" :icon="InfoIcon" class="hint" />
          </th>
          <th>
            Result column
            <OnmsIcon v-onms-tooltip="JDBC_FIELD_HINTS.dataSourceName" :icon="InfoIcon" class="hint" />
          </th>
          <th>
            Type <span class="required">*</span>
            <OnmsIcon v-onms-tooltip="JDBC_FIELD_HINTS.columnType" :icon="InfoIcon" class="hint" />
          </th>
          <th>
            Alias <span class="required">*</span>
            <OnmsIcon v-onms-tooltip="JDBC_FIELD_HINTS.alias" :icon="InfoIcon" class="hint" />
          </th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(column, index) in modelValue" :key="index" :data-test="`jdbc-column-${index}`">
          <td>
            <OnmsInputText
              :aria-label="`Name of column ${index + 1}`"
              :data-test="`jdbc-column-name-${index}`"
              :modelValue="column.name"
              :invalid="!!errors[index]?.name"
              fluid
              @update:modelValue="update(index, 'name', $event)"
            />
            <small v-if="errors[index]?.name" class="error">{{ errors[index].name }}</small>
          </td>
          <td>
            <OnmsInputText
              :aria-label="`Result column of column ${index + 1}`"
              :data-test="`jdbc-column-data-source-name-${index}`"
              :modelValue="column.dataSourceName"
              :placeholder="column.name"
              fluid
              @update:modelValue="update(index, 'dataSourceName', $event)"
            />
          </td>
          <td>
            <OnmsSelect
              :aria-label="`Type of column ${index + 1}`"
              :data-test="`jdbc-column-type-${index}`"
              :options="types"
              :modelValue="column.type"
              :invalid="!!errors[index]?.type"
              fluid
              @update:modelValue="update(index, 'type', $event)"
            />
          </td>
          <td>
            <OnmsInputText
              :aria-label="`Alias of column ${index + 1}`"
              :data-test="`jdbc-column-alias-${index}`"
              :modelValue="column.alias"
              :invalid="!!errors[index]?.alias"
              fluid
              @update:modelValue="update(index, 'alias', $event)"
            />
            <small v-if="errors[index]?.alias" class="error">{{ errors[index].alias }}</small>
          </td>
          <td>
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Remove column"
              aria-label="Remove column"
              :data-test="`jdbc-column-remove-${index}`"
              @click="remove(index)"
            />
          </td>
        </tr>
      </tbody>
    </table>

    <p v-else class="empty">No columns.</p>

    <OnmsButton variant="text" data-test="jdbc-column-add" @click="add">Add column</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import { JDBC_FIELD_HINTS } from '@/lib/jdbcDataCollectionHelpText'
import { getDefaultColumn } from '@/stores/jdbcDataCollectionStore'
import type { JdbcColumn, JdbcColumnErrors } from '@/types/jdbcDataCollection'
import { OnmsButton, OnmsIcon, OnmsIconButton, OnmsInputText, OnmsSelect } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const props = withDefaults(defineProps<{
  modelValue: JdbcColumn[]
  types: string[]
  errors?: JdbcColumnErrors[]
}>(), {
  errors: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: JdbcColumn[]]
}>()

const update = (index: number, field: keyof JdbcColumn, value: unknown) => {
  const next = props.modelValue.map(column => ({ ...column }))
  next[index] = { ...next[index], [field]: String(value ?? '') }
  emit('update:modelValue', next)
}

const add = () => {
  emit('update:modelValue', [...props.modelValue, getDefaultColumn()])
}

const remove = (index: number) => {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<style lang="scss" scoped>
.jdbc-columns-editor {
  .columns {
    width: 100%;
    border-collapse: collapse;

    th {
      text-align: left;
      font-weight: 600;
      padding: 0 0.5em 0.25em 0;
      white-space: nowrap;
    }

    td {
      padding: 0 0.5em 0.5em 0;
      vertical-align: top;
    }

    td:last-child {
      width: 1%;
    }
  }

  .hint {
    vertical-align: middle;
    color: var(--p-text-muted-color);
  }

  .required,
  .error {
    color: var(--p-red-500);
  }

  .error {
    display: block;
  }

  .empty {
    color: var(--p-text-muted-color);
    margin: 0 0 0.5em 0;
  }
}
</style>
