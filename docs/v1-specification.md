# SpendSnap — Đặc tả phiên bản 1

> Tài liệu mô tả toàn bộ tính năng, API contract và mô hình dữ liệu của SpendSnap version 1 (chưa publish).
> Phạm vi: Mobile app (Android, Kotlin/Compose) + Backend (NestJS + MongoDB).

---

## 1. Tổng quan sản phẩm

SpendSnap là ứng dụng quản lý chi tiêu cá nhân, người dùng có thể:
- Chụp ảnh hoá đơn / nhập số tiền nhanh để ghi giao dịch.
- Phân loại giao dịch theo danh mục (expense / income) với icon emoji.
- Theo dõi tổng chi, ngân sách còn lại, breakdown theo danh mục trong tháng.
- Xem lịch sử giao dịch theo dạng grid ảnh.

---

## 2. Kiến trúc

### 2.1 Backend
- **Framework**: NestJS (TypeScript)
- **Database**: MongoDB (Mongoose ODM)
- **Auth**: JWT (`@nestjs/jwt`)
- **Storage**: Cloudinary (upload ảnh hoá đơn, avatar)
- **API docs**: Swagger (`@nestjs/swagger`)
- **Response wrapper** (`TransformInterceptor`): mọi response thành công đều có shape:
  ```json
  { "data": <T>, "message": "Success", "statusCode": 200 }
  ```

Modules:
| Module       | Endpoints chính                                                            |
|--------------|----------------------------------------------------------------------------|
| auth         | POST /auth/signup, POST /auth/signin                                       |
| user         | GET /users/profile, POST /users/updateAvatar, POST /users/create           |
| category     | GET /category, GET /category/icons, GET /category/types, POST /category/create, PATCH /category/:id, DELETE /category/:id |
| budget       | PUT /budget, GET /budget, DELETE /budget/:id                               |
| transactions | GET /transactions, POST /transactions/create                               |
| statistics   | GET /statistics/overview                                                   |
| cloudinary   | (internal service — không expose endpoint)                                 |

### 2.2 Mobile (Android)
- **Min SDK**: 24, **Target SDK**: 36
- **UI**: Jetpack Compose, Material 3
- **DI**: Hilt
- **Network**: Retrofit + OkHttp + kotlinx.serialization
- **Local storage**: Room (cache categories, icons, user) + DataStore (token, language)
- **Camera**: CameraX
- **Image loading**: Coil
- **Pattern**: Client → Service (`safeApiCall`) → Repository (memory + Room cache) → ViewModel (StateFlow) → Composable

Cấu trúc thư mục chính:
```
app/src/main/java/com/spendsnap/app/
├── data/
│   ├── AppDatabase.kt
│   ├── local/ (dao, entities, AuthManager)
│   └── remote/ (clients, models, services, repositories)
├── di/ (NetworkModule, DatabaseModule, RepositoryModule)
├── ui/
│   ├── auth/ (LoginScreen, SignupScreen)
│   ├── camera/ (CameraScreen)
│   ├── categories/ (CategoriesScreen, AddNewCategoryScreen)
│   ├── components/ (LoadingDialog, AppStatusDialog, HeaderSection,…)
│   ├── history/ (HistoryScreen, TransactionDetailScreen)
│   ├── home/ (HomeScreen — Statistics overview)
│   ├── profile/ (ProfileScreen)
│   ├── settings/ (SettingsLanguageScreen)
│   └── Screen.kt (route sealed class)
├── view_models/
└── shared/ (Utils, helpers)
```

### 2.3 Luồng auth
1. Sau signin thành công, BE trả `{ accessToken, user }`.
2. FE lưu `accessToken` vào DataStore (`AuthManager`).
3. OkHttp `authInterceptor` tự gắn `Authorization: Bearer <token>` vào mọi request.
4. Khi signin: FE prefetch `getCategoryIcons()` xuống Room để các screen sau dùng emoji ngay.

