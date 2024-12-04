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
}

export class Order {
    col: String;
    asc: boolean;

    constructor(col: String, asc: boolean) {
        this.col = col;
        this.asc = asc;
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

    setConds(conds: Cond[]): Query {
        this.conds = conds;
        return this;
    }

    addOrder(col: String, asc: boolean) {
        this.orders.push(new Order(col, asc));
        return this;
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

    setSize(size: number): PageQuery {
        this.size = size;
        return this;
    }
}