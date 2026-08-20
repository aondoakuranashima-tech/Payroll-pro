import { PROVIDER_NAMES } from './payment-provider.mjs';

export class PaymentRouter {
  constructor(providers = {}) { this.providers = providers; }

  async route(input) {
    const candidates = [];
    for (const name of PROVIDER_NAMES) {
      const provider = this.providers[name];
      if (!provider) continue;
      const health = typeof provider.health === 'function' ? await provider.health() : { healthy: true };
      if (!health.healthy) continue;
      const methods = await provider.getPaymentMethods(input.country, input.currency);
      if (input.paymentMethod && !methods.includes(input.paymentMethod)) continue;
      const capability = typeof provider.canProcess === 'function'
        ? await provider.canProcess(input)
        : true;
      if (!capability) continue;
      candidates.push({
        provider,
        cost: typeof provider.transactionCost === 'function' ? await provider.transactionCost(input) : Number.MAX_SAFE_INTEGER,
        priority: provider.priority ?? 100,
      });
    }
    candidates.sort((a, b) => a.priority - b.priority || a.cost - b.cost);
    if (!candidates.length) throw new Error('No healthy payment provider supports this transaction');
    return candidates[0].provider;
  }
}