---

## 3. Tính năng theo màn hình

### 3.1 Authentication
- **LoginScreen**: email + password (SHA-256 + Base64 encode trước khi gửi). Hiển thị `AppStatusDialog(type = Error)` khi sai credentials.
- **SignupScreen**: name + email + password + confirm password. Sau khi signup BE tự seed 8 default categories cho user.

### 3.2 Home (Statistics)
- Gọi `GET /statistics/overview` (mặc định tháng hiện tại).
- Hiển thị:
  - **TotalSpentCard**: `totalSpent` + pill `spentChangePercent` (mũi tên lên đỏ / xuống primary).
  - **DonutChartSection**: vẽ động từ `breakdown[].percentage`, màu lấy từ `breakdown[].color` (hex) hoặc fallback palette.
  - **RemainingBudgetCard**: `remainingBudget`, `safeToSpendPerDay`; đổi sang state cảnh báo đỏ + label "OVER BUDGET" khi remaining < 0.
  - Câu insight: từ `insights.topImprovement` ("You're spending less on X. That's $Y saved this month.").
  - **BreakdownItem**(s): emoji (lookup từ slug → emoji), tên, percentage, trend `changePercent` (đỏ khi >= 0, primary khi < 0).

### 3.3 Camera / Tạo giao dịch
- **CameraScreen**: quyền camera, preview, chụp ảnh.
- Sau khi chụp → `CapturePreview`:
  - Tabs EXPENSE / INCOME (pill).
  - Horizontal scroll category cards (emoji + name) — auto filter theo tab, auto-select item đầu.
  - Numeric keypad nhập số tiền (chặn keyboard hệ thống).
  - RETAKE / CONFIRM.
- Gọi `POST /transactions/create` multipart: `amount` (string → number), `categoryId` (string), `image` (optional binary).
- Success → `AppStatusDialog(type = Success)`, clear ảnh + amount.
- Lỗi → `AppStatusDialog(type = Error)`.

### 3.4 History
- Gọi `GET /transactions` trả `{ transactions, totalSpent }`.
- **MoneyLeftCard**: hiển thị `totalSpent` (BE compute, chỉ tính các category `kind = expense`).
- **MomentsGrid**: grid ảnh layout pattern 2-1-2 lặp lại mỗi 5 item.
- **MomentItem**: ảnh + gradient overlay; text overlay từ trên xuống: amount → (emoji + category name) → relative date.
- Relative date format (`Utils.formatRelativeDate`):
  - `< 1 giờ` → `"Xm ago"`
  - `< 1 ngày` → `"Xh ago"`
  - `< 1 tuần` → `"X days ago"`
  - else → `"dd/MM/yyyy"`

### 3.5 Categories
- **CategoriesScreen**: list categories từ `GET /category` (cached qua memory + Room).
- **MostUsedCategoryCard**: ưu tiên category có `isMostUsed = true` (BE tự xác định từ số transaction), fallback về item đầu nếu chưa có transaction nào.
- **CategoryGrid**: emoji icon (lookup slug → emoji), tên, type (`kind`).
- **AddNewCategoryScreen**:
  - Form: name, kind (EXPENSE / INCOME), icon picker (`GET /category/icons`), optional monthly budget input (hiện chưa wire lên `/budget`).
  - Validation inline (`validationError` field-level) vs API error (`AppStatusDialog`).
  - Success → `AppStatusDialog(type = Success)` → onBack.

### 3.6 Profile / Settings
- **ProfileScreen**: hiển thị thông tin user (`GET /users/profile`), avatar.
- **SettingsLanguageScreen**: chọn ngôn ngữ, gọi `PATCH /users/profile` với `{ language }`. **Lưu ý**: BE hiện chưa có endpoint này — sẽ trả 404. Cần wire bên BE hoặc đổi sang endpoint tương ứng.

---

## 4. API Specification

