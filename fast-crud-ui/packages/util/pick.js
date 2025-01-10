import Vue from 'vue';
import {Message} from 'element-ui';
import {FastTableOption} from "../index";
import {defaultIfEmpty, isEmpty, isFunction} from "./util";
import {openDialog} from "./dialog";
import FastTable from '../components/table'

/**
 * pick弹窗选择表格数据
 * @param option FastTableOption实例
 * @param multiple 是否多选
 * @param dialog 弹窗配置
 * @returns {Promise<*>|*}
 */
export function pick({option, multiple = false, dialog = {width: '70%'}}) {
    if (isEmpty(option)) {
        throw new Error("option 不能为空!");
    }
    if (!(option instanceof FastTableOption)) {
        throw new Error("option 必须是 FastTableOption实例");
    }

    option.insertable = false;
    option.updatable = false;
    option.deletable = false;
    option.enableMulti = (multiple === true);

    const dynamicFastTable = Vue.extend({
        name: 'DynamicFastTable',
        render(h) {
            const slotContent = isFunction(option.render) ? option.render(h) : null;
            return h(FastTable, {props: {option}, ref: 'table'}, slotContent);
        }
    });

    const buttons = defaultIfEmpty(dialog.buttons, [
            {
                text: '确定',
                type: 'primary',
                size: option.style.size,
                onClick: (instance) => {
                    const tableRef = instance.$refs.table;
                    const data = multiple ? tableRef.getCheckedRows() : tableRef.getChoseRow();
                    if (isEmpty(data)) {
                        Message.warning('请选择数据');
                        return; // 返回非Promise则不会关闭对话框
                    }
                    return Promise.resolve(data);
                }
            },
            {
                text: '取消',
                size: option.style.size,
                onClick: (instance) => {
                    return Promise.reject();
                }
            }
        ]
    );

    return openDialog.call(this, {
        component: dynamicFastTable,
        dialogProps: {
            ...dialog,
            buttons: buttons
        }
    })
}