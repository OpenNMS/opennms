<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title">Identify your network inventory</div>
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

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.title {
  @include headline3();
}
</style>
