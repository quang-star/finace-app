from __future__ import annotations

import html
import os
import struct
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "Bao_cao_TongQuan_DoAn_QuanLyThuChi.pptx"
SLIDE_W = 12192000
SLIDE_H = 6858000

COLORS = {
    "bg": "0F172A",
    "panel": "172033",
    "panel2": "1F2937",
    "text": "F8FAFC",
    "muted": "CBD5E1",
    "green": "22C55E",
    "blue": "38BDF8",
    "amber": "FBBF24",
    "red": "FB7185",
    "line": "334155",
}


def emu(x: float) -> int:
    return int(x * 914400)


def esc(value: str) -> str:
    return html.escape(value, quote=True)


def png_size(path: Path) -> tuple[int, int]:
    with path.open("rb") as f:
        header = f.read(24)
    if header[:8] != b"\x89PNG\r\n\x1a\n":
        return 1080, 1920
    return struct.unpack(">II", header[16:24])


def fit_box(path: Path, x: int, y: int, w: int, h: int) -> tuple[int, int, int, int]:
    iw, ih = png_size(path)
    scale = min(w / iw, h / ih)
    nw, nh = int(iw * scale), int(ih * scale)
    return x + (w - nw) // 2, y + (h - nh) // 2, nw, nh


def solid_rect(shape_id: int, x: int, y: int, w: int, h: int, color: str, radius: bool = False) -> str:
    preset = "roundRect" if radius else "rect"
    return f"""
    <p:sp>
      <p:nvSpPr><p:cNvPr id="{shape_id}" name="Box {shape_id}"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
      <p:spPr><a:xfrm><a:off x="{x}" y="{y}"/><a:ext cx="{w}" cy="{h}"/></a:xfrm><a:prstGeom prst="{preset}"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val="{color}"/></a:solidFill><a:ln><a:solidFill><a:srgbClr val="{color}"/></a:solidFill></a:ln></p:spPr>
      <p:txBody><a:bodyPr/><a:lstStyle/><a:p/></p:txBody>
    </p:sp>"""


def line(shape_id: int, x1: int, y1: int, x2: int, y2: int, color: str = COLORS["line"], width: int = 19050) -> str:
    return f"""
    <p:cxnSp>
      <p:nvCxnSpPr><p:cNvPr id="{shape_id}" name="Line {shape_id}"/><p:cNvCxnSpPr/><p:nvPr/></p:nvCxnSpPr>
      <p:spPr><a:xfrm><a:off x="{min(x1, x2)}" y="{min(y1, y2)}"/><a:ext cx="{abs(x2 - x1)}" cy="{abs(y2 - y1)}"/></a:xfrm><a:prstGeom prst="line"><a:avLst/></a:prstGeom><a:ln w="{width}"><a:solidFill><a:srgbClr val="{color}"/></a:solidFill></a:ln></p:spPr>
    </p:cxnSp>"""


def text_box(shape_id: int, text: str, x: int, y: int, w: int, h: int, size: int = 24,
             color: str = COLORS["text"], bold: bool = False, align: str = "l") -> str:
    lines = text.split("\n")
    paras = []
    for i, item in enumerate(lines):
        bullet = item.startswith("- ")
        content = item[2:] if bullet else item
        bullet_xml = '<a:buChar char="•"/>' if bullet else '<a:buNone/>'
        margin = ' marL="285750" indent="-171450"' if bullet else ""
        paras.append(
            f'<a:p><a:pPr algn="{align}"{margin}>{bullet_xml}</a:pPr>'
            f'<a:r><a:rPr lang="vi-VN" sz="{size * 100}" dirty="0"'
            f'{" b=\"1\"" if bold else ""}><a:solidFill><a:srgbClr val="{color}"/></a:solidFill>'
            f'<a:latin typeface="Arial"/><a:cs typeface="Arial"/></a:rPr><a:t>{esc(content)}</a:t></a:r></a:p>'
        )
    return f"""
    <p:sp>
      <p:nvSpPr><p:cNvPr id="{shape_id}" name="Text {shape_id}"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
      <p:spPr><a:xfrm><a:off x="{x}" y="{y}"/><a:ext cx="{w}" cy="{h}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln></p:spPr>
      <p:txBody><a:bodyPr wrap="square" lIns="0" tIns="0" rIns="0" bIns="0"/><a:lstStyle/>{''.join(paras)}</p:txBody>
    </p:sp>"""


