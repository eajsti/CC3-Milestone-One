class ReservationPayment extends PaymentFramework {
    public ReservationPayment(String name, String transactionID, double amount,
            boolean hasValidPaymentMethod, double creditBalance, double discountRate) {
        super(name, transactionID, amount, hasValidPaymentMethod, creditBalance, discountRate);
    }

    public boolean execute() {
        return processInvoice();
    }
}
