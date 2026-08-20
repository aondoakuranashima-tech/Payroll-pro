import { PaymentProvider } from '../../../billing-api/payment-provider.mjs';
export class FlutterwaveAdapter extends PaymentProvider {
  constructor(client) { super('flutterwave'); this.client = client; this.priority = 20; }
  createCheckout(input) { return this.client.createCheckout(input); }
  verifyPayment(input) { return this.client.verifyPayment(input); }
  refundPayment(input) { return this.client.refundPayment(input); }
  getTransaction(input) { return this.client.getTransaction(input); }
  handleWebhook(input) { return this.client.handleWebhook(input); }
  getPaymentMethods() { return ['card','bank_transfer','bank','ussd','mobile_money','wallet','qr']; }
}
