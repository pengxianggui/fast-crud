import moment from "moment";
export const dateFormat = function (val, format) {
    const date = new Date(val)
    const adjustFormat = format.replace(/yyyy/g, 'YYYY').replace(/dd/g, 'DD')
    return moment(date).format(adjustFormat)
}