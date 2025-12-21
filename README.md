🚀 Cách chạy project

1- Clone repo

git clone https://github.com/ndkhoa29/DoAnAndroid.git

2️- Build bằng Gradle Wrapper

(Trong cửa sổ Terminal chạy lệnh sau)

./gradlew clean build

⚠️ Không cần cài Gradle thủ công – hệ thống sẽ tự tải đúng phiên bản qua Wrapper.

🌿 Quy tắc làm việc với Git

🔹 Nhánh chính

main: nhánh ổn định, chỉ merge code đã review.

🔹 Nhánh chức năng

Đặt theo cấu trúc:

type/member-name/feature-name

feature:	Làm tính năng mới

fix:	Sửa lỗi

update:	Cập nhật, chỉnh sửa nhỏ

Ví dụ:

feature/khoa/login

feature/khoa/service-list

🔹 Các bước làm việc

1- Tạo branch riêng:

git checkout -b feature/<tên-thành-viên>/<chức-năng>

2- Commit code:

git add .

git commit -m "Mô tả ngắn gọn nội dung thay đổi"

3- Push lên GitHub:

git push -u origin feature/<tên-thành-viên>/<chức-năng>

4- Tạo Pull Request (PR) → chờ trưởng nhóm review & merge.

✅ Lưu ý:

Không commit trực tiếp vào main.

Mỗi tính năng → 1 branch riêng → 1 PR.
"" 
