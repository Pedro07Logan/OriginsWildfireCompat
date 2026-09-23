# Changelog — Fabric variants

## fabric-1.21.1-1.0.0 (`originswildfirecompat-fabric-1.21.1-1.0.0.jar`)

- Origins powers rewritten for the Apoli 1.13 schema (`id` + `amount`, 1.13 modifier operations).
- Masked `icon` format fixed (`id` field) for both origins.
- `male_haste` re-mapped to `multiply_base_multiplicative` (matches vanilla `more_kinetic_damage`).
- **Known issue:** crashes (`Ticking player` NPE) when the `async` mod (`async-fabric`) is installed.
  See `README.md` — root cause is an Async × Apoli incompatibility, not this mod. Workaround: remove Async.
