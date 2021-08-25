<template>
  <div class="p-grid page">
    <div class="p-col-12">
      <h1>Identify your network inventory</h1>
      <Steps />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted } from 'vue'
import Steps from '../components/Inventory/Steps.vue'
import { useStore } from 'vuex'

export default defineComponent({
  name: 'Node Inventory',
  components: {
    Steps
  },
  setup() {
    const store = useStore()
    onMounted(() => {
      store.dispatch('locationsModule/getLocations')
    })
  }
})
</script>
