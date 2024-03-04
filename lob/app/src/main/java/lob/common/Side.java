package lob.common;

public enum Side {
    BUY,
    SELL;

    public Side inverse() {
        switch (this) {
            case BUY:
                return SELL;
            case SELL:
                return BUY;
            default:
                return null;
        }
    }
}