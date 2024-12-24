import FastUpload from "./src/fast-upload.vue";

FastUpload.install= (Vue) => {
    Vue.component(FastUpload.name, FastUpload)
}

export default FastUpload;