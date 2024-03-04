package lob;

import java.util.Iterator;

import lob.common.Order;
import lob.common.Side;
import lob.v1.Limit;

public interface LimitOrderBook {
    public void limit(Order order);

    public void market(Order order);

    public void cancel(long orderId);

    public void amend(long orderId, long size);

    public long bestBuy();

    public long bestSell();

    public long volume();

    public Iterator<Limit> topN(int n, Side side);
}