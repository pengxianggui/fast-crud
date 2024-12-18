import Vue from 'vue';
import {Dialog} from 'element-ui';

export function openDialog({component, props = {}, dialogProps = {width: '50%'}}) {
    return new Promise((resolve, reject) => {
        const dialogInstance = new Vue({
            data() {
                return {
                    visible: true, // 控制 Dialog 的显示
                };
            },
            methods: {
                handleClose(whoClose) {
                    this.visible = false;
                    reject(whoClose);
                },
                cleanUp() {
                    this.$destroy();
                    document.body.removeChild(dialogInstance.$el);
                }
            },
            render(h) {
                return h(Dialog, {
                    class: ['fc-dynamic-dialog'],
                    props: {
                        ...dialogProps,
                        visible: this.visible
                    },
                    on: {
                        "update:visible": (val) => {
                            this.visible = val;
                            if (!val) {
                                this.cleanUp();
                            }
                        },
                        close: () => this.handleClose('dialog'),
                    },
                }, [h(component, {
                    props,
                    on: {
                        ok: (data) => {
                            resolve(data); // 子组件触发 submit 时，返回结果并关闭弹窗
                            this.visible = false;
                        },
                        cancel: () => this.handleClose('component')
                    },
                }),]);
            },
        }).$mount();

        document.body.appendChild(dialogInstance.$el)
    });
}