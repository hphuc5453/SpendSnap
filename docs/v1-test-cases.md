# SpendSnap — Test Cases v1

> Bộ test case cho việc kiểm thử thủ công (manual QA) phiên bản 1.
> Bao trùm: Auth, Categories, Camera/Transactions, History, Home/Statistics, Profile/Settings.
>
> **Định dạng**: mỗi case có `ID`, mô tả, tiền đề (Pre), các bước (Steps), kết quả mong đợi (Expected), độ ưu tiên (P0 = blocker, P1 = high, P2 = medium, P3 = low).

---

## 1. Authentication

### TC-AUTH-001 — Signup thành công (P0)
- **Pre**: Email chưa tồn tại; backend chạy.
- **Steps**:
  1. Mở app lần đầu → màn Login.
  2. Tap "Sign up".
  3. Điền: name, email hợp lệ, password >= 6 ký tự, confirm password giống password.
  4. Tap nút Signup.
- **Expected**:
  - LoadingDialog hiện rồi tắt.
  - Điều hướng về Login (hoặc auto-login tuỳ flow).
  - BE: user mới được tạo + 8 default categories tự seed.

### TC-AUTH-002 — Signup với email đã tồn tại (P1)
- **Pre**: Đã có user với email này.
- **Steps**: Như TC-AUTH-001 nhưng dùng email đã đăng ký.
- **Expected**: `AppStatusDialog(type = Error)` hiển thị, title "Có lỗi xảy ra", message từ BE ("Email already existed" hoặc tương đương).

### TC-AUTH-003 — Signup với confirm password sai (P1)
- **Steps**: Nhập confirm password khác password → tap Signup.
- **Expected**: Inline error dưới field "Password mismatch" (hoặc theo i18n). Không gọi API.

### TC-AUTH-004 — Signup với email không hợp lệ (P1)
- **Steps**: Nhập email "abc" → tap Signup.
- **Expected**: Inline error "Email không hợp lệ". Không gọi API.

### TC-AUTH-005 — Login thành công (P0)
- **Pre**: User đã signup.
- **Steps**: Nhập email + password đúng → tap Login.
- **Expected**:
  - LoadingDialog → tắt.
  - Token được lưu DataStore.
  - **Icons được prefetch** (verify bằng cách mở Categories ngay sau khi login → grid không loading lâu).
  - Điều hướng vào Home.

### TC-AUTH-006 — Login sai password (P0)
- **Steps**: Nhập password sai.
- **Expected**: `AppStatusDialog(type = Error)`, message "Password is incorrect".

### TC-AUTH-007 — Login email không tồn tại (P1)
- **Steps**: Email lạ.
- **Expected**: `AppStatusDialog(type = Error)` với message phù hợp.

### TC-AUTH-008 — Persist session qua restart app (P1)
- **Pre**: Đã login.
- **Steps**: Force-stop app → mở lại.
- **Expected**: Không phải login lại; vào thẳng Home.

### TC-AUTH-009 — Validation field trống (P2)
- **Steps**: Tap Login khi email hoặc password trống.
- **Expected**: Inline error cho field tương ứng. Không gọi API.

---

## 2. Categories

### TC-CAT-001 — Danh sách categories sau signup mới (P0)
- **Pre**: User vừa signup xong, chưa tạo category nào.
- **Steps**: Vào tab Categories.
- **Expected**:
  - Hiển thị 8 default: Food & Drink, Transportation, Shopping, Entertainment, Health, Bills & Utilities, Salary, Freelance.
  - Mỗi card có emoji icon + name + type (expense/income).
  - Không có card nào được flag isMostUsed (chưa có transaction) → MostUsedCategoryCard hiển thị item đầu tiên với fallback.

### TC-CAT-002 — Most used card sau khi tạo transaction (P1)
- **Pre**: Đã tạo nhiều transaction cho 1 category cụ thể.
- **Steps**:
  1. Tạo 3 transaction cho "Food & Drink".
  2. Vào Categories.
- **Expected**: MostUsedCategoryCard hiển thị "Food & Drink" với emoji 🍔.
- **Note**: Nếu cache stale (isMostUsed chưa refresh), kill app rồi mở lại để clear cache.

### TC-CAT-003 — Tạo category mới thành công (P0)
- **Steps**:
  1. Vào Categories → tap card "+" (New Category).
  2. Nhập name, chọn kind = EXPENSE, chọn 1 icon.
  3. Tap "CREATE CATEGORY".
