CREATE DATABASE He_Thong_Ban_Thuoc;
USE He_Thong_Ban_Thuoc;
-- 1. Bảng user (người dùng / admin) 
CREATE TABLE NguoiDung (
  MaNguoiDung INT AUTO_INCREMENT PRIMARY KEY,       -- Mã người dùng
  MaVaiTro INT NOT NULL,                            -- Vai trò (FK -> VaiTro)
  SoDienThoai VARCHAR(20) UNIQUE,                   -- Số điện thoại
  Email VARCHAR(255) UNIQUE,                        -- Email đăng nhập
  PasswordHash VARCHAR(255) NOT NULL,               -- Mật khẩu (mã hóa)
  HoTen NVARCHAR(150),                              -- Họ và tên (Unicode)
  NgaySinh DATE NULL,                               -- Ngày sinh
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,       -- Ngày tạo tài khoản
  NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP 
              ON UPDATE CURRENT_TIMESTAMP,          -- Ngày cập nhật gần nhất
  DiemTichLuy INT DEFAULT 0,                        -- Điểm tích lũy
  TongTien DECIMAL(12,2) DEFAULT 0.00,              -- Tổng số tiền đã chi
  MaCapBac INT DEFAULT NULL,                        -- Cấp bậc thành viên (FK -> CapBac)
  TrangThai TINYINT(1) DEFAULT 1,                   -- Trạng thái (1=hoạt động, 0=khóa)

  -- Chỉ mục
  INDEX idx_role (MaVaiTro),
  INDEX idx_phone (SoDienThoai),
  INDEX idx_email (Email),

  -- Khóa ngoại
  FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro),
  FOREIGN KEY (MaCapBac) REFERENCES CapBac(MaCapBac)
) ENGINE=InnoDB;

SELECT * FROM NGUOIDUNG;
ALTER TABLE NguoiDung ALTER COLUMN MaVaiTro SET DEFAULT 2;


-- 2. Bảng vai trò 
CREATE TABLE VaiTro (
  MaVaiTro INT AUTO_INCREMENT PRIMARY KEY,       -- Mã vai trò
  TenVaiTro NVARCHAR(50) NOT NULL UNIQUE,        -- Tên vai trò (Admin, Khách hàng, Nhân viên...)
  MoTa NVARCHAR(255) NULL                        -- Mô tả thêm
) ENGINE=InnoDB;

SELECT * FROM VAITRO;

-- 3. Bảng cấp bậc thành viên 
CREATE TABLE CapBac (
  MaCapBac INT AUTO_INCREMENT PRIMARY KEY,                -- Mã cấp bậc
  TenCapBac NVARCHAR(50) NOT NULL UNIQUE,                 -- Tên cấp bậc (Vàng, Bạc, Kim Cương...)
  TongTienToiThieu DECIMAL(12,2) DEFAULT 0.00,            -- Ngưỡng tổng tiền để đạt cấp bậc
  TongDiemToiThieu INT DEFAULT 0,                        -- Ngưỡng điểm để đạt cấp bậc
  MoTa NVARCHAR(255) NULL,                               -- Mô tả thêm
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,             -- Ngày tạo
  NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP 
              ON UPDATE CURRENT_TIMESTAMP                 -- Ngày cập nhật gần nhất
) ENGINE=InnoDB;

