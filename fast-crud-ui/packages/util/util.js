export function defaultIfBlank(str, defaultStr) {
    return ifBlank(str) ? defaultStr : str;
}

export function ifBlank(str) {
    return str === undefined || str === null || str.trim().length === 0;
}

/**
 * 驼峰转=>?(默认下划线)
 * @param str
 * @param separator
 * @returns {*}
 */
export function camelCaseTo(str, separator = '_') {
    let temp = str.replace(/[A-Z]/g, function (match) {
        return separator + match.toLowerCase();
    });
    if (temp.slice(0, separator.length) === separator) { //如果首字母是大写，执行replace时会多一个_，这里需要去掉
        temp = temp.slice(1);
    }
    return temp
}

/**
 * ?(默认下划线)转驼峰
 * @param str
 * @param separator
 */
export function caseToCamel(str, separator = '_') {
    return str.split(separator).map((word, index) => {
        if (index === 0) {
            return word.toLowerCase();
        }
        return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
    }).join('');
}

/**
 * 判断值是否为对象. 数组、null等都将返回false, 只有严格的{}才会返回true
 * @param val
 */
export function isObject(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Object]'
}

export function isArray(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Array]'
}

export function isBoolean(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Boolean]'
}

export function isString(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object String]'
}

export function isNumber(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Number]'
}

export function isFunction(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Function]'
}

export function isNull(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Null]'
}

export function isUndefined(val) {
    let toStr = Object.prototype.toString.call(val);
    return toStr === '[object Undefined]'
}

/**
 * 返回值的类型:
 * [object String]、[object Number]、[object Object]、[object Boolean]、
 * [object Array]、[object Function]、[object Null]、[object Undefined]
 * @param value
 * @returns {string}
 */
export function typeOf(value) {
    return Object.prototype.toString.call(value);
}

/**
 * 判断一个值是否为空.
 * 如果是
 * 1. 字符串, 则判断是否为空字符串(空格也被视为空)
 * 2. 对象, 则判断是否无任何键值
 * 3. 数组, 则判断是否无任何数组成员
 * 4. null, 返回true
 * 5. undefined, true
 * 6. 其他情况均返回false
 */
export function isEmpty(value) {
    switch (typeOf(value)) {
        case '[object String]':
            return value.trim() === '';
        case "[object Object]":
            return Object.keys(value).length === 0;
        case "[object Array]":
            return value.length === 0;
        case "[object Undefined]":
        case "[object Null]":
            return true;
    }
    return false;
}

/**
 * 清空对象所有的键值
 * @param obj
 */
export function clear(obj) {
    if (isObject(obj)) {
        for (let key in obj) {
            delete obj[key]
        }
    }
}

/**
 * 将字符串转为对象或数组
 * @param str
 * @returns {{}|any}
 */
export function parse(str) {
    if (isEmpty(str)) {
        return {}
    }

    return JSON.parse(str)
}

export function deepClone(obj) {
    if (isObject(obj)) {
        return Object.assign({}, obj)
    }
    if (isArray(obj)) {
        return Object.assign([], obj)
    }
    return obj;
}

/**
 * @description merge 策略: 将opt2 merge到opt1, 对于opt1已有的key-value, 保持不变, 对于opt2中新的key-value, 追加到opt1中。传入
 * deep值表示是否深度执行merge逻辑(不传入则为true). 函数将更改opt1的值, 同时返回opt1
 * @param opt1 opt1中的k-v将保留。如果不是object类型或者是null类型，则直接返回op1
 * @param opt2 不会改变opt2。如果不是object类型或者是null类型，则直接返回op1
 * @param deep 是否深拷贝模式, 默认true
 * @param ignoreNullAndUndefined 若为true, 则当opt2中的键值如果是null或undefined, 则不会覆盖到opt1中。默认是false
 * @returns {} 返回merge后的opt1的深拷贝对象
 */
export function merge(opt1, opt2, deep = true, ignoreNullAndUndefined = false) {
    if (opt2 === null || !isObject(opt2)) {
        return opt1;
    }

    if (opt1 === null || !isObject(opt1)) {
        return opt1;
    }

    let deepMerge = function (obj1, obj2) {
        if (!isObject(obj1) || !isObject(obj2)) return;
        for (let key in obj2) {
            let valueOfObj1 = obj1[key]
            let valueOfObj2 = obj2[key]

            if (ignoreNullAndUndefined && (isUndefined(valueOfObj2) || isNull(valueOfObj2))) {
                continue
            }

            if (!(key in obj1)) {
                obj1[key] = deepClone(valueOfObj2);
            } else {
                if (!deep) return;
                deepMerge(valueOfObj1, valueOfObj2)
            }
        }
    };

    // deep merge
    deepMerge(opt1, opt2);
    return opt1;
}

/**
 * @description merge 策略2： 对两个对象中的属性和值执行merge操作, 将opt2中的key-value根据key merge到opt1上： 若op1也存在这个key，则取opt2这个key的值
 * 覆盖到opt1上； 若opt1中不存在, 则会被直接追加到opt1中， 因此函数会更改opt1, 执行完后, opt1将是merge后的对象。最后将opt1的深拷贝返回
 * @param opt1 opt1中的k-v将被覆盖。如果不是object类型或者是null类型，则直接返回op1
 * @param opt2 如果不是object类型或者是null类型，则直接返回op1
 * @param deep 是否深拷贝模式, 默认true
 * @param ignoreNullAndUndefined 若为true, 则当opt2中的键值如果是null或undefined, 则不会覆盖到opt1中。默认是false
 * @returns {} 返回merge后的opt1的深拷贝对象
 */
export function coverMerge(opt1, opt2, deep = true, ignoreNullAndUndefined = false) {
    if (opt2 === null || !isObject(opt2)) {
        return opt1;
    }

    if (opt1 === null || !isObject(opt1)) {
        return opt1;
    }

    let deepMerge = function (obj1, obj2) {
        if (!isObject(obj1) || !isObject(obj2)) return;
        for (let key in obj2) {
            let valueOfObj1 = obj1[key]
            let valueOfObj2 = obj2[key]

            if (ignoreNullAndUndefined && (isUndefined(valueOfObj2) || isNull(valueOfObj2))) {
                continue
            }

            if (key in obj1) {
                if (isObject(valueOfObj1) && isObject(valueOfObj2) && deep) {
                    deepMerge(valueOfObj1, valueOfObj2)
                } else {
                    obj1[key] = deepClone(valueOfObj2)
                }
            } else {
                obj1[key] = deepClone(valueOfObj2)
            }
        }
    };

    deepMerge(opt1, opt2);
    return opt1;
}

export function easyOptParse(cond, optMapping = {}) {
    for (const [op, opt] of Object.entries(optMapping)) {
        const regex = new RegExp(`^${op}`);
        if (regex.test(cond.val)) {
            cond.opt = opt;
            cond.val = cond.val.substring(op.length);
            break;
        }
    }
    return cond;
}