> Base URL (debug): `http://10.0.2.2:30050/`
> Mọi response thành công đều bọc `{ data, message, statusCode }`. Bảng dưới chỉ mô tả phần `data`.

### 4.1 Auth

#### POST `/auth/signup`
- **Body**: `{ name, email, password }` — password đã SHA-256 + Base64 encode từ client.
- **Response**: User object `{ _id, email, name, avatar, createdAt, updatedAt }`.
- **Errors**: 401 "Email already existed".
- Side effect: tự gọi `seedDefaultCategories(userId)` insert 8 default categories.

#### POST `/auth/signin`
- **Body**: `{ email, password }` — password đã SHA-256 + Base64 encode.
- **Response**: `{ accessToken: string, user: User }`.
- **Errors**: 401 "Password is incorrect".

### 4.2 User (yêu cầu Bearer token)

#### GET `/users/profile`
- **Response**: User `{ _id, email, name, avatar, language?, createdAt, updatedAt }`.

#### POST `/users/updateAvatar` (multipart)
- **Body**: `avatarFile` (binary).
- **Response**: User đã cập nhật avatar URL.

#### POST `/users/create` (raw — nội bộ)
- **Body**: `{ email, name, password }`. Trường hợp thông thường dùng `/auth/signup`.

### 4.3 Category (yêu cầu Bearer token)

#### GET `/category`
- **Response**: `Array<Category>` mỗi item:
  ```ts
  {
    _id: string,
    userId: string,
    name: string,
    kind: 'expense' | 'income',
    icon: string,          // slug, vd "food", "salary"
    color?: string,        // hex
    isDefault: boolean,
    isMostUsed: boolean,   // true cho category có nhiều transaction nhất
    createdAt, updatedAt
  }
  ```

#### GET `/category/icons`
- **Response**: `Array<{ slug, label, icon }>` — 25 emoji presets (food 🍔, transport 🚗, salary 💰,…). Không có `_id`.

#### GET `/category/types`
- **Response**: `[{ slug: 'expense', label: 'Expense', icon: '💸' }, { slug: 'income', label: 'Income', icon: '💵' }]`.

#### POST `/category/create`
- **Body**: `{ name: string, kind: 'expense'|'income', icon: string (slug), color?: string }`.
- **Response**: Category vừa tạo.
- **Errors**: 409 "Category name already exists".

#### PATCH `/category/:id`
- **Body**: `{ name?, icon?, color? }`.
- **Errors**: 404 "Category not found", 409 trùng tên.

#### DELETE `/category/:id`
- **Response**: `{ success: true }`.
- **Errors**: 404 "Category not found".

### 4.4 Budget (yêu cầu Bearer token)

#### PUT `/budget`
- **Body**: `{ categoryId, yearMonth: 'YYYY-MM', amount: number >= 0 }`.
- **Response**: Budget object `{ _id, userId, categoryId, yearMonth, amount, createdAt, updatedAt }`.
- **Errors**: 404 "Category not found".

#### GET `/budget?yearMonth=YYYY-MM`
- Mặc định tháng UTC hiện tại nếu không có query.
- **Response**: `Array<{ ...budget, categoryId: <populated>, spent: number, remaining: number }>`.

#### DELETE `/budget/:id`
- **Response**: `{ success: true }`.
- **Errors**: 404 "Budget not found".

### 4.5 Transactions (yêu cầu Bearer token)

#### POST `/transactions/create` (multipart)
- **Body**: `amount` (number, > 0), `categoryId` (ObjectId), `image` (optional binary).
- **Response**: Transaction với `categoryId` đã populate `{ _id, name, icon, color, kind }`.
- **Errors**: 404 "Category not found" (categoryId không thuộc user).
- Image upload Cloudinary chỉ khi có file.

