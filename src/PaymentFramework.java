abstract class PaymentFramework {
    private String name;
    private String transactionID;
    private double amount;
    private boolean hasValidPaymentMethod;
    private double creditBalance;
    private double discountRate;
    protected double vatRate = 0.12;

    protected boolean transactionSuccess = false;
    protected double finalAmount = 0;

    public PaymentFramework(String name, String transactionID, double amount,
            boolean hasValidPaymentMethod, double creditBalance, double discountRate) {
        this.name = name;
        this.transactionID = transactionID;
        this.amount = amount;
        this.hasValidPaymentMethod = hasValidPaymentMethod;
        this.creditBalance = creditBalance;
        this.discountRate = discountRate;
    }

    protected boolean validatePayment() {
        double total = applyDiscount(applyVAT(amount));
        return hasValidPaymentMethod && creditBalance >= total;
    }

    protected double applyVAT(double amount) {
        return amount * (1 + vatRate);
    }

    protected double applyDiscount(double amount) {
        return amount - (amount * discountRate);
    }

    protected void finalizeTransaction(double finalAmount) {
        creditBalance -= finalAmount;
        this.finalAmount = finalAmount;
        this.transactionSuccess = true;

        System.out.println("Transaction ID: " + transactionID);
        System.out.println("Customer Name: " + name);
        System.out.println("Transaction Successful!");
        System.out.println("Final Amount Paid: " + finalAmount);
        System.out.println("Remaining Balance: " + creditBalance);
    }

    public boolean processInvoice() {
        if (!validatePayment()) {
            transactionSuccess = false;
            System.out.println("Transaction ID: " + transactionID);
            System.out.println("Payment Failed: Invalid method or insufficient balance.");
            return false;
        }

        double total = applyVAT(amount);
        total = applyDiscount(total);
        finalizeTransaction(total);
        return true;
    }

    public boolean isTransactionSuccessful() {
        return transactionSuccess;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public double getAmount() {
        return amount;
    }

    public double getDiscountRate() {
        return discountRate;
    }
}