- **Expected**:
  - LoadingDialog → tắt.
  - `AppStatusDialog(type = Success)` title "Thành công!".
  - Tap OK → trở về Categories, category mới hiển thị trong grid.

### TC-CAT-004 — Tạo category trùng tên (P1)
- **Pre**: Đã có category tên "Food & Drink".
- **Steps**: Tạo category mới cũng tên "Food & Drink".
- **Expected**: `AppStatusDialog(type = Error)` message "Category name already exists".

### TC-CAT-005 — Tạo category với name trống (P1)
- **Steps**: Để trống name → tap Create.
- **Expected**: Inline error "Category name is required.". Không gọi API.

### TC-CAT-006 — Tạo category không chọn icon (P1)
- **Pre**: BE icons API trả về rỗng (mock).
- **Steps**: Tap Create khi `selectedIconSlug` rỗng.
- **Expected**: `validationError` inline "Vui lòng chọn icon.". Không gọi API.

### TC-CAT-007 — Toggle EXPENSE/INCOME tab (P2)
- **Steps**: Trong AddNewCategoryScreen, tap qua lại 2 tab.
- **Expected**: Tab được highlight đổi đúng, không ảnh hưởng icon đã chọn.

### TC-CAT-008 — Icon picker scroll horizontal (P2)
- **Pre**: BE icons API trả 25 emoji.
- **Steps**: Mở AddNewCategoryScreen, scroll vùng icon picker.
- **Expected**: Tất cả 25 emoji render được, layout 4 cột chunk; auto-select icon đầu khi mới load.

### TC-CAT-009 — Categories cache memory (P2)
- **Steps**:
  1. Vào Categories (lần 1) — chờ API.
  2. Đi sang tab khác rồi quay lại Categories.
- **Expected**: Lần 2 hiển thị instant (từ memory cache), không hit network (verify qua logcat HttpLoggingInterceptor).

### TC-CAT-010 — Categories Room cache giữa các session (P2)
- **Steps**:
  1. Vào Categories.
  2. Kill app, mở lại, vào Categories (đảm bảo offline để confirm không gọi API).
- **Expected**: Categories vẫn hiển thị từ Room cache.

---

## 3. Camera & Tạo Transaction

### TC-TX-001 — Cấp quyền camera (P0)
- **Pre**: App vừa cài, chưa cấp camera permission.
- **Steps**: Vào tab Camera.
- **Expected**: System dialog xin quyền hiện. Nếu deny → màn hình hiển thị "Camera permission required".

### TC-TX-002 — Chụp ảnh + nhập số tiền + tạo transaction expense (P0)
- **Pre**: Đã grant camera; đã có expense category.
- **Steps**:
  1. Vào Camera, tap nút chụp.
  2. Trên CapturePreview: tab EXPENSE đã chọn sẵn, category đầu tiên đã chọn sẵn.
  3. Nhập số tiền bằng numeric keypad (vd "50000").
  4. Tap CONFIRM.
- **Expected**:
  - LoadingDialog hiện.
  - Tạo transaction thành công → `AppStatusDialog(type = Success)` "Thành công!".
  - Sau khi dismiss: trở về CameraView (capturedUri + amountText cleared).
  - Trên BE: transaction được tạo, image upload Cloudinary, `imageUrl` lưu.

### TC-TX-003 — Tạo transaction INCOME (P0)
- **Steps**: Như TC-TX-002 nhưng chuyển tab sang INCOME trước khi confirm.
- **Expected**: Category picker tự filter sang income categories (Salary, Freelance). Transaction tạo thành công với `kind = income` category.

### TC-TX-004 — Đổi tab khi category đã chọn của tab cũ (P1)
- **Steps**:
  1. Tab EXPENSE chọn "Food".
  2. Switch sang INCOME.
- **Expected**: `selectedCategoryId` reset về income category đầu tiên (Salary), không giữ Food id.

### TC-TX-005 — Tạo transaction với amount = 0 (P1)
- **Steps**: Nhập "0" → tap CONFIRM.
- **Expected**: Toast "Vui lòng nhập số tiền hợp lệ". Không gọi API.

### TC-TX-006 — Tạo transaction với amount nhập sai format (P1)
- **Steps**: Nhập "1.2.3" → tap CONFIRM.
- **Expected**: Keypad đã block chấm thứ 2 ngay khi nhập; nếu lọt → toast lỗi format.

### TC-TX-007 — Tạo transaction khi BE trả 404 Category not found (P1)
- **Pre**: Mock `selectedCategoryId` bằng ObjectId không tồn tại.
- **Steps**: Tap CONFIRM.
- **Expected**: `AppStatusDialog(type = Error)` "Category not found".

