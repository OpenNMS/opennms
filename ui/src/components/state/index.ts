import useVuelidate from '@vuelidate/core'
import { reactive } from 'vue-demi'
import reqDefModel from '../models/reqDefModel'

export default {
  reqDef: reactive(new reqDefModel()),

  toModel() {
    const rules = {
      reqDef: this.reqDef.rules
    }
    return useVuelidate(rules, this)
  }
}