def image_pic(shape_id: int, rel_id: str, name: str, x: int, y: int, w: int, h: int) -> str:
    return f"""
    <p:pic>
      <p:nvPicPr><p:cNvPr id="{shape_id}" name="{esc(name)}"/><p:cNvPicPr><a:picLocks noChangeAspect="1"/></p:cNvPicPr><p:nvPr/></p:nvPicPr>
      <p:blipFill><a:blip r:embed="{rel_id}"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>
      <p:spPr><a:xfrm><a:off x="{x}" y="{y}"/><a:ext cx="{w}" cy="{h}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:ln><a:solidFill><a:srgbClr val="{COLORS['line']}"/></a:solidFill></a:ln></p:spPr>
    </p:pic>"""


def title(slide_no: int, main: str, sub: str = "") -> list[str]:
    parts = [text_box(10, main, emu(0.55), emu(0.35), emu(8.8), emu(0.55), 28, bold=True)]
    if sub:
        parts.append(text_box(11, sub, emu(0.58), emu(0.92), emu(8.5), emu(0.35), 12, COLORS["muted"]))
    parts.append(text_box(12, f"{slide_no:02d}", emu(12.1), emu(0.38), emu(0.55), emu(0.28), 10, COLORS["muted"], align="r"))
    parts.append(line(13, emu(0.55), emu(1.18), emu(12.75), emu(1.18), COLORS["line"]))
    return parts


def card(shape_id: int, heading: str, body: str, x: float, y: float, w: float, h: float, accent: str) -> list[str]:
    return [
        solid_rect(shape_id, emu(x), emu(y), emu(w), emu(h), COLORS["panel"], True),
        solid_rect(shape_id + 1, emu(x), emu(y), emu(0.08), emu(h), accent),
        text_box(shape_id + 2, heading, emu(x + 0.22), emu(y + 0.18), emu(w - 0.42), emu(0.35), 15, COLORS["text"], True),
        text_box(shape_id + 3, body, emu(x + 0.22), emu(y + 0.62), emu(w - 0.42), emu(h - 0.75), 11, COLORS["muted"]),
    ]


def architecture_slide(no: int) -> tuple[list[str], list[Path]]:
    parts = title(no, "Kiến trúc tổng thể", "Android Java kết nối Spring Boot REST API, dữ liệu lưu MySQL và xác thực Firebase")
    boxes = [
        ("Android App", "Activities/Fragments\nMVVM + LiveData\nRetrofit Client", 0.8, 2.1, COLORS["green"]),
        ("Firebase Auth", "Email/Google/Facebook\nFirebase ID Token\nĐồng bộ user", 0.8, 4.3, COLORS["amber"]),
        ("Spring Boot API", "Controller\nService\nRepository", 4.9, 2.1, COLORS["blue"]),
        ("MySQL Database", "Users, accounts\nTransactions, budgets\nAI scan logs", 8.9, 2.1, COLORS["red"]),
        ("AI Services", "ML Kit OCR\nYOLO/TFLite\nRandom Forest", 8.9, 4.3, COLORS["green"]),
    ]
    sid = 30
    for heading, body, x, y, accent in boxes:
        parts += card(sid, heading, body, x, y, 3.0, 1.35, accent)
        sid += 10
    parts += [
        line(100, emu(3.8), emu(2.75), emu(4.85), emu(2.75), COLORS["blue"], 25400),
        line(101, emu(7.9), emu(2.75), emu(8.85), emu(2.75), COLORS["blue"], 25400),
        line(102, emu(7.0), emu(4.1), emu(9.0), emu(4.75), COLORS["green"], 25400),
    ]
    return parts, []


