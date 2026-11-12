<template>
  <div class="duplicate-uei-table">
    <table>
      <thead>
        <tr>
          <th scope="col">
            UEI
          </th>
          <th scope="col">
            Defined in
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="item in items"
          :key="item.uei"
          data-test="duplicate-uei-row"
        >
          <td>
            <code>{{ item.uei }}</code>
          </td>
          <td>
            <span
              v-for="source in item.sources"
              :key="source.name"
              class="source"
              data-test="duplicate-uei-source"
            >
              {{ source.name }}
              <OnmsTag
                v-if="!source.enabled"
                value="disabled"
                severity="info"
                data-test="disabled-tag"
              />
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts" setup>
import { OnmsTag } from '@opennms/onms-ui'
import { MibDuplicateUei } from '@/types/mibCompiler'

defineProps<{
  items: MibDuplicateUei[]
}>()
</script>

<style lang="scss" scoped>
.duplicate-uei-table {
  max-height: 12em;
  overflow-y: auto;
  margin: 0 0 1em;

  table {
    width: 100%;
    border-collapse: collapse;
  }

  th,
  td {
    text-align: left;
    vertical-align: top;
    padding: 0.25em 0.5em;
  }

  td:first-child {
    word-break: break-all;
  }

  .source {
    display: inline-flex;
    align-items: center;
    gap: 0.25em;
    margin-right: 0.75em;
    white-space: nowrap;
  }
}
</style>
