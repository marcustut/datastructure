package lob.v1;

import java.util.ArrayList;
import java.util.HashMap;

import lob.common.Order;
import lob.common.Side;
import lob.ds.BST;

/**
 * Represents a tree of price levels (limits) stored in a binary search tree.
 */
public class LimitTree {
    // The underlying binary search tree that stores the limits.
    BST<Limit> limits = new BST<>();

    // The price where the last order is executed.
    long lastPrice = 0;

    // The total number of active orders in this tree across all limits.
    int count = 0;

    // The total volume aggregated from all orders in this tree across all limits.
    long volume = 0;

    // The current top price limit.
    Limit best;

    // Indicate whether this is a buy tree or a sell tree.
    Side side;

    // A hash map for fast access to limit using their price
    HashMap<Long, Limit> limitMap = new HashMap<>();

    public LimitTree(Side side) {
        this.side = side;
    }

    /**
     * Clear all the limits in the tree.
     */
    void clear() {
        // Create a new clean tree (the old one will be garbage collected)
        limits = new BST<>();
        count = 0;
        volume = 0;
    }

    /**
     * Place a limit order onto the tree. This action will create a new limit if
     * there isn't any existing orders at that limit.
     * 
     * @param order - The order to be placed.
     */
    public void limit(Order order) {
        Limit currentLimit = limitMap.get(order.price); // get the current limit
        // Limit currentLimit = limits.search(new Limit(order)); // get the current
        // limit

        if (currentLimit != null) // if the tree contains the price limit
            currentLimit.add(order); // add the order onto the existing limit
        else { // if the tree does not contain the price limit
            Limit limit = new Limit(order); // create a new limit
            updateBest(limit); // update the best price limit
            limits.add(limit); // insert the limit onto the tree
            limitMap.put(order.price, limit); // insert the limit onto the map
        }

        ++count; // update the active orders count
        volume += order.size; // update the total volume
    }

    /**
     * Execute a market order by matching orders from the tree.
     * 
     * @param order - The order to execute
     * @return a list of order ids that was fully filled.
     */
    public ArrayList<Long> market(Order order) {
        ArrayList<Long> executedOrders = new ArrayList<>();

        while (best != null && order.size > 0) { // keep executing until no more levels or the order is fully filled
            if (best.orders.size() == 0) { // find the next best
                updateBest();
                if (best == null || best.orders.size() == 0) // no more liquidity
                    return executedOrders;
            }

            Order matchedOrder = best.orders.getFirst(); // get the first

            long fillSize = Math.min(order.size, matchedOrder.size); // we can only at most fill the order size

            // fill the order
            matchedOrder.size -= fillSize;
            order.size -= fillSize;

            if (matchedOrder.size == 0 && best.count == 1) { // order is fully filled and the limit has no orders
                limits.remove(best); // remove the limit
                limitMap.remove(best.price); // remove the limit
                updateBest(); // look for next best limit
                count--; // update the total count
                volume -= fillSize; // deduct filled size from total volume
                executedOrders.add(matchedOrder.id); // add the filled order id
                continue;
            }

            if (matchedOrder.size == 0) { // order is fully filled but limit has orders left
                best.remove(matchedOrder); // remove the filled order from limit
                executedOrders.add(matchedOrder.id); // add the filled order id
            } else { // order is partially filled
            }

            best.volume -= fillSize; // update the limit volume
            best.count--; // update the limit count
            count--; // update the total count
            volume -= fillSize; // deduct the filled size from total volume
            lastPrice = best.price; // update the last executed price
        }

        return executedOrders;
    }

    /**
     * Removes an order from the tree.
     * 
     * @param order - The order to cancel
     */
    public void cancel(Order order) {
        Limit currentLimit = limitMap.get(order.price); // get the current limit
        // Limit currentLimit = limits.search(new Limit(order)); // get the current
        // limit
        if (currentLimit == null) {
            // System.err.println(
            // "Failed to cancel order " + order.id + ": No orders in the level " +
            // order.price);
            return;
        }

        if (currentLimit.count > 1) // if the limit has more than one order
            currentLimit.remove(order);
        else { // if the limit only has 1 order
            limits.remove(currentLimit); // remove the limit from tree
            limitMap.remove(currentLimit.price); // remove the limit from map

            // update best limit if necessary
            if (currentLimit == best)
                updateBest();
        }

        --count; // update the active orders count
        volume -= order.size; // update the total volume
    }

    /**
     * Amend an order from the tree.
     * 
     * @param order - The order to amend.
     * @param size  - The new size for the order.
     */
    public void amend(Order order, long size) {
        Limit limit = limitMap.get(order.price); // find the limit where the order resides
        // Limit limit = limits.search(new Limit(order)); // find the limit where the
        // order resides
        if (limit == null) {
            // System.err.println(
            // "Failed to amend order " + order.id + ": No orders in the level " +
            // order.price);
            return;
        }

        volume += limit.update(order.id, size);
    }

    /**
     * Update the current top price limit of the tree.
     * 
     * @param limit - The limit to check
     */
    private void updateBest(Limit limit) {
        if (best == null) {
            best = limit;
            return;
        }

        switch (side) {
            case BUY:
                if (limit.price > best.price)
                    best = limit;
                return;
            case SELL:
                if (limit.price < best.price)
                    best = limit;
                return;
        }
    }

    /**
     * Search and update the current top price limit of the tree.
     * 
     * @return
     */
    private void updateBest() {
        if (best == null)
            return;

        if (limits.size() == 0) { // if limits was emptied, then there is no best limit
            best = null;
            return;
        }

        if (best.orders.size() == 0) { // if no more orders in the best limit then it's cleared
            best = null;
            return;
        }

        switch (side) {
            case BUY:
                best = limits.max();
                return;
            case SELL:
                best = limits.min();
                return;
        }
    }
}
