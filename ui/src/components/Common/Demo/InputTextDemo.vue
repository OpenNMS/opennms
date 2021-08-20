<template>
            <InputText
              id="inputText01"
              primeClass="lg"
              type="text"
              placeholder="Please Enter Text"
              v-model="show"
            />
            <p>Input Text :: {{show}}</p>

</template>

<script setup lang="ts">

import {ref} from 'vue'
import InputText from '../InputText.vue'

const show = ref('');

</script>