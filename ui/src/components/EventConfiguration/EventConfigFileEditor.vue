<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbItems" />
    </div>
  </div>

  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <TopBar v-if="false" />
        <div class="feather-row">
          <transition name="fade">
            <div class="feather-col-3" v-if="true">
              <FileSidebar />
            </div>
          </transition>
          <div :class="`feather-col-${showHelp ? 8 : 9} editor-area`">
            <Editor />
          </div>
          <div>
             <transition name="fade">
            <div class="feather-col-4" v-if="showHelp">
              <Help />
            </div>
          </transition>
            <FeatherButton class="help-btn" text @click="!showHelp">
              Help
            </FeatherButton>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BreadCrumbs from '../Layout/BreadCrumbs.vue';
import { breadcrumbItems } from './data';
import TopBar from '../FileEditor/TopBar.vue';
import FileSidebar from '../FileEditor/FileSidebar.vue';
import { FeatherButton } from '@featherds/button';
import Editor from '../FileEditor/Editor.vue';
import Help from '../FileEditor/Help.vue';

const showHelp = ref(false)
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/themes/variables";

.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
  position: relative;
}

.editor-area {
  padding: 10px;
}

.help-btn {
  position: absolute;
  right: 30px;
  top: 0px;
}
</style>