package lob.common;

/**
 * Order represents an individual order placed on a particular price level
 * (limit) of the orderbook.
 */
public class Order {
    // The unique identifier for the order.
    public long id = 0;

    // The side of the order, either buy or sell only.
    public Side side;

    // The quantity of the order.
    public long size = 0;

    // The price of the order.
    public long price = 0;

    public Order(long id, Side side, long size, long price) {
        this.id = id;
        this.side = side;
        this.size = size;
        this.price = price;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
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