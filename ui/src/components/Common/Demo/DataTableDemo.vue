<template>
  <div>
    <DataTable
      :value="nodeDataValue"
      :customData="customData"
      :rowsPerPageOptions="[5, 3, 1]"
      :paginator="isData"
    />
  </div>
</template>

<script setup lang="ts">

import { onMounted, ref, computed } from 'vue'
import DataTable from '../DataTable.vue'
import { useStore } from 'vuex'

const store = useStore()
const isData = ref(false)
let customData: any = ref([]) //custom data

const provisionDService = computed(() => { return store.state.configuration.provisionDService })

const nodeDataValue = computed(() => {
  if (provisionDService.value) {

    let copyState = [], cronScheduleType: string[], valuePos: number, ele: number
    cronScheduleType = ['minute', 'hour', 'day of month', 'month', 'day of week']
    copyState = JSON.parse(JSON.stringify(provisionDService.value))

    let data = (copyState as any)["requisition-def"]
    if (data && data.length > 1) {
      customData.value = ['edit', 'delete']
      isData.value = true //show pagination 

      // cron-schedule expression changed to human readable format 
      const copydata = data.filter((rowData: any) => {
        let items = rowData['cron-schedule'].split(' ')
        items.forEach((element: any) => {
          if (!isNaN(element)) {
            ele = element
            valuePos = items.indexOf(element)
          }
        })
        return rowData['cron-schedule'] = `Every ${ele} ${cronScheduleType[valuePos]}`
      })
      //return updated data
      return copydata
    }
  }
  return []
})

onMounted(async () => {
  try {
    await store.dispatch('configuration/getProvisionDService')
  } catch {
    console.error("Error in API - Inside datatableDemo")
  }
})
</script>
