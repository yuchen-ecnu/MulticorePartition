package embs;

public enum DependencyRelation {
    PREDECESSOR(-1),//前驱
    SUCCESSOR(1),//后继
    NONE(0) //无依赖关系
    ;

    private int v = 0;

    DependencyRelation(int v) {
        this.v = v;
    }

    public int getValue() {
        return this.v;
    }
}
