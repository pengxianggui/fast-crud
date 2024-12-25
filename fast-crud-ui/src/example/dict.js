const pickerOptionsQ = {
    disabledDate(time) {
        return time.getTime() > Date.now();
    },
    shortcuts: [{
        text: '最近一周',
        onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
        }
    }, {
        text: '最近一个月',
        onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
        }
    }, {
        text: '最近三个月',
        onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
            picker.$emit('pick', [start, end]);
        }
    }]
}
const pickerOptionsE = {
    disabledDate(time) {
        return time.getTime() > Date.now();
    },
    shortcuts: [{
        text: '今天',
        onClick(picker) {
            picker.$emit('pick', new Date());
        }
    }, {
        text: '昨天',
        onClick(picker) {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24);
            picker.$emit('pick', date);
        }
    }, {
        text: '一周前',
        onClick(picker) {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', date);
        }
    }]
}
const sexOptions = [
    {
        label: '男',
        value: '1'
    },
    {
        label: '女',
        value: '0'
    }
]
const hobbyOptions = [
    {
        name: '篮球',
        code: '1'
    },
    {
        name: '足球',
        code: '2'
    },
    {
        name: '排球',
        code: '3'
    },
    {
        name: '乒乓球',
        code: '4'
    },
    {
        name: '羽毛球',
        code: '5'
    },
    {
        name: '台球',
        code: '6'
    },
    {
        name: '游泳',
        code: '7'
    }
]

export default {
    pickerOptionsQ,
    pickerOptionsE,
    sexOptions,
    hobbyOptions
}