def ai_slide(no: int) -> tuple[list[str], list[Path]]:
    parts = title(no, "Luồng AI hỗ trợ nhập liệu", "OCR hóa đơn và nhận diện sản phẩm giúp giảm nhập tay, người dùng vẫn xác nhận trước khi lưu")
    steps = [
        ("1", "Ảnh hóa đơn / camera", "Chụp từ thiết bị Android"),
        ("2", "ML Kit OCR / YOLO", "Xử lý cục bộ để giảm tải server"),
        ("3", "Trích xuất đặc trưng", "Số tiền, ngày, merchant, nhãn sản phẩm"),
        ("4", "Random Forest", "Gợi ý danh mục chi tiêu"),
        ("5", "Feedback loop", "Lưu chỉnh sửa để cải thiện mô hình"),
    ]
    sid = 40
    x = 0.65
    for num, heading, body in steps:
        parts += [
            solid_rect(sid, emu(x), emu(2.1), emu(2.25), emu(1.55), COLORS["panel"], True),
            text_box(sid + 1, num, emu(x + 0.12), emu(2.23), emu(0.35), emu(0.35), 15, COLORS["green"], True),
            text_box(sid + 2, heading, emu(x + 0.18), emu(2.68), emu(1.9), emu(0.35), 13, COLORS["text"], True),
            text_box(sid + 3, body, emu(x + 0.18), emu(3.08), emu(1.86), emu(0.42), 9, COLORS["muted"]),
        ]
        if x < 10:
            parts.append(line(sid + 4, emu(x + 2.25), emu(2.88), emu(x + 2.55), emu(2.88), COLORS["line"]))
        sid += 10
        x += 2.5
    parts += card(120, "Giá trị kỹ thuật", "- Edge AI xử lý tác vụ nặng ngay trên thiết bị\n- Backend tập trung phân loại, lưu log và học từ phản hồi\n- Thiết kế vẫn hoạt động khi tạm ẩn AI khỏi giao diện", 1.0, 4.55, 11.25, 1.2, COLORS["blue"])
    return parts, []


def image_slide(no: int, images: list[Path]) -> tuple[list[str], list[Path]]:
    parts = title(no, "Minh họa giao diện", "Một số màn hình tham khảo của ứng dụng quản lý thu chi")
    xs = [0.85, 4.65, 8.45]
    for i, img in enumerate(images[:3]):
        x, y, w, h = fit_box(img, emu(xs[i]), emu(1.55), emu(3.0), emu(4.9))
        parts.append(solid_rect(30 + i, emu(xs[i] - 0.08), emu(1.47), emu(3.16), emu(5.06), COLORS["panel"], True))
        parts.append(image_pic(40 + i, f"rId{i + 10}", img.name, x, y, w, h))
    return parts, images[:3]


def generic_slide(no: int, main: str, sub: str, cards: list[tuple[str, str, str]]) -> tuple[list[str], list[Path]]:
    parts = title(no, main, sub)
    positions = [(0.8, 1.75), (4.7, 1.75), (8.6, 1.75), (0.8, 4.05), (4.7, 4.05), (8.6, 4.05)]
    for idx, (heading, body, accent) in enumerate(cards):
        x, y = positions[idx]
        parts += card(30 + idx * 10, heading, body, x, y, 3.0, 1.7, accent)
    return parts, []


def cover_slide() -> tuple[list[str], list[Path]]:
    images = sorted((ROOT / "ui_references").glob("*.png"))
    img = images[0] if images else None
    parts = [
        solid_rect(1, 0, 0, SLIDE_W, SLIDE_H, COLORS["bg"]),
        solid_rect(2, emu(0.55), emu(0.55), emu(5.8), emu(0.08), COLORS["green"]),
        text_box(3, "TỔNG QUAN ĐỒ ÁN CUỐI MÔN", emu(0.6), emu(1.05), emu(6.4), emu(0.45), 18, COLORS["green"], True),
        text_box(4, "Ứng dụng quản lý thu chi cá nhân", emu(0.58), emu(1.62), emu(6.5), emu(1.25), 36, COLORS["text"], True),
        text_box(5, "Nội dung trình bày: tổng quan đề tài, chức năng chính, phân công công việc và tiến độ hiện tại", emu(0.62), emu(3.05), emu(6.2), emu(0.82), 15, COLORS["muted"]),
        text_box(6, "Ngày báo cáo: 01/06/2026", emu(0.62), emu(6.15), emu(3.8), emu(0.35), 12, COLORS["muted"]),
    ]
    media = []
    if img:
        x, y, w, h = fit_box(img, emu(8.0), emu(0.72), emu(3.6), emu(5.95))
        parts.append(solid_rect(7, emu(7.8), emu(0.55), emu(4.0), emu(6.25), COLORS["panel"], True))
        parts.append(image_pic(8, "rId10", img.name, x, y, w, h))
        media.append(img)
    return parts, media


