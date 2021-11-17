import useVuelidate from '@vuelidate/core'
import { reactive } from 'vue'
import reqDefModel from '../ReqDefinitionFormModel'
import threadpollModel from '../ThreadpoolModel'

export default {
  reqDef: reactive(new reqDefModel()),
  threadpool: reactive(new threadpollModel()),

  toModel() {
    const rules = {
      reqDef: this.reqDef.rules,
      threadpool: this.threadpool.rules
    }
    return useVuelidate(rules, this)
  }
}
