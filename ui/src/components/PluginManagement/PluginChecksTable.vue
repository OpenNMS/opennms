<template>
  <OnmsTable :value="checks" dataKey="id" size="small" data-test="plugin-checks-table">
    <template #empty>
      <span data-test="no-checks">No checks were reported.</span>
    </template>
    <OnmsColumn field="id" header="Check" />
    <OnmsColumn header="Level">
      <template #body="{ data }">
        <OnmsTag :severity="CHECK_TAG[data.level as KarCheckLevel] ?? 'secondary'" :value="data.level" :data-level="data.level" data-test="check-level" />
      </template>
    </OnmsColumn>
    <OnmsColumn field="message" header="Message" />
  </OnmsTable>
</template>

<script setup lang="ts">
import { OnmsColumn, OnmsTable, OnmsTag } from '@opennms/onms-ui'
import { KarCheck, KarCheckLevel } from '@/types/pluginManagement'
import { CHECK_TAG } from './pluginDisplay'

defineProps<{
  checks: KarCheck[]
}>()
</script>