SLIDES = [
    cover_slide,
    lambda: generic_slide(2, "Bối cảnh và mục tiêu", "Ứng dụng tập trung vào nhu cầu ghi chép, theo dõi và phân tích tài chính cá nhân.",
                          [("Vấn đề", "- Ghi chép thủ công dễ bỏ sót\n- Khó theo dõi ngân sách theo danh mục\n- Dữ liệu phân tán giữa nhiều ví", COLORS["red"]),
                           ("Mục tiêu", "- Theo dõi thu chi hằng ngày\n- Quản lý ví, danh mục và ngân sách\n- Báo cáo chi tiêu rõ ràng", COLORS["green"]),
                           ("Phạm vi", "- Mobile Android cho người dùng cuối\n- Backend REST API quản lý nghiệp vụ\n- MySQL lưu dữ liệu quan hệ", COLORS["blue"]),
                           ("Điểm nổi bật", "- Firebase Authentication\n- Kiến trúc MVVM + Repository\n- AI hỗ trợ scan hóa đơn/sản phẩm", COLORS["amber"])]),
    lambda: generic_slide(3, "Tổng quan về đề tài", "Đề tài xây dựng ứng dụng mobile hỗ trợ người dùng quản lý dòng tiền cá nhân một cách đơn giản và có hệ thống.",
                          [("Tên đề tài", "- Ứng dụng quản lý thu chi cá nhân\n- Tên sản phẩm: CapMoney\n- Nền tảng chính: Android", COLORS["green"]),
                           ("Đối tượng", "- Người dùng cá nhân\n- Sinh viên, nhân viên văn phòng\n- Người cần theo dõi chi tiêu hằng ngày", COLORS["blue"]),
                           ("Bài toán", "- Khó nhớ các khoản chi nhỏ\n- Khó kiểm soát ngân sách theo tháng\n- Thiếu báo cáo trực quan", COLORS["red"]),
                           ("Hướng giải quyết", "- Ghi nhận thu/chi nhanh\n- Phân loại theo danh mục\n- Tổng hợp báo cáo và ngân sách", COLORS["amber"])]),
    lambda: generic_slide(4, "Chức năng chính", "Các chức năng được chia theo nhóm nghiệp vụ phục vụ trực tiếp quá trình quản lý tài chính cá nhân.",
                          [("Tài khoản", "- Đăng nhập/đăng ký\n- Firebase Authentication\n- Đồng bộ người dùng về backend", COLORS["green"]),
                           ("Thu chi", "- Thêm giao dịch thu/chi\n- Xem lịch sử giao dịch\n- Tự động cập nhật số dư ví", COLORS["blue"]),
                           ("Danh mục & ngân sách", "- Quản lý danh mục\n- Thiết lập hạn mức chi tiêu\n- Theo dõi số tiền đã chi", COLORS["amber"]),
                           ("Báo cáo", "- Thống kê theo thời gian\n- Tổng hợp theo danh mục\n- Hỗ trợ biểu đồ trực quan", COLORS["red"]),
                           ("AI hỗ trợ", "- OCR hóa đơn bằng ML Kit\n- Gợi ý danh mục bằng Random Forest\n- Nhận diện sản phẩm bằng YOLO", COLORS["green"]),
                           ("Cá nhân", "- Xem thông tin người dùng\n- Xem ví chính\n- Đăng xuất tài khoản", COLORS["blue"])]),
    lambda: generic_slide(5, "Phân công công việc", "Bảng phân công mẫu theo module. Có thể thay tên thành viên theo nhóm trước khi nộp/trình bày.",
                          [("Thành viên 1", "- Phân tích yêu cầu\n- Thiết kế database\n- Viết tài liệu tổng quan", COLORS["green"]),
                           ("Thành viên 2", "- Xây dựng Android UI\n- Màn hình đăng nhập, trang chủ\n- Luồng thêm giao dịch", COLORS["blue"]),
                           ("Thành viên 3", "- Xây dựng Spring Boot API\n- Service/Repository/DTO\n- Tích hợp MySQL", COLORS["amber"]),
                           ("Thành viên 4", "- Chức năng ngân sách/báo cáo\n- Kiểm thử luồng nghiệp vụ\n- Chuẩn bị slide/demo", COLORS["red"]),
                           ("Phần AI", "- OCR hóa đơn\n- Random Forest phân loại\n- YOLO nhận diện sản phẩm", COLORS["green"]),
                           ("Phần chung", "- Review code\n- Sửa lỗi tích hợp\n- Hoàn thiện tài liệu", COLORS["blue"])]),
    lambda: generic_slide(6, "Tiến độ đến hiện tại", "Tình trạng dự án ở mốc báo cáo: đã có khung hệ thống, các module chính và tài liệu kỹ thuật.",
                          [("Đã hoàn thành", "- Cấu trúc Android MVVM\n- Backend Spring Boot phân tầng\n- Schema MySQL và seed data", COLORS["green"]),
                           ("Đã có chức năng", "- Đăng nhập Firebase\n- Quản lý giao dịch\n- Danh mục, ví, ngân sách, báo cáo", COLORS["blue"]),
                           ("Đã tích hợp", "- Retrofit API client\n- Firebase token interceptor\n- Controller/Service/Repository", COLORS["amber"]),
                           ("Đang hoàn thiện", "- Kiểm thử end-to-end\n- Tối ưu giao diện\n- Rà soát lỗi nghiệp vụ", COLORS["red"]),
                           ("AI", "- Có pipeline OCR/YOLO/Random Forest\n- Một số luồng có thể tạm ẩn UI\n- Cần thêm dữ liệu để cải thiện độ chính xác", COLORS["green"]),
                           ("Kế hoạch tiếp theo", "- Hoàn thiện demo\n- Bổ sung test/lint/build\n- Chuẩn bị báo cáo cuối kỳ", COLORS["blue"])]),
    lambda: generic_slide(7, "Công nghệ sử dụng", "Stack được chọn theo hướng dễ triển khai, dễ bảo trì và phù hợp đồ án mobile.",
                          [("Mobile", "- Android Java\n- Retrofit\n- RecyclerView\n- Firebase SDK", COLORS["green"]),
                           ("Backend", "- Java Spring Boot\n- Spring Data JPA\n- REST Controller\n- Service Layer", COLORS["blue"]),
                           ("Database", "- MySQL\n- Schema quan hệ\n- Index theo user/date\n- Ràng buộc khóa ngoại", COLORS["amber"]),
                           ("AI/ML", "- Google ML Kit OCR\n- YOLO/TFLite\n- Smile Random Forest\n- Feedback loop", COLORS["red"])]),
    lambda: architecture_slide(8),
    lambda: generic_slide(9, "Kiến trúc Android", "Ứng dụng Android tổ chức theo MVVM, tách rõ giao diện, trạng thái và nguồn dữ liệu.",
                          [("View Layer", "- Activity/Fragment/XML Layout\n- Chỉ xử lý hiển thị và tương tác\n- Quan sát LiveData", COLORS["green"]),
                           ("ViewModel", "- Giữ trạng thái UI\n- Điều phối luồng dữ liệu\n- Không giữ Context", COLORS["blue"]),
                           ("Repository", "- Single source of truth\n- Gọi Retrofit API\n- Gom logic truy xuất dữ liệu", COLORS["amber"]),
                           ("Modules", "- Home, Transaction, Budget\n- Account, Category, Profile\n- Scan Bill/Product", COLORS["red"])]),
    lambda: generic_slide(10, "Kiến trúc Backend", "Spring Boot áp dụng layered architecture để giữ nghiệp vụ ổn định và dễ mở rộng.",
                          [("Controller", "- Nhận HTTP request\n- Chuẩn hóa response\n- Kiểm tra xác thực", COLORS["blue"]),
                           ("Service", "- Tính số dư ví\n- Xử lý giao dịch/ngân sách\n- Tích hợp AI classification", COLORS["green"]),
                           ("Repository", "- Spring Data JPA\n- CRUD theo entity\n- Tối ưu truy vấn MySQL", COLORS["amber"]),
                           ("Security", "- FirebaseAuthFilter\n- Verify ID Token\n- Gắn user vào SecurityContext", COLORS["red"])]),
    lambda: generic_slide(11, "Thiết kế cơ sở dữ liệu", "Schema MySQL xoay quanh user, ví, danh mục, giao dịch, ngân sách và log AI.",
                          [("Bảng lõi", "- users\n- accounts\n- categories\n- transactions", COLORS["green"]),
                           ("Kế hoạch", "- budgets\n- recurring_transactions\n- recurring_budgets\n- notifications", COLORS["blue"]),
                           ("AI Logs", "- transaction_images\n- ai_scan_logs\n- ai_product_logs\n- confidence_score", COLORS["amber"]),
                           ("Toàn vẹn", "- Khóa ngoại rõ ràng\n- Cascade theo user\n- Index user/date/account", COLORS["red"])]),
    lambda: generic_slide(12, "Luồng nghiệp vụ chính", "Các thao tác người dùng đi qua app, API, service và database theo một luồng nhất quán.",
                          [("Đăng nhập", "- Firebase Auth\n- Lấy Firebase ID Token\n- Đồng bộ user về backend", COLORS["green"]),
                           ("Thêm giao dịch", "- Nhập số tiền, loại, danh mục\n- Backend gán ví chính\n- Cập nhật số dư tự động", COLORS["blue"]),
                           ("Ngân sách", "- Tạo hạn mức theo danh mục\n- Theo dõi đã chi\n- Cảnh báo khi vượt hạn mức", COLORS["amber"]),
                           ("Báo cáo", "- Lọc theo ngày/tháng\n- Tổng hợp theo danh mục\n- Trả dữ liệu cho biểu đồ", COLORS["red"])]),
    lambda: ai_slide(13),
    lambda: image_slide(14, sorted((ROOT / "ui_references").glob("*.png"))[1:4]),
    lambda: generic_slide(15, "API và bảo mật", "Backend cung cấp REST API theo module, hầu hết endpoint yêu cầu Firebase token.",
                          [("Auth", "- POST /api/auth/firebase-login\n- Đồng bộ profile\n- Không lưu token thật trong code", COLORS["green"]),
                           ("Finance", "- /api/accounts\n- /api/categories\n- /api/transactions\n- /api/budgets", COLORS["blue"]),
                           ("Reports", "- /api/reports/by-category\n- Lọc theo startDate/endDate\n- Trả phần trăm và tổng tiền", COLORS["amber"]),
                           ("AI", "- /api/ai-scan/classify\n- /api/ai-scan/feedback\n- /api/ai-product/classify", COLORS["red"])]),
    lambda: generic_slide(16, "Kết luận và hướng phát triển", "Dự án đã hình thành đầy đủ mobile app, backend API, database và pipeline AI hỗ trợ nhập liệu.",
                          [("Kết quả", "- Có cấu trúc Android rõ ràng\n- Backend phân tầng\n- Schema dữ liệu đầy đủ", COLORS["green"]),
                           ("Giá trị", "- Giảm thao tác ghi chép\n- Theo dõi chi tiêu trực quan\n- Dữ liệu tập trung theo tài khoản", COLORS["blue"]),
                           ("Hạn chế", "- AI đang có phần tạm ẩn UI\n- Cần thêm kiểm thử tự động\n- Cần tối ưu triển khai production", COLORS["amber"]),
                           ("Mở rộng", "- Dashboard nâng cao\n- Nhắc nhở thông minh\n- Huấn luyện AI từ feedback thực tế", COLORS["red"])]),
]


