# Implementation Plan

## Current Scope

This update focuses on:

1. Add Facebook Login via Firebase Authentication.
2. Support Firebase token synchronization on Spring Boot backend.
3. Simplify finance flow to use only one default wallet: **"Ví chính"**.
4. Keep budgets and reports.
5. Put OCR Scan Bill and YOLO Product Scan on hold.
6. Do not change database schema.

---

## Do Not Touch

- Do not delete database tables.
- Do not modify `database/schema.sql`.
- Do not remove existing OCR/YOLO related files.
- Do not remove existing Transfer backend files.
- Do not migrate Java to Kotlin.
- Do not add Jetpack Compose.
- Do not replace Firebase Auth.
- Do not replace Spring Boot.

---

## Default Wallet Rule

The app must behave as a one-wallet app, but the database still keeps the `accounts` table.

For every user:

- There must be one default account named **"Ví chính"**.
- Backend must create it automatically if it does not exist.
- Android should not let users choose account when creating transactions.
- Backend must assign the default `account_id` automatically.

---

## AI On Hold Rule

OCR and YOLO are not part of this implementation phase.

Keep existing files:

- `ScanBillActivity.java`
- `ScanProductActivity.java`
- `AiScanController.java`
- `AiProductController.java`
- `AiScanLog.java`
- `AiProductLog.java`

Only hide entry points from UI or show **"Coming soon"**.

---

## Phase 1: Android Authentication

Implement login methods:

- Email/Password
- Google Login
- Facebook Login via Firebase Auth

Rules:

- Android uses Firebase Auth for all login methods.
- After login, Android retrieves Firebase ID Token.
- `TokenInterceptor` attaches Firebase ID Token to all backend requests.
- Do not create custom JWT.
- Do not migrate Java to Kotlin.

Login UI:

- Keep Email login.
- Keep Google login.
- Add Facebook login button.
- Style Facebook button consistently with current UI.

---

## Phase 2: Backend Firebase Sync

Backend verifies Firebase ID Token using Firebase Admin SDK.

When user logs in:

- Decode Firebase token.
- Get `firebase_uid`.
- Get `email`.
- Get `display name`.
- Get `avatar URL` if available.
- Determine `auth_provider` from Firebase provider data.
- Create or update user in MySQL.

Important:

- Do not trust `auth_provider` from Android request body.
- Backend must decide `auth_provider`.

After creating a new user:

Automatically create default account:

```txt
account_name = "Ví chính"
account_type = "cash"
balance = 0
currency = "VND"
```

Required backend function:

```java
Account getOrCreateDefaultAccount(Integer userId);
```

---

## Phase 3: One Wallet Finance Flow

The app uses only one wallet.

Rules:

* Do not delete `accounts` table.
* Do not delete `transfer_groups` table.
* Do not modify `schema.sql`.
* Hide multi-wallet management from Android UI.
* Hide transfer feature from Android UI.
* Do not show account picker when adding transaction.

Transaction creation flow:

1. Android sends transaction data without account selection.
2. Backend gets current user from Firebase token.
3. Backend calls `getOrCreateDefaultAccount(userId)`.
4. Backend assigns `account_id` automatically.
5. Backend saves transaction.
6. Backend updates balance of **"Ví chính"**.

UI changes:

* Profile screen only shows **"Ví chính"** balance.
* AddTransaction bottom sheet does not show wallet selector.
* Home calendar shows daily income/expense based on the default wallet.
* Do not remove existing backend transfer code unless explicitly requested.
* Just stop exposing transfer actions in Android UI.

---

## Phase 4: Budgets and Reports

Keep budget feature:

* Budget applies to categories.
* Budget works for the only default wallet.
* Show warning at 80%.
* Show warning at 100%.

Keep report feature:

* Pie chart by category.
* Bar chart income vs expense.
* Calendar daily summary.
* All reports use transactions from the default wallet.

---

## Phase 5: OCR Scan Bill

Status: **On Hold**

Rules:

* Do not implement OCR in this phase.
* Do not delete `ScanBillActivity`.
* Do not delete `AiScanController`.
* Hide OCR entry point or mark as **"Coming soon"**.

---

## Phase 6: YOLO Product Scan

Status: **On Hold**

Rules:

* Do not implement YOLO in this phase.
* Do not delete `ScanProductActivity`.
* Do not delete `AiProductController`.
* Hide product scan entry point or mark as **"Coming soon"**.

---

## Verification

### Automated

Run Android build:

```bash
cd PersonalFinanceApp
./gradlew assembleDebug
```

Run backend:

```bash
cd finance-backend
mvn spring-boot:run
```

---

### Manual

Test login:

* Email login
* Google login
* Facebook login

Verify MySQL:

* `users` table has correct `firebase_uid`.
* `users.auth_provider` is correct.
* `accounts` table has exactly one default account per user.

Test transaction:

* Add income transaction.
* Add expense transaction.
* Verify `transaction.account_id` is default account.
* Verify balance changes correctly.

Test UI:

* No wallet picker.
* No transfer button.
* Profile shows only **"Ví chính"**.
* Reports and budgets still work.
