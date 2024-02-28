package lob;

import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.util.concurrent.UncheckedExecutionException;

import lob.ds.BST;

public class LOB {
    public enum Side {
        BUY,
        SELL
    }

    /**
     * Order represents an individual order placed on a particular price level
     * (limit) of the orderbook.
     */
    public class Order {
        // The unique identifier for the order.
        int id = 0;

        // The side of the order, either buy or sell only.
        Side side;

        // The quantity of the order.
        int size = 0;

        // The price of the order.
        int price = 0;

        public Order(int id, Side side, int size, int price) {
            this.id = id;
            this.side = side;
            this.size = size;
            this.price = price;
        }

        @Override
        public int hashCode() {
            return id;
        }

        /**
         * Override the default `Object.equals` method because `Order` is equivalent if
         * the ID is the same.
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof Order) {
                Order order = (Order) o;
                return id == order.id;
            }
            return false;
        }
    }

    /**
     * Limit represents one price level in the orderbook.
     */
    public class Limit implements Comparable<Limit> {
        // The price for this limit level.
        private int price;

        // The number of orders at this limit level
        private int count = 0;

        // The total volume at this limit level
        private int volume = 0;

        // A queue of orders at this limit level
        LinkedList<Order> orders = new LinkedList<>();

        /**
         * Creates a new limit with an initial order. A limit does not exist if it does
         * not have any orders hence to create a new limit it must has at least one
         * initial order.
         * 
         * @param order - The first order in the limit.
         */
        public Limit(Order order) {
            this.price = order.price;
            this.count = 1;
            this.volume = order.size;
            this.orders.add(order);
        }

        /**
         * Insert a new order onto the limit level.
         * 
         * @param order - The order to be inserted.
         */
        public void add(Order order) {
            orders.add(order);
            ++count;
            volume += order.size;
        }

        /**
         * Remove an existing order from the limit level.
         * 
         * @param order - The order to be removed.
         */
        public void remove(Order order) {
            orders.remove(order); // NOTE: this is O(n) but can be optimised to O(1) if we store the pointers on
                                  // `Order` and avoid `LinkedList`.
            --count;
            volume -= order.size;
        }

        /**
         * Implements the `Comparable` interface so that `BST` is able to keep the limit
         * levels sorted in the tree.
         */
        @Override
        public int compareTo(Limit o) {
            if (price == o.price)
                return 0;
            else if (price > o.price)
                return 1;
            else
                return -1;
        }
    }

    /**
     * Represents a tree of price levels (limits) stored in a binary search tree.
     */
    public class LimitTree {
        // The underlying binary search tree that stores the limits.
        private BST<Limit> limits = new BST<>();

        // The last best price
        private int lastBestPrice = 0;

        // The total number of active orders in this tree across all limits.
        private int count = 0;

        // The total volume aggregated from all orders in this tree across all limits.
        private int volume = 0;

        // The current top price limit.
        private Limit best;

        // Indicate whether this is a buy tree or a sell tree.
        private Side side;

        public LimitTree(Side side) {
            this.side = side;
        }

        /**
         * Clear all the limits in the tree.
         */
        void clear() {
            // Create a new clean tree (the old one will be garbage collected)
            limits = new BST<>();
            lastBestPrice = 0;
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
            Limit currentLimit = limits.search(new Limit(order)); // get the current limit

            if (currentLimit != null) // if the tree contains the price limit
                currentLimit.add(order); // add the order onto the existing limit
            else { // if the tree does not contain the price limit
                Limit limit = new Limit(order); // create a new limit
                updateBest(limit); // update the best price limit
                limits.add(limit); // insert the limit onto the tree
            }

            ++count; // update the active orders count
            volume += order.size; // update the total volume

            // update the last best price
            if (best != null)
                lastBestPrice = best.price;
        }

        /**
         * Execute a market order by matching orders from the tree.
         * 
         * @param order - The order to be executed.
         */
        public void market(Order order) {
            throw new UnsupportedOperationException("unimplemented");
        }

        /**
         * Removes an order from the tree.
         * 
         * @param order - The order to cancel
         */
        public void cancel(Order order) {
            Limit currentLimit = limits.search(new Limit(order)); // get the current limit
            if (currentLimit == null) {
                System.err.println(
                        "Failed to cancel order " + order.id + ": No orders in the level " + order.price);
                return;
            }

            if (currentLimit.count > 1) // if the limit has more than one order
                currentLimit.remove(order);
            else { // if the limit only has 1 order
                limits.remove(currentLimit); // remove the limit from tree

                // update best limit if necessary
                if (currentLimit == best)
                    updateBest();
            }

            --count; // update the active orders count
            volume -= order.size; // update the total volume

            // update the last best price
            if (best != null)
                lastBestPrice = best.price;
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

    // The tree for storing buy limit levels.
    private LimitTree buy = new LimitTree(Side.BUY);

    // The tree for storing sell limit levels.
    private LimitTree sell = new LimitTree(Side.SELL);

    // Store the orders according to their id.
    private HashMap<Integer, Order> orders = new HashMap<>();

    public void limit(Order order) {
        getTree(order.side).limit(order);
        orders.put(order.id, order); // add the order onto the map
    }

    public void cancel(int orderId) {
        Order order = orders.remove(orderId); // removes the order from the map
        getTree(order.side).cancel(order); // cancel the order from the tree
    }

    public int best_buy() {
        if (buy.best == null)
            return 0;
        return buy.best.price;
    }

    public int best_sell() {
        if (sell.best == null)
            return 0;
        return sell.best.price;
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
}