def slide_xml(parts: list[str]) -> str:
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val="{COLORS['bg']}"/></a:solidFill><a:effectLst/></p:bgPr></p:bg><p:spTree>
    <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
    <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
    {''.join(parts)}
  </p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>'''


def rels_xml(items: list[tuple[str, str, str]]) -> str:
    rels = ['<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">']
    for rid, typ, target in items:
        rels.append(f'<Relationship Id="{rid}" Type="{typ}" Target="{target}"/>')
    rels.append("</Relationships>")
    return "".join(rels)


def content_types(slide_count: int, media: list[Path]) -> str:
    overrides = [
        '<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>',
        '<Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>',
        '<Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>',
        '<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>',
        '<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>',
        '<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>',
    ]
    overrides += [f'<Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>' for i in range(1, slide_count + 1)]
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  {''.join(overrides)}
</Types>'''


def presentation_xml(count: int) -> str:
    ids = "".join([f'<p:sldId id="{256+i}" r:id="rId{i}"/>' for i in range(1, count + 1)])
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId{count + 1}"/></p:sldMasterIdLst>
  <p:sldIdLst>{ids}</p:sldIdLst>
  <p:sldSz cx="{SLIDE_W}" cy="{SLIDE_H}" type="wide"/>
  <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>'''


THEME = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="CapMoney"><a:themeElements><a:clrScheme name="CapMoney"><a:dk1><a:srgbClr val="0F172A"/></a:dk1><a:lt1><a:srgbClr val="F8FAFC"/></a:lt1><a:dk2><a:srgbClr val="172033"/></a:dk2><a:lt2><a:srgbClr val="CBD5E1"/></a:lt2><a:accent1><a:srgbClr val="22C55E"/></a:accent1><a:accent2><a:srgbClr val="38BDF8"/></a:accent2><a:accent3><a:srgbClr val="FBBF24"/></a:accent3><a:accent4><a:srgbClr val="FB7185"/></a:accent4><a:accent5><a:srgbClr val="94A3B8"/></a:accent5><a:accent6><a:srgbClr val="10B981"/></a:accent6><a:hlink><a:srgbClr val="38BDF8"/></a:hlink><a:folHlink><a:srgbClr val="818CF8"/></a:folHlink></a:clrScheme><a:fontScheme name="Arial"><a:majorFont><a:latin typeface="Arial"/></a:majorFont><a:minorFont><a:latin typeface="Arial"/></a:minorFont></a:fontScheme><a:fmtScheme name="Default"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme></a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/></a:theme>'''

