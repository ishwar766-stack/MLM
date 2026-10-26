# Shop & Earn - E-commerce + Referral (Unilevel) Android app

Kotlin + Jetpack Compose. Works as a fully offline **demo**: shop, cart/coupon/checkout (mock payment),
order tracking, unilevel commission engine (5 levels), genealogy tree, ranks, leaderboard,
e-wallet with TDS, KYC form validation, and share-invite.

## Build the APK with GitHub (no Android Studio needed)
1. Create a new GitHub repository and upload everything in this folder (keep the `.github` folder).
2. Open the **Actions** tab -> **Android CI** (it runs on every push; or click *Run workflow*).
3. When the run is green, open it and download **shop-and-earn-debug-apk** under *Artifacts*.
4. Unzip, copy `app-debug.apk` to your phone and install (allow "install unknown apps").

Demo login: any 10-digit number, OTP `1234`.

## Try the flow
Profile -> Become a Distributor -> Team -> "Simulate a downline purchase" (commissions land in Wallet)
-> Profile -> KYC (e.g. PAN `ABCDE1234F`, Aadhaar 12 digits, account 9-18 digits, IFSC `SBIN0001234`) -> Wallet -> Withdraw.

## Next steps for production
- Replace `data/Store.kt` with a Retrofit/Ktor client talking to your Node.js/FastAPI + PostgreSQL backend
  (commission posting, ledger, order and return reversal must be done server-side in DB transactions).
- Real payment gateway, KYC provider, GST invoicing, biometric/MFA for withdrawals, push notifications.
- Release signing via GitHub Secrets, then `gradle bundleRelease` for Play Store.
- Legal review: India's Consumer Protection (Direct Selling) Rules 2021 and the Prize Chits and Money
  Circulation Schemes (Banning) Act - commissions must stay tied to genuine product sales.
