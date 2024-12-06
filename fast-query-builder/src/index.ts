export enum Opt {
    EQ = "=",
    NE = "!=",
    GT = ">",
    GE = ">=",
    LT = "<",
    LE = "<=",
    IN = "in",
    NIN = "nin",
    LIKE = "like",
    NLIKE = "nlike",
    NULL = "null",
    NNULL = "nnull"
}

export enum Rel {
    AND = "and",
    OR = "or"
}

export class Cond {
    col: String;
    opt: Opt;
    val: any;

    constructor(col: String, opt: Opt, val: any) {
        this.col = col;
        this.opt = opt;
        this.val = val;
    }

    setOpt(opt: Opt) {
        this.opt = opt;
        return this;
    }

    setVal(val: any) {
        this.val = val;
        return this;
    }
}

export class Order {
    col: String;
    asc: boolean;

    constructor(col: String, asc: boolean) {
        this.col = col;
        this.asc = asc;
    }

    setAsc(asc: boolean) {
        this.asc = asc;
        return this;
    }
}

export class Query {
    cols: String[] = [];
    conds: Cond[] = [];
    distinct: boolean = false;
    orders: Order[] = [];

    constructor() {
    }

    setCols(cols: String[]): Query {
        this.cols = cols;
        return this;
    }

    addCond(col: String, opt: Opt, val: any) {
        this.conds.push(new Cond(col, opt, val));
        return this;
    }

    removeCond(col: String) {
        this.conds = this.conds.filter(cond => cond.col != col);
        return this;
    }

    setConds(conds: Cond[]): Query {
        this.conds = conds;
        return this;
    }

    getCond(col: String) {
        return this.conds.find(cond => cond.col == col);
    }

    addOrder(col: String, asc: boolean) {
        this.orders.push(new Order(col, asc));
        return this;
    }

    removeOrder(col: String) {
        this.orders = this.orders.filter(order => order.col != col);
        return this;
    }

    getOrder(col: String) {
        return this.orders.find(order => order.col == col);
    }

    setOrders(orders: Order[]): Query {
        this.orders = orders;
        return this;
    }
}

export class PageQuery extends Query {
    current: number = 1;
    size: number = 20;

    constructor(current: number = 1, size: number = 20) {
        super();
        this.current = current;
        this.size = size;
    }

    /**
     * 设置每页大小
     * @param size
     */
    setSize(size: number): PageQuery {
        this.size = size;
        return this;
    }
}