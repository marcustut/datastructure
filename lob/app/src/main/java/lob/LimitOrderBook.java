package lob;

import lob.common.Order;

public interface LimitOrderBook {
    public void limit(Order order);

    public void market(Order order);

    public void cancel(long orderId);

    public void amend(long orderId, long size);

    public long best_buy();

    public long best_sell();

    public long volume();
}