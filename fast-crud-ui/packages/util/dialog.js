import Vue from 'vue';
import {Dialog} from 'element-ui';

/**
 * 动态打开弹窗
 * @param component 弹窗中的组件，可通过emit('ok', data)触发后续then, 或者 通过emit('cancel')取消弹窗触发catch
 * @param props 传递给component的props
 * @param dialogProps 传递给dialog的props, 其中还支持自定义的buttons数组，描述dialog按钮
 * @example
 * openDialog({
 *  component: YourForm,
 *  props: {
 *      model: {...}
 *  },
 *  dialogProps: {
 *      width: '50%',
 *      // 其它dialog支持的props...
 *      buttons: [
 *          {
 *              text: '确定',
 *              type: 'primary',
 *              onClick: (component) => { // component是你传入的YourForm组件实例
 *                  if (!component.validate()) { // YourForm里提供的校验方法
 *                      Message.warning('校验不通过!');
 *                      return; // 返回非Promise则不会关闭对话框
 *                  }
 *                  return Promise.resolve(component.getModel()); // YourForm提供的getModel获取表单数据, 走resolve逻辑
 *              }
 *          },
 *          {
 *              text: '取消',
 *              onClick: (component) => {
 *                  return Promise.reject(); // 走catch逻辑
 *              }
 *          }
 *          // 其它自定义按钮
 *      ],
 *      // TODO 支持event
 *  }
 * })
 * @returns {Promise<unknown>}
 */
export function openDialog({component, props = {}, dialogProps = {width: '50%'}}) {
    const _this = this;
    const {buttons = [], ...validDialogProps} = dialogProps;
    return new Promise((resolve, reject) => {
        const dialogInstance = new Vue({
            data() {
                return {
                    visible: true, // 控制 Dialog 的显示
                };
            },
            methods: {
                handleOk(data) {
                    this.visible = false;
                    resolve(data);
                },
                handleClose(whoClose) {
                    this.visible = false;
                    reject(whoClose);
                },
                cleanUp() {
                    document.body.removeChild(dialogInstance.$el);
                    this.$destroy();
                }
            },
            render(h) {
                return h(Dialog, {
                    class: ['fc-dynamic-dialog'],
                    props: {
                        ...validDialogProps,
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
                }, [
                    h(component, {
                        props,
                        ref: 'component',
                        on: {
                            ok: (data) => this.handleOk(data),
                            cancel: () => this.handleClose('component')
                        },
                    }),
                    // 具名插槽footer
                    h('template', {slot: 'footer'}, buttons.map(btn => {
                        return h('el-button', {
                            props: {
                                type: btn.type,
                                icon: btn.icon,
                                size: btn.size
                            },
                            on: {
                                click: () => {
                                    const componentInstance = dialogInstance.$refs.component;
                                    const returnObj = btn.onClick.call(_this, componentInstance);
                                    if (!(returnObj instanceof Promise)) {
                                        return;
                                    }
                                    returnObj.then((data) => this.handleOk(data))
                                        .catch(() => this.handleClose('dialog'));
                                }
                            }
                        }, btn.text);
                    }))
                ]);
            },
        }).$mount();

        document.body.appendChild(dialogInstance.$el)
    });
}