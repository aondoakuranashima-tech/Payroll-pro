export class PaymentProvider {
  constructor(name, capabilities = {}) {
    this.name = name;
    this.capabilities = capabilities;
  }

  async createCheckout() { throw new Error(`${this.name}: createCheckout not implemented`); }
  async authorizePayment() { return { status: 'unsupported' }; }
  async capturePayment() { return { status: 'unsupported' }; }
  async verifyPayment() { throw new Error(`${this.name}: verifyPayment not implemented`); }
  async refundPayment() { throw new Error(`${this.name}: refundPayment not implemented`); }
  async createSubscription() { return { status: 'unsupported' }; }
  async cancelSubscription() { return { status: 'unsupported' }; }
  async getPaymentMethods() { return this.capabilities.paymentMethods ?? []; }
  async getTransaction() { throw new Error(`${this.name}: getTransaction not implemented`); }
  async handleWebhook() { throw new Error(`${this.name}: handleWebhook not implemented`); }
}

export const PROVIDER_NAMES = Object.freeze(['paystack', 'flutterwave', 'dodo', 'paddle', 'paypal']);
