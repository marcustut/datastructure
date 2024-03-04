package lob.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import lob.LimitOrderBook;
import lob.common.Order;
import lob.common.Side;
import lob.ds.Tree.TraversalOrder;

public class LOB implements LimitOrderBook {
    // The tree for storing buy limit levels.
    private LimitTree buy = new LimitTree(Side.BUY);

    // The tree for storing sell limit levels.
    private LimitTree sell = new LimitTree(Side.SELL);

    // Store the orders according to their id.
    private HashMap<Long, Order> orders = new HashMap<>();

    @Override
    public void limit(Order order) {
        getTree(order.side).limit(order);
        orders.put(order.id, order); // add the order onto the map
    }

    @Override
    public void market(Order order) {
        ArrayList<Long> executedOrders = getTree(order.side.inverse()).market(order);

        // remove executed orders
        for (long executedOrder : executedOrders)
            orders.remove(executedOrder);
    }

    @Override
    public void cancel(long orderId) {
        Order order = orders.remove(orderId); // removes the order from the map

        if (order != null)
            getTree(order.side).cancel(order); // cancel the order from the tree
    }

    @Override
    public void amend(long orderId, long size) {
        Order order = orders.get(orderId); // get the order from the map
        if (order == null) {
            System.err.println("amending a non-existent order " + orderId);
            return;
        }

        getTree(order.side).amend(order, size);
    }

    @Override
    public long best_buy() {
        if (buy.best == null)
            return 0;
        return buy.best.price;
    }

    @Override
    public long best_sell() {
        if (sell.best == null)
            return 0;
        return sell.best.price;
    }

    @Override
    public long volume() {
        return buy.volume + sell.volume;
    }

    private LimitTree getTree(Side side) {
        switch (side) {
            case BUY:
                return buy;
            case SELL:
                return sell;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<Limit> it = sell.limits.traverse(TraversalOrder.PostOrderTraversal);
        while (it.hasNext()) {
            Limit limit = it.next();
            sb.append(limit.price + " (" + limit.volume + ")\n");
        }

        sb.append("----------------------------------------------------\n");

        it = buy.limits.traverse(TraversalOrder.PostOrderTraversal);
        while (it.hasNext()) {
            Limit limit = it.next();
            sb.append(limit.price + " (" + limit.volume + ")\n");
        }

        return sb.toString();
    }
}