### TC-TX-008 — Retake ảnh (P2)
- **Steps**: Sau khi chụp, tap RETAKE.
- **Expected**: Trở về CameraView, có thể chụp lại; amount reset.

### TC-TX-009 — Tạo transaction không có image (P1)
- **Pre**: Nếu UI có flow để skip ảnh (hiện tại chưa expose, ảnh là bắt buộc bước trước confirm).
- **Note**: BE đã support `image` optional, nhưng FE v1 luôn yêu cầu chụp ảnh trước. Test ở mức API: dùng Postman gửi multipart không có `image` field → BE vẫn tạo thành công, `imageUrl` = undefined.

### TC-TX-010 — Numeric keypad backspace (P2)
- **Steps**: Nhập "12345" → tap BACKSPACE 2 lần.
- **Expected**: Hiển thị "123".

### TC-TX-011 — Numeric keypad max length (P2)
- **Steps**: Cố nhập > 10 ký tự.
- **Expected**: Sau ký tự thứ 10, không nhận thêm input.

### TC-TX-012 — Reset state sau success (regression bug fix) (P0)
- **Steps**:
  1. Tạo transaction thành công.
  2. Tap OK trên dialog.
  3. Lập tức tạo transaction mới.
- **Expected**: Dialog Success không hiện lại tự động (state đã reset). Luồng tạo thứ 2 phải gọi API thật.

---

## 4. History

### TC-HIST-001 — Hiển thị danh sách transactions (P0)
- **Pre**: Đã có ít nhất 5 transaction.
- **Steps**: Vào tab History.
- **Expected**:
  - LoadingDialog hiện rồi tắt.
  - `MoneyLeftCard` hiển thị `totalSpent` từ BE (không phải sumOf local).
  - `MomentsGrid` render layout 2-1-2 lặp lại.
  - Mỗi item có: amount (primary), emoji + category name (trắng bold), relative date (xám).

### TC-HIST-002 — Empty state (P1)
- **Pre**: User mới, chưa có transaction.
- **Steps**: Vào History.
- **Expected**: `MoneyLeftCard` show "$0.00". Section transactions hiển thị "No transactions yet. Start snapping!".

### TC-HIST-003 — Relative date format - vừa tạo (P1)
- **Pre**: Tạo transaction xong ngay trước khi mở History.
- **Expected**: Date hiển thị "0m ago" hoặc "Xm ago" (X = phút).

### TC-HIST-004 — Relative date format - vài giờ trước (P1)
- **Pre**: Transaction `createdAt` cách hiện tại 3h.
- **Expected**: "3h ago".

### TC-HIST-005 — Relative date format - vài ngày trước (P1)
- **Pre**: Transaction cách hiện tại 4 ngày.
- **Expected**: "4 days ago".

### TC-HIST-006 — Relative date format - quá 1 tuần (P1)
- **Pre**: Transaction cách hiện tại 10 ngày.
- **Expected**: Hiển thị "dd/MM/yyyy".

### TC-HIST-007 — Transaction không có ảnh (P2)
- **Pre**: Một transaction `imageUrl = null`.
- **Steps**: Vào History.
- **Expected**: Card vẫn render đầy đủ amount + category + date, không có nền ảnh (background đen từ Card). Không crash.

### TC-HIST-008 — Transaction với category bị xoá (P2)
- **Pre**: Tạo transaction → xoá category đó.
- **Steps**: Vào History.
- **Expected**: Card hiển thị emoji `📦` + name "Unknown".
- **Note**: Hiện BE chưa cấm xoá category đang có transaction. Cần đảm bảo FE không crash.

### TC-HIST-009 — totalSpent không tính income (P1)
- **Pre**: Có 1 expense $100 + 1 income $500.
- **Steps**: Vào History.
- **Expected**: `MoneyLeftCard` chỉ hiển thị $100, không bao gồm $500.

---

## 5. Home / Statistics

### TC-HOME-001 — Overview tháng hiện tại (P0)
- **Pre**: Đã có vài transaction trong tháng.
- **Steps**: Mở app, vào Home.
- **Expected**:
  - LoadingDialog → tắt.
  - `TotalSpentCard` hiển thị tổng chi tháng + pill % thay đổi vs tháng trước.
  - DonutChart vẽ động các cung khớp với `breakdown[].percentage`, màu lấy từ BE `color` (hex).
  - `RemainingBudgetCard`: hiển thị remaining; nếu có `safeToSpendPerDay` → kèm dòng "Safe to spend today".
  - Breakdown items render emoji + name + % + amount + trend %.