SLIDE_LAYOUT = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank" preserve="1"><p:cSld name="Blank"><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>'''

SLIDE_MASTER = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld><p:clrMap bg1="dk1" tx1="lt1" bg2="dk2" tx2="lt2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/><p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst><p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle/></p:txStyles></p:sldMaster>'''


def write_pptx() -> None:
    OUT.parent.mkdir(parents=True, exist_ok=True)
    slide_payloads = []
    media_files: list[Path] = []
    media_map: dict[Path, str] = {}

    for factory in SLIDES:
        parts, media = factory()
        slide_media = []
        for item in media:
            if item not in media_map:
                media_map[item] = f"image{len(media_map) + 1}.png"
                media_files.append(item)
            slide_media.append((item, media_map[item]))
        slide_payloads.append((parts, slide_media))

    with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", content_types(len(slide_payloads), media_files))
        z.writestr("_rels/.rels", rels_xml([
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument", "ppt/presentation.xml"),
            ("rId2", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties", "docProps/core.xml"),
            ("rId3", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties", "docProps/app.xml"),
        ]))
        pres_rels = [(f"rId{i}", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide", f"slides/slide{i}.xml") for i in range(1, len(slide_payloads) + 1)]
        pres_rels.append((f"rId{len(slide_payloads) + 1}", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster", "slideMasters/slideMaster1.xml"))
        z.writestr("ppt/presentation.xml", presentation_xml(len(slide_payloads)))
        z.writestr("ppt/_rels/presentation.xml.rels", rels_xml(pres_rels))
        z.writestr("ppt/theme/theme1.xml", THEME)
        z.writestr("ppt/slideLayouts/slideLayout1.xml", SLIDE_LAYOUT)
        z.writestr("ppt/slideLayouts/_rels/slideLayout1.xml.rels", rels_xml([
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster", "../slideMasters/slideMaster1.xml")
        ]))
        z.writestr("ppt/slideMasters/slideMaster1.xml", SLIDE_MASTER)
        z.writestr("ppt/slideMasters/_rels/slideMaster1.xml.rels", rels_xml([
            ("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout", "../slideLayouts/slideLayout1.xml"),
            ("rId2", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme", "../theme/theme1.xml"),
        ]))

        for i, (parts, slide_media) in enumerate(slide_payloads, start=1):
            z.writestr(f"ppt/slides/slide{i}.xml", slide_xml(parts))
            rels = [("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout", "../slideLayouts/slideLayout1.xml")]
            for idx, (_, media_name) in enumerate(slide_media, start=10):
                rels.append((f"rId{idx}", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image", f"../media/{media_name}"))
            z.writestr(f"ppt/slides/_rels/slide{i}.xml.rels", rels_xml(rels))

        for source, media_name in media_map.items():
            z.write(source, f"ppt/media/{media_name}")

        z.writestr("docProps/core.xml", '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><dc:title>Báo cáo ứng dụng quản lý thu chi cá nhân</dc:title><dc:subject>CapMoney</dc:subject><dc:creator>Codex</dc:creator><cp:lastModifiedBy>Codex</cp:lastModifiedBy><dcterms:created xsi:type="dcterms:W3CDTF">2026-06-01T00:00:00Z</dcterms:created><dcterms:modified xsi:type="dcterms:W3CDTF">2026-06-01T00:00:00Z</dcterms:modified></cp:coreProperties>''')
        z.writestr("docProps/app.xml", f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"><Application>Codex</Application><PresentationFormat>On-screen Show (16:9)</PresentationFormat><Slides>{len(slide_payloads)}</Slides></Properties>''')

    print(OUT)
    print(f"slides={len(slide_payloads)} size={OUT.stat().st_size}")


if __name__ == "__main__":
    write_pptx()
