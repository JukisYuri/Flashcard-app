# Danh sách tính năng ứng dụng Mind Card

Dưới đây là bảng tổng hợp tất cả các tính năng của hệ thống (bao gồm ứng dụng di động Android và Spring Boot Backend), được cập nhật theo trạng thái hiện thực thực tế mới nhất.

---

## 1. Các tính năng ĐÃ hiện thực (Fully Implemented)

Các tính năng này đã được phát triển hoàn thiện, tích hợp giữa Android Frontend và Spring Boot Backend, và hoạt động trơn tru.

| Phân nhóm | Tên tính năng | Mô tả chi tiết |
| :--- | :--- | :--- |
| **Xác thực** | Đăng ký & Đăng nhập | Cho phép người dùng đăng ký tài khoản mới và đăng nhập thông qua Firebase Authentication. |
| | Khôi phục mật khẩu | Màn hình Quên mật khẩu gửi liên kết đặt lại mật khẩu qua email. |
| **Quản lý Bộ thẻ** | Tạo bộ thẻ thủ công | Nhập tên bộ thẻ, thể loại và đường dẫn ảnh đại diện để tạo bộ thẻ học mới. |
| | Sửa đổi bộ thẻ | Chỉnh sửa tên, thể loại hoặc ảnh bìa của bộ thẻ đã có. |
| | Xóa bộ thẻ | Nhấn giữ (Long press) vào một bộ thẻ trong danh sách bài học để xóa bộ thẻ khỏi cả bộ nhớ cục bộ và Server. |
| **Quản lý Thẻ học** | Thêm thẻ thủ công | Nhập từ tiếng Anh, phiên âm, từ loại, định nghĩa tiếng Việt, câu ví dụ và từ đồng nghĩa. |
| | Xem trước thẻ (Live Preview) | Màn hình thêm thẻ hiển thị hình ảnh thẻ thực tế cập nhật theo thời gian thực khi người dùng đang nhập thông tin. |
| | Sửa đổi thẻ học | Hộp thoại thông minh cho phép sửa chi tiết từng thẻ bên trong màn hình quản lý bộ thẻ, tích hợp mini preview. |
| | Xóa thẻ học | Xóa thẻ khỏi bộ thẻ học trực tiếp từ màn hình chỉnh sửa bộ thẻ. |
| **Hệ thống AI** | Sinh bộ thẻ tự động | Người dùng gửi từ khóa/chủ đề (ví dụ: *Du lịch*, *Món ăn*), AI tự sinh tiêu đề bộ thẻ và danh sách các thẻ tương ứng. |
| | Tự động dự phòng (Fallback) | Nếu API Gemini bị quá tải (lỗi 503/429), server tự động chuyển sang sinh bộ thẻ demo cục bộ chất lượng cao để tránh crash ứng dụng. |
| **Học tập** | Giao diện Flashcard lật | Lật thẻ để xem định nghĩa, câu ví dụ và từ đồng nghĩa với hiệu ứng xoay 3D mượt mà. |
| | Đánh giá mức độ nhớ | Đánh giá khả năng ghi nhớ qua các nút: `Again` (Chưa nhớ), `Hard` (Khó), `Easy` (Dễ nhớ). |
| | Thống kê kết quả học | Kết thúc phiên học sẽ hiển thị màn hình kết quả tính toán độ chính xác (%), điểm kinh nghiệm (XP) đạt được và thời gian học. |
| **Tiện ích học tập** | Nhắc nhở học tập (Daily Reminder) | Tự động đặt lịch thông báo nhắc nhở học tập hàng ngày lúc **20:00** thông qua `AlarmManager`. Hỗ trợ xin cấp quyền thông báo trên Android 13+ và tự động khôi phục lịch khi thiết bị khởi động lại (`BootReceiver`). |
| | Phát âm từ vựng (TTS Audio) | Tích hợp dịch vụ Text-to-Speech (`WidgetTtsService`) hỗ trợ phát âm tiếng Anh chuẩn khi người dùng bấm nút loa trên thẻ học. |
| **Cá nhân hóa & Game** | Chỉ số Profile | Hiển thị Level, Điểm kinh nghiệm (XP), tổng số từ vựng đã học, chuỗi học liên tục hiện tại (Current Streak) và chuỗi tốt nhất (Best Streak). |
| | Biểu đồ hoạt động tuần | Biểu đồ cột tự động tính toán và hiển thị các ngày trong tuần người dùng có vào học thẻ từ. |
| | Hệ thống Huy hiệu (Badges) | Grid 2x2 hiển thị 4 huy hiệu mục tiêu (`Day One`, `Streak Lord`, `Vocab Master`, `XP Champion`) kèm thanh tiến trình thực tế. |
| | Chỉnh sửa thông tin cá nhân | Thay đổi tên hiển thị trong hồ sơ cá nhân. |
| | Bảng xếp hạng (Leaderboard) | Màn hình hiển thị danh sách xếp hạng người dùng trong tuần dựa trên số lượng XP tích lũy được từ các phiên học. |
| **Offline & Đồng bộ** | Lưu trữ cục bộ & Offline Mode | Tích hợp **Room Database** cục bộ trên thiết bị Android. Khi mất mạng, người dùng vẫn có thể xem và học thẻ bình thường. Khi có mạng trở lại, `SyncManager` sẽ tự động đồng bộ dữ liệu về Server. |
| **Backend & CSDL** | Lưu trữ CSDL MySQL | Chuyển đổi hoàn toàn Spring Boot backend sang lưu trữ dữ liệu bền vững trên hệ quản trị cơ sở dữ liệu **MySQL**. |

---

## 2. Các tính năng CHƯA hiện thực hoặc đề xuất cải tiến (Not/Partially Implemented)

Đây là các tính năng đang ở dạng khung giao diện hoặc đang chờ tích hợp thư viện bổ sung:

| Phân nhóm | Tên tính năng | Trạng thái hiện tại | Đề xuất hướng phát triển |
| :--- | :--- | :--- | :--- |
| **Xác thực** | Đăng nhập bằng Google | ⚠️ **Chỉ có giao diện** | Tích hợp thư viện Google Sign-In SDK vào Android và Firebase để đăng nhập bằng một chạm. |
| **Ảnh đại diện** | Tải ảnh bìa trực tiếp | ⚠️ **Nhập link dạng văn bản** | Thay vì nhập link URL thủ công, cho phép người dùng chọn ảnh trực tiếp từ thư viện điện thoại và tải lên server qua API (ví dụ: Lưu trữ cục bộ hoặc S3/Cloudinary). |
