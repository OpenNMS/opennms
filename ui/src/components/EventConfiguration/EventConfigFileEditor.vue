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
          <div class="feather-col-3 ">
            <div class="save">
              <FeatherButton primary>Save</FeatherButton>
            </div>
            <div class="reset">
              <FeatherButton primary>Reset</FeatherButton>
            </div>
          </div>
          <div class="feather-col-9 editor-area">
            <Editor />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import BreadCrumbs from '../Layout/BreadCrumbs.vue';
import { breadcrumbItems } from './data';
import TopBar from '../FileEditor/TopBar.vue';
import { FeatherButton } from '@featherds/button';
import Editor from '../FileEditor/Editor.vue';
</script>

<style scoped lang="scss">
@import "@featherds/dropdown/scss/mixins";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
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

.save,
.reset {
  margin-left: 10px;
  button {
    margin-top: 5px;
    margin-bottom: 0px;
    width: 40%;
  }
}

</style>

