🚀 Cách chạy project

1- Clone repo

git clone https://github.com/<your-team>/HomeServiceApp.git

cd HomeServiceApp

2️- Build bằng Gradle Wrapper

./gradlew clean build

⚠️ Không cần cài Gradle thủ công – hệ thống sẽ tự tải đúng phiên bản qua Wrapper.

🌿 Quy tắc làm việc với Git

🔹 Nhánh chính

main: nhánh ổn định, chỉ merge code đã review.

🔹 Nhánh chức năng

Đặt theo cấu trúc:

<type>/<member-name>/<feature-name>

Ví dụ:

feature/khoa/login

feature/nhi/service-list

🔹 Các bước làm việc

1- Tạo branch riêng:

git checkout -b feature/<tên-thành-viên>/<chức-năng>

2- Commit code:

git add .

git commit -m "Mô tả ngắn gọn nội dung thay đổi"

3- Push lên GitHub:

git push -u origin feature/<tên-thành-viên>/<chức-năng>

4- Tạo Pull Request (PR) → chờ trưởng nhóm review & merge.
