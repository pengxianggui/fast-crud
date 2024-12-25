import moment from "moment";
import {isEmpty, isNumber, isString} from "../util/util";

export const dateFormat = function (val, format) {
    const date = new Date(val)
    const adjustFormat = format.replace(/yyyy/g, 'YYYY').replace(/dd/g, 'DD')
    return moment(date).format(adjustFormat)
}

export const ellipsis = function (val, len) {
    if (isEmpty(val) || !isString(val)) return '';
    if (!isNumber(len)) {
        console.warn('The "ellipsis" filter requires a numeric second argument as the maxLength.');
        return val;
    }

    return val.length > len ? val.slice(0, len) + '...' : val;
}