### TC-HOME-002 — Chưa có transaction nào (P1)
- **Pre**: User mới.
- **Steps**: Vào Home.
- **Expected**:
  - `TotalSpentCard` = $0.00, không có pill % (vì `spentChangePercent = null` khi prev = 0 và cur = 0 → BE trả 0; hoặc null).
  - DonutChart render full vòng grey (`#2C2C2E`).
  - Breakdown section "No spending yet this month".

### TC-HOME-003 — Over budget cảnh báo (P1)
- **Pre**: User có `totalBudget = $100`, đã tiêu `totalSpent = $150`.
- **Expected**: `RemainingBudgetCard` đổi sang nền đỏ, label "OVER BUDGET", amount `$50` (abs value), không có dòng safe-to-spend.

### TC-HOME-004 — Past month không có safe-to-spend (P1)
- **Pre**: Query `yearMonth` của tháng đã qua (chưa expose UI nhưng test API).
- **Expected**: `safeToSpendPerDay = null`, FE không render surface "Safe to spend today".

### TC-HOME-005 — Insight topImprovement (P2)
- **Pre**: Tháng này tiêu Travel ít hơn tháng trước.
- **Expected**: Trên Home hiển thị câu: "You're spending less on Travel. That's $X.XX saved this month."

### TC-HOME-006 — Trend màu khác nhau (P2)
- **Steps**: Quan sát breakdown items có `changePercent > 0` và `changePercent < 0`.
- **Expected**: Trend `>= 0` → đỏ `#EF5350` với dấu `+`; trend `< 0` → primary green với không có dấu cộng. `changePercent = null` → không hiển thị trend.

### TC-HOME-007 — Icon lookup từ slug (P1)
- **Pre**: Breakdown item có `icon = "food"`.
- **Expected**: Render emoji 🍔. Nếu slug không khớp icon map → fallback 📦.

---

## 6. Profile & Settings

### TC-PROF-001 — Hiển thị profile (P1)
- **Pre**: Đã login.
- **Steps**: Vào tab Profile.
- **Expected**: Hiển thị name, email, avatar (placeholder nếu null).

### TC-PROF-002 — Update language - bug đã biết (P2)
- **Steps**: Vào Settings → Language → chọn ngôn ngữ.
- **Expected (mong muốn)**: BE update language thành công.
- **Expected (hiện tại)**: BE chưa implement `PATCH /users/profile` → trả 404. FE nên handle bằng `AppStatusDialog(type = Error)`. **Mark BE TODO**.

### TC-PROF-003 — Logout (P1)
- **Pre**: Đã login.
- **Steps**: (Nếu có nút Logout) Tap Logout.
- **Expected**: Token xoá khỏi DataStore, memory cache xoá, điều hướng về Login. Mở lại app phải login lại.

---

## 7. Cross-cutting

### TC-XCUT-001 — Network offline (P1)
- **Pre**: Đã có data cache (categories, icons).
- **Steps**: Bật airplane mode, mở app.
- **Expected**:
  - Categories vẫn hiển thị từ Room cache.
  - History: hiển thị error "Lỗi: <message>" (không crash).
  - Home: error state với message từ exception.

### TC-XCUT-002 — Token hết hạn (P1)
- **Pre**: Token expired hoặc bị BE revoke.
- **Steps**: Gọi 1 API bất kỳ.
- **Expected**: BE trả 401 → FE hiển thị error dialog. **TODO**: chưa có auto-redirect về Login khi 401.

### TC-XCUT-003 — Đa session (P2)
- **Steps**: Login user A → switch DataStore token sang token user B → vào Home.
- **Expected**: Hiển thị data của user B (verify userId trong token được BE dùng làm scope).

### TC-XCUT-004 — Room downgrade an toàn (P2)
- **Pre**: Đang chạy DB version cao hơn (vd nếu trước đó dev chạy v8).
- **Steps**: Cài lại app từ build v1.
- **Expected**: App có thể crash vì Room không cho downgrade. Workaround: uninstall hoặc clear data trước khi cài v1.

### TC-XCUT-005 — Reusable AppStatusDialog (P2)
- **Steps**: Trigger các error/success dialog ở: Login fail, Signup fail, Create category success, Create category fail (API), Create transaction success, Create transaction fail.
- **Expected**: Tất cả đều dùng cùng `AppStatusDialog` component, layout đồng nhất (icon halo, title, message, button primary "ĐÓNG").

