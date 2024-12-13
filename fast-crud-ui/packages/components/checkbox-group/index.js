import FastCheckboxGroup from './src/fast-checkbox-group.vue'

FastCheckboxGroup.install = (Vue) => {
    Vue.component(FastCheckboxGroup.name, FastCheckboxGroup)
}

export default FastCheckboxGroup