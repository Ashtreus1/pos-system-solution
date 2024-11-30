package objects;

public class QueueingOrder {
    private int queueingNo;
    private String code;
    private String name;
    private int qty;
    private double price;
    private boolean status;

    public QueueingOrder(int queueingNo, String code, String name, int qty, double price, boolean status) {
        this.queueingNo = queueingNo;
        this.code = code;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.status = status;
    }

    public int getQueueingNo() { return queueingNo; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getQty() { return qty; }
    public double getPrice() { return price; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status){ this. status = status; }
}
