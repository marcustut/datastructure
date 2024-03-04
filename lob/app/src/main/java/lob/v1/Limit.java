package lob.v1;

import java.util.LinkedList;
import java.util.Iterator;

import lob.common.Order;

/**
 * Limit represents one price level in the orderbook.
 */
public class Limit implements Comparable<Limit> {
    // The price for this limit level.
    public long price;

    // The number of orders at this limit level
    public int count = 0;

    // The total volume at this limit level
    public long volume = 0;

    // A queue of orders at this limit level
    public LinkedList<Order> orders = new LinkedList<>();

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
     * @return whether the removal succeeded or not.
     */
    public boolean remove(Order order) {
        // NOTE: this is O(n) but can be optimised to O(1) if we store the pointers on
        // `Order` and avoid `LinkedList`.
        if (orders.remove(order)) { // only deduct count if the order indeed exists
            --count;
            volume -= order.size;
            return true;
        }

        return false;
    }

    /**
     * Update an order in the list.
     * 
     * @param orderId - The order to be updated.
     * @param size    - The new size for the order.
     * @return the change in volume after the update.
     */
    public long update(long orderId, long size) {
        Iterator<Order> iter = orders.iterator();
        Order order;

        while (iter.hasNext()) {
            order = iter.next();
            if (order.id == orderId) {
                long delta = size - order.size;
                volume += delta; // update the volume
                order.size = size;
                return delta;
            }
        }

        return 0;
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

    @Override
    public String toString() {
        return price + " (" + volume + ")";
    }
}