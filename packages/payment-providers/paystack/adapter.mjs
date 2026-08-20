import { PaymentProvider } from '../../../billing-api/payment-provider.mjs';
export class PaystackAdapter extends PaymentProvider {
  constructor(client) { super('paystack'); this.client = client; this.priority = 10; }
  createCheckout(input) { return this.client.createCheckout(input); }
  verifyPayment(input) { return this.client.verifyPayment(input); }
  refundPayment(input) { return this.client.refundPayment(input); }
  getTransaction(input) { return this.client.getTransaction(input); }
  handleWebhook(input) { return this.client.handleWebhook(input); }
  getPaymentMethods() { return ['card','bank_transfer','bank','ussd','mobile_money','qr']; }
}
