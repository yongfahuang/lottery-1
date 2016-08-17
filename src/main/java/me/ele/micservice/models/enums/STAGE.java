package me.ele.micservice.models.enums;

/**
 * 抽奖阶段（不同阶段的奖品不一样）
 */
public enum STAGE{
    /**
     * 第一阶段是4000人次
     */
    ONE(1),
    /**
     * 第二阶段是3500人次后
     */
    TWO(2);

    private int val;
    STAGE(int num) {
        this.val = num;
    }

    public int getVal() {
        return val;
    }

}