### TC-XCUT-006 — Loading indicator (P2)
- **Steps**: Trigger mọi API call (signin, signup, create category, create transaction, get transactions, get overview).
- **Expected**: `LoadingDialog` hiện đầy đủ thời gian request → tắt khi response về.

---

## 8. API contract / Integration

> Test trực tiếp bằng Postman / curl, không qua UI.

### TC-API-001 — Response wrapper đồng nhất (P0)
- **Steps**: Gọi bất kỳ endpoint thành công.
- **Expected**: Body chứa `{ data: ..., message: "Success", statusCode: 200 }`.

### TC-API-002 — JWT guard từ chối request không có token (P0)
- **Steps**: Gọi `GET /users/profile` không Authorization header.
- **Expected**: 401 Unauthorized.

### TC-API-003 — JWT guard từ chối token sai (P0)
- **Steps**: Gọi với `Authorization: Bearer abc`.
- **Expected**: 401.

### TC-API-004 — Cross-user isolation (P0)
- **Pre**: User A và B, mỗi user có categories riêng.
- **Steps**: Login user A → gọi `PATCH /category/<id-của-B>` để update.
- **Expected**: 404 "Category not found" (BE filter theo `userId`).

### TC-API-005 — Statistics aggregation chính xác (P1)
- **Pre**: User có:
  - Tháng này: 2 expense ($100, $200), 1 income ($500).
  - Tháng trước: 1 expense ($150).
- **Steps**: `GET /statistics/overview?yearMonth=<currentMonth>`.
- **Expected**:
  - `totalSpent = 300`, `totalSpentLastMonth = 150`, `spentChangePercent = 100`.
  - `totalIncome = 500`, `totalIncomeLastMonth = 0`, `incomeChangePercent = null` (prev = 0).
  - `breakdown` chỉ chứa expense categories.

### TC-API-006 — Multipart create transaction (P1)
- **Steps**: `POST /transactions/create` với `amount=50000` (string), `categoryId=<valid>`, không image.
- **Expected**: 201, transaction được tạo, `imageUrl` undefined, `categoryId` populated.

### TC-API-007 — Multipart create transaction với image (P1)
- **Steps**: Như TC-API-006 + thêm field `image` binary jpg.
- **Expected**: 201, `imageUrl` là URL Cloudinary `https://res.cloudinary.com/...`.

### TC-API-008 — Budget upsert (P1)
- **Steps**: `PUT /budget` body `{ categoryId, yearMonth: "2026-05", amount: 5000000 }` lần 1 → tạo mới; lần 2 cùng key → update amount.
- **Expected**: Cùng `_id` cho cả 2 lần, amount lần 2 ghi đè.

### TC-API-009 — Validation yearMonth regex (P2)
- **Steps**: Gửi `yearMonth: "2026-13"`.
- **Expected**: 400 validation error.

---

## 9. Performance / UX nhỏ

### TC-PERF-001 — Icons prefetch sau login (P2)
- **Steps**: Login → ngay lập tức vào Categories.
- **Expected**: Grid hiển thị emoji ngay, không chờ network round-trip riêng (icons đã được prefetch trong AuthViewModel.signIn).

### TC-PERF-002 — Cache hit miss (P3)
- **Steps**: Bật HttpLoggingInterceptor, mở app sau khi đã warm cache.
- **Expected**: `/category` và `/category/icons` không hit network nếu memory cache còn (categoryRepository).

### TC-PERF-003 — UI không jank khi scroll History dài (P3)
- **Pre**: 100+ transactions.
- **Expected**: Scroll mượt, không drop frame > 16ms khi load ảnh dần (Coil cache).

---

## 10. Regression checklist trước release

- [ ] Login + signup flow end-to-end.
- [ ] Tạo expense + income transaction thành công.
- [ ] Tạo category mới + edit/delete (nếu UI có).
- [ ] History grid hiển thị đúng amount + category + date.
- [ ] Home overview render đầy đủ 4 sections (total spent, donut, remaining budget, breakdown).
- [ ] Token persist sau restart app.
- [ ] Cấp / từ chối camera permission.
- [ ] Network offline không crash.
- [ ] AppStatusDialog hiển thị đồng nhất cho tất cả error/success.
- [ ] Reset state sau success: transaction, category.
- [ ] minSdk 24 device: `Utils.formatRelativeDate` không crash (đã dùng SimpleDateFormat).
- [ ] Room version 1 cài sạch: schema mới được tạo đúng.
