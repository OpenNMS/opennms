<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12 headline3">Plugin Management</div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <table class="tl1 tl2 tl3" summary="Plugin Management">
          <thead>
            <tr>
              <th scope="col">ID</th>
              <th scope="col">Name</th>
              <th scope="col">Enable/Disable</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="plugin in plugins" :key="plugin.extensionID">
              <td>{{ plugin.extensionID }}</td>
              <td>{{ plugin.menuEntry }}</td>
              <td>
                <FeatherButton
                  @click="updatePluginStatus(plugin)"
                  secondary
                >{{ plugin.enabled ? 'Disable' : 'Enable' }}</FeatherButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
  
<script setup lang="ts">
import { Plugin } from '@/types'
import { PropType } from 'vue'
import { useStore } from 'vuex'
import { FeatherButton } from '@featherds/button'

const store = useStore()

const updatePluginStatus = (plugin: Plugin) => store.dispatch('pluginModule/updatePluginStatus', plugin)

defineProps({
  plugins: {
    required: true,
    type: Array as PropType<Plugin[]>
  }
})
</script>
  
<style lang="scss" scoped>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-bottom: 15px;
}
table {
  width: 100%;
  @include table;
}
</style>