#### GET `/transactions`
- **Response**:
  ```ts
  {
    transactions: Array<{
      _id, userId,
      categoryId: { _id, name, icon, color, kind },   // populated
      amount, imageUrl?, createdAt, updatedAt
    }>,
    totalSpent: number   // tổng amount của giao dịch thuộc expense categories
  }
  ```
- Sort: `createdAt` desc.

### 4.6 Statistics (yêu cầu Bearer token)

#### GET `/statistics/overview?yearMonth=YYYY-MM`
- Mặc định tháng UTC hiện tại.
- **Response**:
  ```ts
  {
    yearMonth: string,
    totalSpent, totalSpentLastMonth,
    spentChangePercent: number | null,  // null khi prev = 0
    totalIncome, totalIncomeLastMonth, incomeChangePercent: number | null,
    totalBudget, remainingBudget,
    safeToSpendPerDay: number | null,   // null khi past month
    daysLeftInMonth: number,
    breakdown: Array<{
      categoryId, name, icon, color?, kind: 'expense'|'income',
      amount, percentage,
      amountLastMonth, changePercent: number | null
    }>,
    insights: {
      topImprovement: { categoryId, name, icon, savedAmount, changePercent } | null,
      topRegression:  { categoryId, name, icon, extraAmount, changePercent } | null,
      topSpending:    { categoryId, name, icon, amount, percentage }         | null
    }
  }
  ```

---

## 5. Mô hình dữ liệu

### 5.1 MongoDB collections

| Collection      | Trường chính                                                                                     |
|-----------------|--------------------------------------------------------------------------------------------------|
| `users`         | _id, email (unique), name, password (hashed), avatar?, language?, createdAt, updatedAt           |
| `categories`    | _id, userId, name, kind, icon (slug), color?, isDefault, (+ index userId+name unique)            |
| `budgets`       | _id, userId, categoryId, yearMonth (regex YYYY-MM), amount, (+ unique userId+categoryId+yearMonth)|
| `transactions`  | _id, userId, categoryId, amount, imageUrl?, createdAt, updatedAt                                 |

### 5.2 Local cache (Android Room — version 1)

| Entity              | Trường                                                                                  |
|---------------------|-----------------------------------------------------------------------------------------|
| `users`             | id (PK, String), email, name, avatar?, language?                                        |
| `categories`        | id (PK, String), name, kind, icon, color?, isDefault, isMostUsed                        |
| `category_icons`    | slug (PK, String), label, icon (emoji)                                                  |
| `expenses` (legacy) | (Expense entity — chưa được sử dụng, dùng API endpoint /transactions thay vì cache local)|

Cấu hình: `fallbackToDestructiveMigration()` — dev có thể downgrade bằng cách clear app data.

### 5.3 DataStore
- `accessToken`: JWT
- `language`: language code (vd "vi", "en")

---

## 6. Format chung

- **Số tiền**: `Utils.formatNumber(value)` → `"1,234,567.89"` (Locale.US, dấu phẩy 3 số).
- **Currency display**: prefix `$`.
- **Date relative**: xem [section 3.4](#34-history).
- **Currency**: USD (placeholder cho v1, chưa multi-currency).

---

## 7. Known limitations / TODO

1. **`PATCH /users/profile`** (FE gọi để update language) — BE chưa implement.
2. **Budget input** trong AddNewCategoryScreen — chưa wire lên `PUT /budget`. Hiện input chỉ là UI placeholder.
3. **TransactionDetailScreen** — đã có file nhưng chưa wire navigation từ `MomentItem` click.
4. **Settings tab** — mới có Language; chưa có logout, currency, theme.
5. **Insights** trên HomeScreen — mới render `topImprovement`; `topRegression` và `topSpending` chưa có UI riêng.
6. **CategoryRepository cache invalidation**: `isMostUsed` có thể stale khi user tạo transaction (chưa invalidate cache).
7. **i18n**: strings hỗn hợp Việt + Anh, chưa hoàn chỉnh ở các file `strings.xml`.