-- 4. Bảng địa chỉ đặt hàng (địa chỉ đặt hàng của user)
CREATE TABLE DiaChi (
  MaDiaChi INT AUTO_INCREMENT PRIMARY KEY,                  -- Mã địa chỉ
  MaNguoiDung INT NOT NULL,                                 -- FK -> NguoiDung
  LoaiDiaChi NVARCHAR(100) DEFAULT 'Nhà',          -- Loại địa chỉ
  TenNguoiNhan NVARCHAR(255) NOT NULL,                      -- Tên người nhận
  SoDienThoai VARCHAR(20) NULL,                             -- SĐT người nhận
  DiaChiChiTiet VARCHAR(500),                               -- Số nhà, đường
  TinhThanh VARCHAR(100),                                   -- Tỉnh/thành
  QuanHuyen VARCHAR(100),                                   -- Quận/huyện
  PhuongXa VARCHAR(100),                                    -- Phường/xã
  DiaChiMacDinh TINYINT(1) DEFAULT 0,                       -- 1 = mặc định
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,               -- Ngày tạo

  -- Khóa ngoại
  FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

ALTER TABLE DiaChi
MODIFY COLUMN LoaiDiaChi NVARCHAR(100) DEFAULT 'Nhà';

ALTER TABLE DiaChi
ADD CONSTRAINT chk_LoaiDiaChi CHECK (LoaiDiaChi IN ('Nhà', 'Văn phòng'));

-- 5. Bảng danh mục thuốc (phân loại thuốc)
CREATE TABLE DanhMucThuoc (
  MaDanhMuc INT AUTO_INCREMENT PRIMARY KEY,                 -- Mã danh mục
  TenDanhMuc NVARCHAR(150) NOT NULL,                        -- Tên danh mục
  Slug VARCHAR(150) UNIQUE,                                 -- Đường dẫn URL
  MaDanhMucCha INT DEFAULT NULL,                            -- FK -> chính nó (danh mục cha)
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,

  -- Khóa ngoại tự tham chiếu
  FOREIGN KEY (MaDanhMucCha) REFERENCES DanhMucThuoc(MaDanhMuc)
) ENGINE=InnoDB;

-- 6. Bảng trung gian giữa danh mục và sản phẩm 
CREATE TABLE Sanpham_Danhmuc (
  MaSanPham INT NOT NULL,                               -- FK -> san_pham
  MaDanhMuc INT NOT NULL,                               -- FK -> danh_muc_thuoc
  -- Khóa ngoại
  FOREIGN KEY (MaSanPham) REFERENCES san_pham(MaSanPham) ON DELETE CASCADE,
  FOREIGN KEY (MaDanhMuc) REFERENCES DanhMucThuoc(MaDanhMuc) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Bảng sản phẩm (quản lý thông tin thuốc) 
CREATE TABLE san_pham (
  MaSanPham INT AUTO_INCREMENT PRIMARY KEY,                 -- Mã sản phẩm
  MaThuoc VARCHAR(100) NOT NULL UNIQUE,                     -- Mã thuốc riêng
  TenThuoc NVARCHAR(255) NOT NULL,                          -- Tên thuốc
  MoTaNgan NVARCHAR(500),                                   -- Mô tả ngắn
  MoTaChiTiet TEXT,                                         -- Mô tả chi tiết
  TrangThaiBan TINYINT(1) DEFAULT 0,                        -- 0 = ngừng bán, 1 = đang bán
  Gia DECIMAL(12,2) NOT NULL,                               -- Giá bán
  NhaSanXuat NVARCHAR(200),                                 -- Nhà sản xuất
  MaVach VARCHAR(100),                                      -- Mã vạch
  MaDonVi INT NOT NULL,                                     -- FK -> DonViTinh
  HanSuDung DATE NULL,                                      -- Hạn sử dụng
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
  NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- Khóa ngoại
  FOREIGN KEY (MaDonVi) REFERENCES DonViTinh(MaDonVi)
) ENGINE=InnoDB;

-- 8. Bảng đơn vị tính (quản lý đơn vị tính sản phẩm)
CREATE TABLE DonViTinh (
  MaDonVi INT AUTO_INCREMENT PRIMARY KEY,               -- Mã đơn vị
  TenDonVi NVARCHAR(50) NOT NULL,                       -- Tên đơn vị (viên, hộp, chai…)
  MoTa NVARCHAR(255),                                   -- Mô tả thêm
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP            -- Ngày tạo
) ENGINE=InnoDB;

-- 9. Bảng ảnh sản phẩm (quản lý ảnh của từng sản phẩm) 
CREATE TABLE AnhSanPham (
  MaAnh INT AUTO_INCREMENT PRIMARY KEY,                     -- Mã ảnh
  MaSanPham INT NOT NULL,                                   -- FK -> san_pham
  DuongDan VARCHAR(500) NOT NULL,                           -- Đường dẫn ảnh
  AnhChinh TINYINT(1) DEFAULT 0,                            -- 1 = ảnh chính
  ThuTu INT DEFAULT 0,                                      -- Thứ tự hiển thị
  NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,

  -- Khóa ngoại
  FOREIGN KEY (MaSanPham) REFERENCES san_pham(MaSanPham)
) ENGINE=InnoDB;

-- 10. Bảng sản phẩm liên quan 
CREATE TABLE SanPhamLienQuan (
  MaSanPham INT NOT NULL,                               -- FK -> san_pham (sản phẩm chính)
  MaSanPhamLQ INT NOT NULL,                             -- FK -> san_pham (sản phẩm liên quan)
  LoaiQuanHe ENUM('Thay thế','Combo','Tương tự') DEFAULT 'Tương tự',
  -- Khóa ngoại
  FOREIGN KEY (MaSanPham) REFERENCES san_pham(MaSanPham) ON DELETE CASCADE,
  FOREIGN KEY (MaSanPhamLQ) REFERENCES san_pham(MaSanPham) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Bảng tổn kho (quản lý sản phẩm trong kho và sản phẩm tồn kho)
CREATE TABLE TonKho (
	MaSanPham INT PRIMARY KEY,
    SoLuong INT DEFAULT 0,
    MucCanhBao INT DEFAULT 10,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MaSanPham) REFERENCES san_pham(MaSanPham) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 12. Bảng mã giảm giá (quản lý mã giảm giá và cá khuyến mãi) 
CREATE TABLE MaGiamGia (
    MaGiamGia INT AUTO_INCREMENT PRIMARY KEY,
    MaCode VARCHAR(100) UNIQUE NOT NULL,
    MoTa NVARCHAR(255),
    LoaiGiamGia ENUM('amount','percent') NOT NULL,
    GiaTriGiam DECIMAL(10,2) NOT NULL,
    GiaTriDonToiThieu DECIMAL(12,2) DEFAULT 0,
    SoLuongPhatHanh INT DEFAULT 0,
    SoLuongDaDung INT DEFAULT 0,
    NgayBatDau DATETIME,
    NgayKetThuc DATETIME,
    TrangThai TINYINT(1) DEFAULT 0,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 13. Bảng thanh toán 
CREATE TABLE ThanhToan (
    MaThanhToan INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    MaPhuongThuc INT NOT NULL,
    SoTien DECIMAL(12,2) NOT NULL,
    MaGiaoDich VARCHAR(100) UNIQUE,
    TrangThai ENUM('Chờ xử lý','Thành công','Thất bại','Hoàn tiền') DEFAULT 'Chờ xử lý',
    NgayThanhToan DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (MaDonHang) REFERENCES DonHang(MaDonHang) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MaPhuongThuc) REFERENCES PhuongThucThanhToan(MaPhuongThuc) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 14. Bảng phương thức thanh toán 
CREATE TABLE PhuongThucThanhToan (
    MaPhuongThuc INT AUTO_INCREMENT PRIMARY KEY,
    TenPhuongThuc NVARCHAR(100) NOT NULL,
    MoTa NVARCHAR(255),
    PhiGiaoDich DECIMAL(10,2) DEFAULT 0,
    TrangThai TINYINT(1) DEFAULT 0,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 15. Bảng đơn hàng (lưu thông tin các đơn hàng) 
CREATE TABLE DonHang (
    MaDonHang INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    MaDiaChi INT,
    MaGiamGia INT NULL,
    TongTien DECIMAL(12,2) NOT NULL,
    GiamGia DECIMAL(12,2) DEFAULT 0,
    TienPhaiTra DECIMAL(12,2) NOT NULL,
    MaPhuongThuc INT DEFAULT NULL,
    TrangThai ENUM('Chưa duyệt','Đang xử lý','Đã giao','Đã hủy') DEFAULT 'Chưa duyệt',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_donhang_user FOREIGN KEY (MaNguoiDung) 
        REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT fk_donhang_diachi FOREIGN KEY (MaDiaChi) 
        REFERENCES DiaChi(MaDiaChi),
    CONSTRAINT fk_donhang_giamgia FOREIGN KEY (MaGiamGia) 
        REFERENCES MaGiamGia(MaGiamGia),
    CONSTRAINT fk_donhang_phuongthuc FOREIGN KEY (MaPhuongThuc) 
        REFERENCES PhuongThucThanhToan(MaPhuongThuc)
) ENGINE=InnoDB;

ALTER TABLE DonHang 
MODIFY TrangThai ENUM('Đang xử lý','Đang giao','Đã giao','Đã hủy') DEFAULT 'Đang xử lý';

-- 16. Bảng chi tiết đơn hàng 
CREATE TABLE ChiTietDonHang (
    MaChiTiet INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(12,2) NOT NULL,
    ThanhTien DECIMAL(12,2) GENERATED ALWAYS AS (SoLuong * DonGia) STORED,
    CONSTRAINT fk_ctdh_donhang FOREIGN KEY (MaDonHang) 
        REFERENCES DonHang(MaDonHang)
        ON DELETE CASCADE,
    CONSTRAINT fk_ctdh_sanpham FOREIGN KEY (MaSanPham) 
        REFERENCES san_pham(MaSanPham)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 17. Bảng lịch sử trạng thái đơn hàng 
CREATE TABLE LichSuDonHang (
    MaLichSu INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    GhiChu NVARCHAR(255) NULL,
    TrangThai ENUM('chưa duyệt','đang xử lý','đã giao','đã hủy') NOT NULL,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lichsu_donhang FOREIGN KEY (MaDonHang) 
        REFERENCES DonHang(MaDonHang)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 18. Bảng hóa đơn 
CREATE TABLE HoaDon (
    MaHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    MaDonHang INT NOT NULL,
    SoHoaDon VARCHAR(50) UNIQUE NOT NULL,
    FilePDF VARCHAR(255) NOT NULL,
    TongTien DECIMAL(12,2) NOT NULL,
    ThueVAT DECIMAL(12,2) DEFAULT 0,
    NgayPhatHanh DATETIME,
    TrangThai ENUM('Đã phát hành','Đã hủy') DEFAULT 'Đã phát hành',
    CONSTRAINT fk_hoadon_donhang FOREIGN KEY (MaDonHang) 
        REFERENCES DonHang(MaDonHang)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 19. Bảng chi tiết hóa đơn 
CREATE TABLE ChiTietHoaDon (
    MaCTHD INT AUTO_INCREMENT PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(12,2) NOT NULL,
    ThanhTien DECIMAL(12,2) GENERATED ALWAYS AS (SoLuong * DonGia) STORED,
    CONSTRAINT fk_cthd_hoadon FOREIGN KEY (MaHoaDon) 
        REFERENCES HoaDon(MaHoaDon)
        ON DELETE CASCADE,
    CONSTRAINT fk_cthd_sanpham FOREIGN KEY (MaSanPham) 
        REFERENCES san_pham(MaSanPham)
) ENGINE=InnoDB;

-- 20. Bảng đơn thuốc (Upload) 
CREATE TABLE DonThuoc (
    MaDonThuoc INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    FileAnh VARCHAR(500),
    TrangThai ENUM('Chờ duyệt','Đã duyệt','Từ chối') DEFAULT 'Chờ duyệt',
    GhiChu NVARCHAR(500) NULL,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NguoiDuyet INT NULL,
    NgayDuyet DATETIME NULL,
    CONSTRAINT fk_donthuoc_user FOREIGN KEY (MaNguoiDung) 
        REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

-- 21. Bảng chi tiết đơn thuốc 
CREATE TABLE ChiTietDonThuoc (
    MaCTDT INT AUTO_INCREMENT PRIMARY KEY,
    MaDonThuoc INT NOT NULL,
    TenThuoc NVARCHAR(255) NOT NULL,
    SoLuong INT DEFAULT 0,
    HuongDanSuDung NVARCHAR(500) NULL,
    CONSTRAINT fk_ctdt_donthuoc FOREIGN KEY (MaDonThuoc) 
        REFERENCES DonThuoc(MaDonThuoc)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 22. Bảng trung gian giữa đơn thuốc và đơn hàng 
CREATE TABLE DonThuoc_DonHang (
    MaDonHang INT NOT NULL,
    MaDonThuoc INT NOT NULL,
    CONSTRAINT fk_dt_dh_donhang FOREIGN KEY (MaDonHang) 
        REFERENCES DonHang(MaDonHang)
        ON DELETE CASCADE,
    CONSTRAINT fk_dt_dh_donthuoc FOREIGN KEY (MaDonThuoc) 
        REFERENCES DonThuoc(MaDonThuoc)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 23. Bảng quản lý phiên tư vấn 
CREATE TABLE PhienTuVan (
    MaPhienTuVan INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiGui INT NOT NULL,
    MaNguoiNhan INT NULL,
    TrangThai ENUM('Đang mở','Đang xử lý','Đã kết thúc') DEFAULT 'Đang mở',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ptv_nguoi_gui FOREIGN KEY (MaNguoiGui) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT fk_ptv_nguoi_nhan FOREIGN KEY (MaNguoiNhan) REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

-- 1.24. Bảng quản lý tin nhắn trong phiên 
CREATE TABLE TinNhan (
    MaTinNhan INT AUTO_INCREMENT PRIMARY KEY,
    MaTuVan INT NOT NULL,
    NguoiDung ENUM('Khách hàng','Nhân viên','AI') NOT NULL,
    NoiDung TEXT,
    ThoiGian DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tn_tuvan FOREIGN KEY (MaTuVan) 
        REFERENCES PhienTuVan(MaPhienTuVan)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 1.25. Bảng quản lý đánh giá sản phẩm 
CREATE TABLE DanhGiaSanPham (
    MaDanhGia INT AUTO_INCREMENT PRIMARY KEY,
    MaSanPham INT NOT NULL,
    MaNguoiDung INT NOT NULL,
    SoSao TINYINT NOT NULL CHECK (SoSao BETWEEN 1 AND 5),
    TieuDe NVARCHAR(255),
    NoiDung TEXT,
    TrangThai ENUM('Chờ duyệt','Hiển thị','Ẩn') DEFAULT 'Chờ duyệt',
    SoLuotHuuIch INT DEFAULT 0,
    SoLuotKhongHuuIch INT DEFAULT 0,
    PhanHoi TEXT,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dg_sanpham FOREIGN KEY (MaSanPham) REFERENCES san_pham(MaSanPham),
    CONSTRAINT fk_dg_user FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

-- 1.26. Bảng quà tặng đổi điểm 
CREATE TABLE QuaTang (
    MaQuaTang INT AUTO_INCREMENT PRIMARY KEY,
    TenQuaTang NVARCHAR(255) NOT NULL,
    MoTa TEXT,                                      -- Mô tả chi tiết quà tặng
    HinhAnh VARCHAR(500),                           -- URL ảnh sản phẩm
    DiemCanDoi INT NOT NULL,                        -- Số điểm cần để đổi
    SoLuongTon INT DEFAULT 0,                       -- Số lượng tồn kho (0 = không giới hạn)
    TrangThai TINYINT(1) DEFAULT 1,                 -- 1=Hiển thị, 0=Ẩn
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_diem (DiemCanDoi),
    INDEX idx_trangthai (TrangThai)
) ENGINE=InnoDB;

-- 1.27. Bảng Điểm Thưởng (Chi tiết giao dịch điểm)
CREATE TABLE DiemThuong (
    MaDiem INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    MaDonHang INT NULL,                             -- Liên kết đơn hàng (nếu có)
    LoaiGiaoDich ENUM('Tich','Tieu','HetHan','HoanLai') NOT NULL,
    SoDiem INT NOT NULL,                            -- Số điểm thay đổi (+/-)
    SoDiemConLai INT NOT NULL,                      -- Số điểm còn lại sau giao dịch
    MoTa TEXT,                                      -- Mô tả giao dịch
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayHetHan DATETIME NOT NULL,                   -- Điểm hết hạn sau 3 tháng
    TrangThai ENUM('HieuLuc','DaSuDung','HetHan') DEFAULT 'HieuLuc',
    CONSTRAINT fk_diem_user FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung) ON DELETE CASCADE,
    INDEX idx_user_trangthai (MaNguoiDung, TrangThai),
    INDEX idx_hethan (NgayHetHan),
    INDEX idx_ngaytao (NgayTao)
) ENGINE=InnoDB;

-- 1.28. Bảng Lịch Sử Đổi Quà
CREATE TABLE LichSuDoiQua (
    MaDoiQua INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    MaQuaTang INT NOT NULL,
    DiemSuDung INT NOT NULL,                        -- Số điểm đã sử dụng
    SoLuong INT DEFAULT 1,                          -- Số lượng quà đổi
    TenQuaTang NVARCHAR(255),                       -- Lưu tên quà tại thời điểm đổi
    DiemCanDoiLucDoi INT,                           -- Lưu điểm cần đổi tại thời điểm đó
    GhiChu TEXT,                                    -- Ghi chú (địa chỉ nhận quà, etc)
    TrangThai ENUM('Chờ xử lý','Đã duyệt','Đổi thành công','Từ chối') DEFAULT 'Chờ xử lý',
    LyDoTuChoi TEXT,                                -- Lý do từ chối (nếu có)
    NgayDoi DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayXuLy DATETIME NULL,                         -- Ngày admin xử lý
    NguoiXuLy INT NULL,                             -- Admin xử lý
    CONSTRAINT fk_ls_user FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung) ON DELETE CASCADE,
    CONSTRAINT fk_ls_qua FOREIGN KEY (MaQuaTang) REFERENCES QuaTang(MaQuaTang) ON DELETE RESTRICT,
    CONSTRAINT fk_ls_admin FOREIGN KEY (NguoiXuLy) REFERENCES NguoiDung(MaNguoiDung) ON DELETE SET NULL,
    INDEX idx_nguoidung (MaNguoiDung),
    INDEX idx_ngaydoi (NgayDoi),
    INDEX idx_trangthai (TrangThai)
) ENGINE=InnoDB;

-- 1.28. Bảng thông báo hệ thống 
CREATE TABLE ThongBao (
    MaThongBao INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    TieuDe NVARCHAR(255) NOT NULL,
    NoiDung TEXT NOT NULL,
    LoaiThongBao ENUM('Đơn hàng','Hệ thống','Khuyến mãi','Đổi quà','Tư vấn') DEFAULT 'Hệ thống',
    TrangThai ENUM('Chưa đọc','Đã đọc') DEFAULT 'Chưa đọc',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayDoc DATETIME NULL,
    CONSTRAINT fk_tb_user FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

-- 1.29. Bảng giỏ hàng 
CREATE TABLE GioHang (
    MaGioHang INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    TrangThai ENUM('Đang mở','Thanh toán','Hủy') DEFAULT 'Đang mở',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_gh_user FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

-- 1.30. Bảng chi tiết giỏ hàng 
CREATE TABLE ChiTietGioHang (
    MaCTGH INT AUTO_INCREMENT PRIMARY KEY,
    MaGioHang INT NOT NULL,
    MaSanPham INT NOT NULL,
    SoLuong INT DEFAULT 1,
    DonGia DECIMAL(12,2) NOT NULL,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ctgh_gh FOREIGN KEY (MaGioHang) REFERENCES GioHang(MaGioHang) ON DELETE CASCADE,
    CONSTRAINT fk_ctgh_sp FOREIGN KEY (MaSanPham) REFERENCES san_pham(MaSanPham)
) ENGINE=InnoDB;

ALTER TABLE DanhMucThuoc 
ADD COLUMN Icon VARCHAR(255) AFTER Slug;

SELECT * FROM san_pham;

-- Danh mục cha
INSERT INTO DanhMucThuoc (TenDanhMuc, Slug, Icon) VALUES
(N'Vitamin & Khoáng chất', 'vitamin-khoang-chat', 'https://example.com/icons/vitamin.png'),
(N'Hỗ trợ điều trị', 'ho-tro-dieu-tri', 'https://example.com/icons/treatment.png'),
(N'Thuốc không kê đơn', 'thuoc-khong-ke-don', 'https://example.com/icons/otc.png'),
(N'Dụng cụ y tế', 'dung-cu-y-te', 'https://example.com/icons/medical-tools.png');

-- Danh mục con của "Vitamin & Khoáng chất" (cha = 1)
INSERT INTO DanhMucThuoc (TenDanhMuc, Slug, Icon, MaDanhMucCha) VALUES
(N'Vitamin C', 'vitamin-c', 'https://example.com/icons/vitamin-c.png', 1),
(N'Vitamin D', 'vitamin-d', 'https://example.com/icons/vitamin-d.png', 1),
(N'Khoáng chất', 'khoang-chat', 'https://example.com/icons/mineral.png', 1);

-- Danh mục con của "Hỗ trợ điều trị" (cha = 2)
INSERT INTO DanhMucThuoc (TenDanhMuc, Slug, Icon, MaDanhMucCha) VALUES
(N'Tiểu đường', 'tieu-duong', 'https://example.com/icons/diabetes.png', 2),
(N'Huyết áp cao', 'huyet-ap-cao', 'https://example.com/icons/hypertension.png', 2),
(N'Xương khớp', 'xuong-khop', 'https://example.com/icons/bone.png', 2);


-- Danh mục con của "Thuốc không kê đơn" (cha = 3)
INSERT INTO DanhMucThuoc (TenDanhMuc, Slug, Icon, MaDanhMucCha) VALUES
(N'Thuốc giảm đau', 'thuoc-giam-dau', 'https://example.com/icons/painkiller.png', 3),
(N'Thuốc hạ sốt', 'thuoc-ha-sot', 'https://example.com/icons/fever.png', 3),
(N'Thuốc ho - cảm cúm', 'thuoc-ho-cam-cum', 'https://example.com/icons/cold.png', 3);

-- Danh mục con của "Dụng cụ y tế" (cha = 4)
INSERT INTO DanhMucThuoc (TenDanhMuc, Slug, Icon, MaDanhMucCha) VALUES
(N'Nhiệt kế', 'nhiet-ke', 'https://example.com/icons/thermometer.png', 4),
(N'Máy đo huyết áp', 'may-do-huyet-ap', 'https://example.com/icons/blood-pressure.png', 4),
(N'Khẩu trang', 'khau-trang', 'https://example.com/icons/mask.png', 4);

-- Sản phẩm 1: Paracetamol 500mg → Thuốc giảm đau (MaDanhMuc = 8)
INSERT INTO Sanpham_Danhmuc (MaSanPham, MaDanhMuc)
VALUES (1, 8);

-- Sản phẩm 2: Amoxicillin 500mg → Thuốc ho - cảm cúm (MaDanhMuc = 10)
INSERT INTO Sanpham_Danhmuc (MaSanPham, MaDanhMuc)
VALUES (2, 10);

-- Sản phẩm 3: Vitamin C 500mg → Vitamin C (MaDanhMuc = 5)
INSERT INTO Sanpham_Danhmuc (MaSanPham, MaDanhMuc)
VALUES (3, 5);

INSERT INTO ThongBao (MaNguoiDung, TieuDe, NoiDung, LoaiThongBao, TrangThai)
VALUES
-- User 8
(8, N'Đơn hàng #DH1001', N'Đơn hàng của bạn đã được xác nhận.', 'Đơn hàng', 'Chưa đọc'),
(8, N'Khuyến mãi tháng 10', N'Giảm giá 20% cho đơn hàng trên 200k.', 'Khuyến mãi', 'Đã đọc'),

-- User 9
(9, N'Đổi quà thành công', N'Bạn vừa đổi quà thành công bằng điểm tích lũy.', 'Đổi quà', 'Chưa đọc'),
(9, N'Thông báo hệ thống', N'Hệ thống sẽ bảo trì vào ngày 30/09.', 'Hệ thống', 'Chưa đọc'),

-- User 36
(36, N'Tư vấn thuốc', N'Bạn có một phản hồi mới từ dược sĩ.', 'Tư vấn', 'Chưa đọc'),
(36, N'Đơn hàng #DH1002', N'Đơn hàng của bạn đang được vận chuyển.', 'Đơn hàng', 'Đã đọc'),

-- User 49
(49, N'Khuyến mãi đặc biệt', N'Nhận mã giảm giá 50k cho đơn hàng đầu tiên.', 'Khuyến mãi', 'Chưa đọc'),

-- User 50
(50, N'Đơn hàng #DH1003', N'Đơn hàng của bạn đã giao thành công.', 'Đơn hàng', 'Đã đọc'),

-- User 51
(51, N'Thông báo hệ thống', N'Nâng cấp hệ thống thành công.', 'Hệ thống', 'Chưa đọc'),

-- User 52
(52, N'Tư vấn sức khỏe', N'Dược sĩ đã phản hồi câu hỏi của bạn.', 'Tư vấn', 'Đã đọc'),

-- User 53
(53, N'Khuyến mãi đặc biệt', N'Ưu đãi 15% cho các sản phẩm Vitamin.', 'Khuyến mãi', 'Chưa đọc'),
(53, N'Đơn hàng #DH1004', N'Đơn hàng của bạn đã được xác nhận.', 'Đơn hàng', 'Chưa đọc');

CREATE TABLE Banner (
    MaBanner INT AUTO_INCREMENT PRIMARY KEY,
    HinhAnh VARCHAR(255) NOT NULL,   -- link ảnh banner
    LienKet VARCHAR(255) NOT NULL,   -- link chuyển hướng khi click
    TrangThai ENUM('Ẩn','Hiển thị') DEFAULT 'Hiển thị',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO LichSuDonHang (MaDonHang, GhiChu, TrangThai)
VALUES
-- Đơn hàng 1 (hiện tại đang "Đang xử lý")
(1, N'Khách hàng đặt đơn hàng', 'chưa duyệt'),
(1, N'Đơn hàng đang được xử lý', 'đang xử lý'),

-- Đơn hàng 2 (hiện tại đang "Chưa duyệt")
(2, N'Khách hàng đặt đơn hàng', 'chưa duyệt'),

-- Đơn hàng 3 (hiện tại đang "Đã giao")
(3, N'Khách hàng đặt đơn hàng', 'chưa duyệt'),
(3, N'Đơn hàng đang được xử lý', 'đang xử lý'),
(3, N'Đơn hàng đã được giao thành công', 'đã giao'),

-- Đơn hàng 4 (hiện tại đang "Đang xử lý")
(4, N'Khách hàng đặt đơn hàng', 'chưa duyệt'),
(4, N'Đơn hàng đang được xử lý', 'đang xử lý');


-- User 52
INSERT INTO ThongBao (MaNguoiDung, TieuDe, NoiDung, LoaiThongBao, TrangThai)
VALUES
(52, N'Khuyến mãi tháng 10', N'Giảm giá 10% cho các sản phẩm chăm sóc sức khỏe.', 'Khuyến mãi', 'Chưa đọc'),
(52, N'Đơn hàng #DH1050', N'Đơn hàng của bạn đang được đóng gói.', 'Đơn hàng', 'Chưa đọc'),
(52, N'Tư vấn thuốc', N'Dược sĩ đã phản hồi câu hỏi của bạn về thuốc vitamin.', 'Tư vấn', 'Chưa đọc'),
(52, N'Thông báo hệ thống', N'Hệ thống sẽ bảo trì vào cuối tuần này.', 'Hệ thống', 'Chưa đọc'),
(52, N'Đơn hàng #DH1051', N'Đơn hàng của bạn đã được vận chuyển.', 'Đơn hàng', 'Chưa đọc');

SELECT * FROM NGUOIDUNG;

ALTER TABLE NguoiDung
ADD COLUMN GioiTinh ENUM('Nam', 'Nữ', 'Khác') DEFAULT NULL,
ADD COLUMN AnhDaiDien VARCHAR(255) DEFAULT NULL;

INSERT INTO DiaChi (
  MaNguoiDung, LoaiDiaChi, TenNguoiNhan, SoDienThoai,
  DiaChiChiTiet, TinhThanh, QuanHuyen, PhuongXa,
  DiaChiMacDinh
) VALUES
-- Địa chỉ mặc định
(54, 'Nhà', 'Nguyễn Văn A', '0905123456',
 '123 Nguyễn Trãi, P.3, Q.5', 'TP Hồ Chí Minh', 'Quận 5', 'Phường 3', 1),

-- Địa chỉ bình thường
(54, 'Văn phòng', 'Nguyễn Văn A', '0905123456',
 '45 Trần Hưng Đạo, P.6, Q.1', 'TP Hồ Chí Minh', 'Quận 1', 'Phường 6', 0);
 
DELETE FROM DIACHI WHERE MaDiaChi IN (22,23,24,25,26) AND MaDiaChi IS NOT NULL;
SELECT * FROM DIACHI;
 

-- Tạo lại bảng PhienTuVan
CREATE TABLE PhienTuVan (
    MaPhienTuVan INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiGui INT NOT NULL,
    MaNguoiNhan INT NULL,
    TrangThai ENUM('Đang mở','Đang xử lý','Đã kết thúc') DEFAULT 'Đang mở',
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ptv_nguoi_gui FOREIGN KEY (MaNguoiGui) REFERENCES NguoiDung(MaNguoiDung),
    CONSTRAINT fk_ptv_nguoi_nhan FOREIGN KEY (MaNguoiNhan) REFERENCES NguoiDung(MaNguoiDung)
) ENGINE=InnoDB;

-- Tạo lại bảng TinNhan (đã sửa lỗi dấu phẩy thừa)
CREATE TABLE TinNhan (
    MaTinNhan INT AUTO_INCREMENT PRIMARY KEY,
    MaTuVan INT NOT NULL,
    NguoiDung ENUM('Khách hàng','AI') NOT NULL,  -- ✅ Đã bỏ dấu phẩy thừa
    NoiDung TEXT,
    ThoiGian DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tn_tuvan FOREIGN KEY (MaTuVan) 
        REFERENCES PhienTuVan(MaPhienTuVan)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Index để tăng tốc truy vấn
CREATE INDEX idx_phien_nguoi_gui ON PhienTuVan(MaNguoiGui, TrangThai);
CREATE INDEX idx_tin_nhan_phien ON TinNhan(MaTuVan, ThoiGian DESC);

SELECT * FROM TINNHAN;
DELETE FROM TINNHAN
WHERE MaTinNhan BETWEEN 118 AND 121;
SELECT * FROM PHIENTUVAN;
DELETE FROM PHIENTUVAN
WHERE MaPhienTuVan BETWEEN 4 AND 5;

INSERT INTO ThongBao (MaNguoiDung, TieuDe, NoiDung, LoaiThongBao, TrangThai)
VALUES
(74, 'Đơn hàng #001 đã được xác nhận', 'Đơn hàng #001 của bạn đã được xác nhận và đang chuẩn bị giao.', 'Đơn hàng', 'Chưa đọc'),
(74, 'Hệ thống bảo trì', 'Hệ thống sẽ bảo trì vào 12/10/2025 từ 01:00 đến 03:00.', 'Hệ thống', 'Đã đọc'),
(74, 'Khuyến mãi cuối tuần', 'Nhận ưu đãi giảm 20% cho tất cả sản phẩm trong cuối tuần này.', 'Khuyến mãi', 'Chưa đọc'),
(74, 'Đổi quà thành công', 'Bạn đã đổi thành công mã quà tặng #XYZ123.', 'Đổi quà', 'Đã đọc'),
(74, 'Tư vấn sản phẩm', 'Nhân viên tư vấn đã phản hồi yêu cầu của bạn.', 'Tư vấn', 'Chưa đọc'),
(74, 'Đơn hàng #002 đã giao', 'Đơn hàng #002 đã được giao thành công.', 'Đơn hàng', 'Đã đọc'),
(74, 'Cập nhật chính sách', 'Hệ thống đã cập nhật chính sách bảo mật mới.', 'Hệ thống', 'Chưa đọc'),
(74, 'Khuyến mãi đặc biệt', 'Chương trình khuyến mãi đặc biệt chỉ áp dụng hôm nay.', 'Khuyến mãi', 'Đã đọc'),
(74, 'Đổi quà thất bại', 'Quà tặng #ABC456 không thể đổi do hết hạn.', 'Đổi quà', 'Chưa đọc'),
(74, 'Tư vấn hẹn gặp', 'Bạn đã được sắp xếp lịch hẹn tư vấn vào 14/10/2025.', 'Tư vấn', 'Đã đọc');

-- Insert quà tặng mẫu
INSERT INTO QuaTang (TenQuaTang, MoTa, HinhAnh, DiemCanDoi, SoLuongTon, TrangThai) VALUES
('Mặt nạ Banobagi Stem Cell Vitamin cấp ẩm, dưỡng sáng da, nâng tone', 
 'Mặt nạ dưỡng da cao cấp từ Hàn Quốc, giúp cấp ẩm và dưỡng sáng da hiệu quả', 
 '/images/mask-yellow.jpg', 1500, 0, 1),

('Mặt nạ Banobagi Stem Cell Vitamin Mask Whitening And Tighten Pores', 
 'Mặt nạ làm trắng và se khít lỗ chân lông, phù hợp mọi loại da', 
 '/images/mask-brown.jpg', 1500, 0, 1),

('Mặt nạ cấp ẩm, dưỡng sáng da cho da mụn Banobagi Stem Cell Vitamin', 
 'Đặc biệt phù hợp cho da mụn và da nhạy cảm', 
 '/images/mask-pink.jpg', 1500, 0, 1),

('Mặt nạ cấp ẩm Banobagi Stem Cell Vitamin Mask dưỡng ẩm sâu', 
 'Cung cấp độ ẩm tối ưu, làm mềm mịn da', 
 '/images/mask-green.jpg', 1500, 0, 1),

('Voucher giảm giá 50.000đ', 
 'Áp dụng cho đơn hàng từ 500.000đ trở lên', 
 '/images/voucher-50k.jpg', 3000, 100, 1),

('Voucher giảm giá 100.000đ', 
 'Áp dụng cho đơn hàng từ 1.000.000đ trở lên', 
 '/images/voucher-100k.jpg', 4500, 50, 1),

('Set 3 mặt nạ Banobagi mix', 
 'Combo 3 mặt nạ các loại khác nhau', 
 '/images/mask-set-3.jpg', 4000, 0, 1),

('Serum dưỡng trắng Vitamin C', 
 'Serum dưỡng trắng cao cấp, làm mờ thâm nám', 
 '/images/serum-vitamin-c.jpg', 6000, 20, 1);

SELECT * FROM DIEMTHUONG;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE LichSuDoiQua;
TRUNCATE TABLE DiemThuong;
SET FOREIGN_KEY_CHECKS = 1;

SELECT * FROM DiemThuong WHERE MaNguoiDung = 74;
SELECT * FROM LichSuDoiQua WHERE MaNguoiDung = 74;

DELIMITER $$
-- ==========================================
-- TRIGGER 1: Sau khi thêm mới giao dịch điểm
-- ==========================================
CREATE TRIGGER trg_after_insert_diemthuong
AFTER INSERT ON DiemThuong
FOR EACH ROW
BEGIN
    DECLARE v_current INT;

    -- Lấy điểm hiện tại của người dùng
    SELECT DiemTichLuy INTO v_current
    FROM NguoiDung
    WHERE MaNguoiDung = NEW.MaNguoiDung
    FOR UPDATE;

    -- Tùy loại giao dịch, cộng hoặc trừ điểm
    IF NEW.LoaiGiaoDich = 'Tich' THEN
        UPDATE NguoiDung
        SET DiemTichLuy = v_current + NEW.SoDiem
        WHERE MaNguoiDung = NEW.MaNguoiDung;
    ELSEIF NEW.LoaiGiaoDich IN ('Tieu', 'HetHan') THEN
        UPDATE NguoiDung
        SET DiemTichLuy = GREATEST(v_current - NEW.SoDiem, 0)
        WHERE MaNguoiDung = NEW.MaNguoiDung;
    END IF;
END$$
-- ==========================================
-- TRIGGER 2: Sau khi xóa giao dịch điểm
-- (Khôi phục lại điểm nếu cần)
-- ==========================================
CREATE TRIGGER trg_after_delete_diemthuong
AFTER DELETE ON DiemThuong
FOR EACH ROW
BEGIN
    DECLARE v_current INT;

    SELECT DiemTichLuy INTO v_current
    FROM NguoiDung
    WHERE MaNguoiDung = OLD.MaNguoiDung
    FOR UPDATE;

    -- Đảo ngược thao tác lúc thêm
    IF OLD.LoaiGiaoDich = 'Tich' THEN
        UPDATE NguoiDung
        SET DiemTichLuy = GREATEST(v_current - OLD.SoDiem, 0)
        WHERE MaNguoiDung = OLD.MaNguoiDung;
    ELSEIF OLD.LoaiGiaoDich IN ('Tieu', 'HetHan') THEN
        UPDATE NguoiDung
        SET DiemTichLuy = v_current + OLD.SoDiem
        WHERE MaNguoiDung = OLD.MaNguoiDung;
    END IF;
END$$

DELIMITER ;

-- Cấu hình event Scheduler 
SET GLOBAL event_scheduler = ON;
-- Kiểm tra trạng thái 
SHOW VARIABLES LIKE 'event_scheduler';

DELIMITER $$
CREATE EVENT IF NOT EXISTS ev_expire_diemthuong
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 HOUR)   -- chạy 1 tiếng sau nửa đêm
DO
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_ma INT;
    DECLARE v_maNguoiDung INT;
    DECLARE v_soDiem INT;

    -- Con trỏ quét các bản ghi hết hạn
    DECLARE cur CURSOR FOR
        SELECT MaDiem, MaNguoiDung, SoDiem
        FROM DiemThuong
        WHERE TrangThai = 'HieuLuc' AND NgayHetHan <= NOW();

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_ma, v_maNguoiDung, v_soDiem;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Trừ điểm khỏi người dùng
        UPDATE NguoiDung
        SET DiemTichLuy = GREATEST(DiemTichLuy - v_soDiem, 0)
        WHERE MaNguoiDung = v_maNguoiDung;

        -- Cập nhật trạng thái điểm
        UPDATE DiemThuong
        SET TrangThai = 'HetHan'
        WHERE MaDiem = v_ma;

        -- Ghi lại lịch sử điểm bị hết hạn (optional)
        INSERT INTO DiemThuong (
            MaNguoiDung, LoaiGiaoDich, SoDiem, SoDiemConLai, MoTa, NgayTao, NgayHetHan, TrangThai
        )
        VALUES (
            v_maNguoiDung, 'HetHan', v_soDiem,
            (SELECT DiemTichLuy FROM NguoiDung WHERE MaNguoiDung = v_maNguoiDung),
            CONCAT('Điểm hết hạn (', v_soDiem, ' điểm)'),
            NOW(), NOW(), 'HetHan'
        );
    END LOOP;

    CLOSE cur;
END$$
DELIMITER ;


-- AFTER UPDATE để xử lý logic trừ điểm khi trạng thái đổi từ "Chờ xử lý" → "Đổi thành công".
DELIMITER $$
CREATE TRIGGER trg_after_update_lichsudoiqua
AFTER UPDATE ON LichSuDoiQua
FOR EACH ROW
BEGIN
    -- Chỉ trừ điểm khi trạng thái thay đổi từ 'Chờ xử lý' sang 'Đổi thành công'
    IF OLD.TrangThai = 'Chờ xử lý' AND NEW.TrangThai = 'Đổi thành công' THEN
        UPDATE NguoiDung
        SET DiemTichLuy = DiemTichLuy - NEW.DiemSuDung
        WHERE MaNguoiDung = NEW.MaNguoiDung;
    END IF;
END$$
DELIMITER ;

INSERT INTO DiemThuong (MaNguoiDung, MaDonHang, LoaiGiaoDich, SoDiem, SoDiemConLai, MoTa, NgayHetHan, TrangThai) VALUES
(74, 12345, 'Tich', 550, 550, 'Đơn hàng VIÊN UỐNG BỔ SUNG VITAMIN VÀ KHOÁNG CHẤT LAB WELL MULTI VITAS 60V', 
 DATE_ADD('2025-07-16 07:41:00', INTERVAL 3 MONTH), 'HieuLuc'),

(74, 12346, 'Tich', 990, 1540, 'Đơn hàng STUGERON 25 25X10 và 6 sản phẩm', 
 DATE_ADD('2025-07-16 07:38:00', INTERVAL 3 MONTH), 'HieuLuc'),

(74, 11234, 'Tich', 450, 1990, 'Đơn hàng BỔ GAN TRƯỜNG PHÚC 3X10 và 1 sản phẩm', 
 DATE_ADD('2024-05-29 15:46:00', INTERVAL 3 MONTH), 'HieuLuc');

-- Insert dữ liệu mẫu lịch sử đổi quà
INSERT INTO LichSuDoiQua (MaNguoiDung, MaQuaTang, DiemSuDung, SoLuong, TenQuaTang, DiemCanDoiLucDoi, TrangThai, NgayDoi) VALUES
(74, 1, 1500, 1, 'Mặt nạ Banobagi Stem Cell Vitamin cấp ẩm, dưỡng sáng da, nâng tone', 1500, 'Đổi thành công', '2024-12-10 10:30:00'),
(74, 2, 1500, 1, 'Mặt nạ Banobagi Stem Cell Vitamin Mask Whitening And Tighten Pores', 1500, 'Chờ xử lý', '2025-01-15 14:20:00');

SELECT * FROM NGUOIDUNG;
UPDATE NguoiDung
SET DiemTichLuy = 0
WHERE MaNguoiDung = 74;

ALTER TABLE LichSuDoiQua 
ADD COLUMN HinhAnh VARCHAR(500) NULL AFTER TenQuaTang;

-- Chức năng tìm kiếm sản phẩm (tên sản phẩm , nhà sản xuất hoặc mô tả ngắn) 
-- Bảng từ khóa tìm kiếm phổ biến (cho "tìm kiếm hàng đầu") 
CREATE TABLE tu_khoa_tim_kiem (
    MaTuKhoa INT AUTO_INCREMENT PRIMARY KEY,
    TuKhoa NVARCHAR(255) NOT NULL UNIQUE,
    SoLanTimKiem INT DEFAULT 1,
    LanTimKiemCuoi DATETIME DEFAULT CURRENT_TIMESTAMP,
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_so_lan (SoLanTimKiem DESC),
    INDEX idx_lan_tim_kiem_cuoi (LanTimKiemCuoi DESC)
) ENGINE=InnoDB;

-- Bảng lịch sử tìm kiếm cá nhân 
CREATE TABLE lich_su_tim_kiem (
    MaLichSu INT AUTO_INCREMENT PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    TuKhoa NVARCHAR(255) NOT NULL,
    ThoiGianTimKiem DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung) ON DELETE CASCADE,
    INDEX idx_nguoi_dung_tg (MaNguoiDung, ThoiGianTimKiem DESC)
) ENGINE=InnoDB;

-- INDEX BỔ SUNG CHO BẢNG SAN_PHAM (tối ưu search)
ALTER TABLE san_pham 
ADD FULLTEXT INDEX ft_ten_mota (TenThuoc, MoTaNgan);

SELECT * FROM NGUOIDUNG;


DELETE FROM san_pham
WHERE masanpham = 1;

INSERT INTO Banner (HinhAnh, LienKet, TrangThai) VALUES
('https://drive.google.com/uc?export=download&id=1afElPqfDu3krhEKHfAbTdoPpaJrkl8qg', '/sanpham/1', 'Hiển thị'),
('https://drive.google.com/uc?export=download&id=1BLJ7zxBKJKkd-0t3DA4kyxCM6nn8UUva', '/sanpham/2', 'Hiển thị'),
('https://drive.google.com/uc?export=download&id=1TZxvUpGNIr8lSgGBhFX02vkxP-GH4s-I', '/sanpham/3', 'Hiển thị'),
('https://drive.google.com/uc?export=download&id=1gy-D2GFUV9lGN-O3cddYf-0H8gjQVeD9', '/sanpham/4', 'Hiển thị');

SELECT * FROM san_pham;
SELECT * FROM danhmucthuoc;
SELECT * FROM donvitinh;

INSERT INTO san_pham 
(MaThuoc, TenThuoc, MoTaNgan, MoTaChiTiet, TrangThaiBan, Gia, NhaSanXuat, MaVach, MaDonVi, HanSuDung, NgayTao, NgayCapNhat) VALUES
-- Danh mục 1: Vitamin & Khoáng chất
('VITC01', 'Vitamin C 500mg', 'Hỗ trợ tăng sức đề kháng', 'Vitamin C 500mg giúp tăng cường miễn dịch, hỗ trợ làm đẹp da.', 1, 120000, 'Công ty Dược ABC', '123456789001', 1, '2026-09-29', NOW(), NOW()),
('VITD01', 'Vitamin D3 1000IU', 'Hỗ trợ xương chắc khỏe', 'Vitamin D3 giúp hấp thụ canxi và bảo vệ xương.', 1, 150000, 'Công ty Dược ABC', '123456789002', 1, '2026-09-29', NOW(), NOW()),
('MIN01', 'Canxi + Magie', 'Hỗ trợ hệ xương', 'Bổ sung Canxi và Magie giúp xương chắc khỏe.', 1, 180000, 'Công ty Dược XYZ', '123456789003', 1, '2026-09-29', NOW(), NOW()),
('VITC02', 'Vitamin C 1000mg', 'Tăng sức đề kháng', 'Viên uống Vitamin C 1000mg hỗ trợ miễn dịch.', 1, 200000, 'Công ty Dược ABC', '123456789004', 1, '2026-09-29', NOW(), NOW()),
('VITB01', 'Vitamin B Complex', 'Hỗ trợ năng lượng', 'Vitamin B Complex giúp chuyển hóa năng lượng và giảm mệt mỏi.', 1, 130000, 'Công ty Dược ABC', '123456789005', 1, '2026-09-29', NOW(), NOW()),
('MIN02', 'Kẽm 50mg', 'Tăng miễn dịch', 'Kẽm giúp tăng cường hệ miễn dịch và hỗ trợ làn da.', 1, 110000, 'Công ty Dược XYZ', '123456789006', 1, '2026-09-29', NOW(), NOW()),

-- Danh mục 2: Hỗ trợ điều trị
('DIAB01', 'Thuốc tiểu đường GlucoX', 'Hỗ trợ ổn định đường huyết', 'Thuốc hỗ trợ kiểm soát đường huyết hiệu quả.', 1, 250000, 'Công ty Dược DEF', '123456789007', 2, '2026-09-29', NOW(), NOW()),
('HBP01', 'Thuốc hạ huyết áp', 'Giúp huyết áp ổn định', 'Sử dụng thuốc theo chỉ định bác sĩ.', 1, 300000, 'Công ty Dược DEF', '123456789008', 2, '2026-09-29', NOW(), NOW()),
('BONE01', 'Thuốc bổ xương khớp', 'Hỗ trợ xương khớp', 'Hỗ trợ giảm đau nhức xương khớp và tăng cường canxi.', 1, 220000, 'Công ty Dược GHI', '123456789009', 2, '2026-09-29', NOW(), NOW()),
('DIAB02', 'Thuốc điều trị tiểu đường GlucoY', 'Hỗ trợ đường huyết', 'Thuốc hỗ trợ kiểm soát đường huyết.', 1, 260000, 'Công ty Dược DEF', '123456789010', 2, '2026-09-29', NOW(), NOW()),

-- Danh mục 3: Thuốc không kê đơn
('PAIN01', 'Thuốc giảm đau Paracetamol', 'Giảm đau, hạ sốt', 'Hỗ trợ giảm đau, hạ sốt nhanh.', 1, 50000, 'Công ty Dược JKL', '123456789011', 1, '2026-09-29', NOW(), NOW()),
('FEVER01', 'Thuốc hạ sốt Ibuprofen', 'Hạ sốt, chống viêm', 'Thuốc giảm sốt, chống viêm hiệu quả.', 1, 55000, 'Công ty Dược JKL', '123456789012', 1, '2026-09-29', NOW(), NOW()),
('COLD01', 'Thuốc ho – cảm cúm', 'Giảm ho, giảm triệu chứng cảm', 'Hỗ trợ giảm ho, nghẹt mũi, hắt hơi.', 1, 60000, 'Công ty Dược MNO', '123456789013', 1, '2026-09-29', NOW(), NOW()),
('PAIN02', 'Thuốc đau bụng Antacid', 'Hỗ trợ tiêu hóa', 'Giảm khó chịu do đầy hơi, khó tiêu.', 1, 45000, 'Công ty Dược JKL', '123456789014', 1, '2026-09-29', NOW(), NOW()),
('COLD02', 'Thuốc cảm cúm VitaminC', 'Giảm triệu chứng cảm cúm', 'Hỗ trợ giảm ho, sốt nhẹ, mệt mỏi.', 1, 65000, 'Công ty Dược MNO', '123456789015', 1, '2026-09-29', NOW(), NOW()),

-- Danh mục 4: Dụng cụ y tế
('THERM01', 'Nhiệt kế điện tử', 'Đo nhiệt độ cơ thể', 'Đo nhiệt độ chính xác, tiện lợi.', 1, 350000, 'Công ty Y Tế PQR', '123456789016', 3, '2028-09-29', NOW(), NOW()),
('BP01', 'Máy đo huyết áp', 'Đo huyết áp nhanh', 'Máy đo huyết áp tự động, chính xác.', 1, 450000, 'Công ty Y Tế PQR', '123456789017', 3, '2028-09-29', NOW(), NOW()),
('MASK01', 'Khẩu trang y tế', 'Bảo vệ hô hấp', 'Khẩu trang kháng khuẩn, thoải mái khi đeo.', 1, 20000, 'Công ty Y Tế PQR', '123456789018', 3, '2028-09-29', NOW(), NOW()),

-- Các sản phẩm còn lại (30 sản phẩm) vẫn áp dụng đơn vị tương tự
('VITC03', 'Vitamin C 250mg', 'Hỗ trợ miễn dịch', 'Giúp tăng sức đề kháng và chống oxy hóa.', 1, 100000, 'Công ty Dược ABC', '123456789019', 1, '2026-09-29', NOW(), NOW()),
('VITD02', 'Vitamin D 500IU', 'Hỗ trợ hấp thụ canxi', 'Vitamin D hỗ trợ xương chắc khỏe.', 1, 120000, 'Công ty Dược ABC', '123456789020', 1, '2026-09-29', NOW(), NOW()),
('MIN03', 'Magie 100mg', 'Hỗ trợ xương khớp', 'Magie giúp cơ và xương hoạt động tốt.', 1, 90000, 'Công ty Dược XYZ', '123456789021', 1, '2026-09-29', NOW(), NOW()),
('DIAB03', 'Thuốc điều trị tiểu đường Zeta', 'Hỗ trợ kiểm soát đường huyết', 'Thuốc hỗ trợ giảm đường huyết.', 1, 270000, 'Công ty Dược DEF', '123456789022', 2, '2026-09-29', NOW(), NOW()),
('HBP02', 'Thuốc hạ huyết áp Beta', 'Ổn định huyết áp', 'Hỗ trợ giảm huyết áp cao.', 1, 310000, 'Công ty Dược DEF', '123456789023', 2, '2026-09-29', NOW(), NOW()),
('PAIN03', 'Thuốc đau đầu Aspirin', 'Giảm đau đầu', 'Hỗ trợ giảm đau đầu và hạ sốt.', 1, 52000, 'Công ty Dược JKL', '123456789024', 1, '2026-09-29', NOW(), NOW()),
('COLD03', 'Thuốc cảm lạnh', 'Giảm triệu chứng cảm lạnh', 'Giảm ho, nghẹt mũi.', 1, 60000, 'Công ty Dược MNO', '123456789025', 1, '2026-09-29', NOW(), NOW()),
('VITB02', 'Vitamin B1', 'Hỗ trợ thần kinh', 'Vitamin B1 giúp tăng cường chức năng thần kinh.', 1, 90000, 'Công ty Dược ABC', '123456789026', 1, '2026-09-29', NOW(), NOW()),
('VITB03', 'Vitamin B2', 'Hỗ trợ trao đổi chất', 'Vitamin B2 giúp chuyển hóa năng lượng.', 1, 95000, 'Công ty Dược ABC', '123456789027', 1, '2026-09-29', NOW(), NOW()),
('MIN04', 'Sắt 50mg', 'Hỗ trợ tạo máu', 'Sắt giúp phòng thiếu máu.', 1, 85000, 'Công ty Dược XYZ', '123456789028', 1, '2026-09-29', NOW(), NOW()),
('THERM02', 'Nhiệt kế thủy ngân', 'Đo nhiệt độ cơ thể', 'Nhiệt kế thủy ngân chính xác, an toàn.', 1, 300000, 'Công ty Y Tế PQR', '123456789029', 3, '2028-09-29', NOW(), NOW()),
('MASK02', 'Khẩu trang N95', 'Bảo vệ hô hấp tối đa', 'Khẩu trang N95 lọc bụi và vi khuẩn.', 1, 40000, 'Công ty Y Tế PQR', '123456789030', 3, '2028-09-29', NOW(), NOW());

select * from sanpham_danhmuc;

-- Bảng sanpham_danhmuc đã chỉnh lại
INSERT INTO sanpham_danhmuc (MaSanPham, MaDanhMuc) VALUES
-- Danh mục cha: Vitamin & Khoáng chất (1)
(4, 1),
(5, 1),
(6, 1),
(7, 1),
(8, 1),
(9, 1),
(27, 1),
(28, 1),
(29, 1),
(30, 1),
(31, 1),

-- Danh mục cha: Hỗ trợ điều trị (2)
(10, 2),
(11, 2),
(12, 2),
(13, 2),
(23, 2),
(26, 2),

-- Danh mục cha: Thuốc không kê đơn (3)
(14, 3),
(15, 3),
(16, 3),
(17, 3),
(18, 3),
(24, 3),
(25, 3),

-- Danh mục cha: Dụng cụ y tế (4)
(19, 4),
(20, 4),
(21, 4),
(32, 4),
(33, 4),

-- Danh mục con: Vitamin C (5)
(4, 5),
(7, 5),
(22, 5),
(10, 5),

-- Danh mục con: Vitamin D (6)
(5, 6),
(23, 6),
(11, 6),

-- Danh mục con: Khoáng chất (7)
(6, 7),
(31, 7),
(12, 7),
(24, 7),

-- Danh mục con: Tiểu đường (8)
(10, 8),
(25, 8),
(13, 8),

-- Danh mục con: Huyết áp cao (9)
(11, 9),
(26, 9),
(14, 9),

-- Danh mục con: Xương khớp (10)
(12, 10),
(15, 10),

-- Danh mục con: Thuốc giảm đau (11)
(14, 11),
(27, 11),

-- Danh mục con: Thuốc hạ sốt (12)
(15, 12),
(17, 12),

-- Danh mục con: Thuốc ho - cảm cúm (13)
(16, 13),
(18, 13),
(28, 13),

-- Danh mục con: Nhiệt kế (14)
(19, 14),
(32, 14),

-- Danh mục con: Máy đo huyết áp (15)
(20, 15),

-- Danh mục con: Khẩu trang (16)
(21, 16),
(33, 16);

SELECT * FROM NGUOIDUNG;

DELETE FROM NGUOIDUNG 
WHERE MANGUOIDUNG = 84;

SELECT * FROM donhang;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM donhang
WHERE MaDonHang IN (49,50,51,55);
SET FOREIGN_KEY_CHECKS = 1;
SELECT * FROM ANHSANPHAM;
DELETE FROM ANHSANPHAM
WHERE MAANH IN (56); 

SELECT * FROM DANHMUCTHUOC;

SELECT * FROM san_pham;

ALTER TABLE NGUOIDUNG 
ADD COLUMN RefreshToken VARCHAR(500) NULL,
ADD COLUMN RefreshTokenExpiry DATETIME NULL;

CREATE INDEX idx_refresh_token ON NguoiDung(RefreshToken);

-- KIỂM TRA 
DESCRIBE NguoiDung;