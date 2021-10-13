<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <div class="title">Identify your network inventory</div>
        <Steps />
      </div>
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
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin: 15px;
  .title {
    @include headline1();
  }
}
</style>
