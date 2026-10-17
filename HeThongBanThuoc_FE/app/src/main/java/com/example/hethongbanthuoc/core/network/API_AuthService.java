package com.example.hethongbanthuoc.core.network;

import java.util.List;
import java.util.Map;

import com.example.hethongbanthuoc.feature_home.models.Model_Banner;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHang;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHangRequest;
import com.example.hethongbanthuoc.core.models.DanhMucSanPhamResponse;
import com.example.hethongbanthuoc.feature_profile.models.Model_DiaChi;
import com.example.hethongbanthuoc.core.models.GeneralResponse;
import com.example.hethongbanthuoc.feature_notification.models.model_HasNewResponse;
import com.example.hethongbanthuoc.core.models.StatisticsResponse;
import com.example.hethongbanthuoc.feature_notification.models.model_ThongBaoDetailResponse;
import com.example.hethongbanthuoc.feature_notification.models.model_ThongBaoListResponse;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;
import com.example.hethongbanthuoc.core.models.Model_User;

import com.example.hethongbanthuoc.feature_chat.models.model_ChatResponse;
import com.example.hethongbanthuoc.feature_chat.models.model_ChatSessionResponse;
import com.example.hethongbanthuoc.feature_chat.models.model_ChatHistoryResponse;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_ChiTietDonHangRequest;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_DonHangRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface API_AuthService {

    // 1. API - Đăng ký tài khoản
    @POST("auth/register")
    Call<Map<String, String>> register(@Body Map<String, String> registerRequest);

    // 2. API - Đăng nhập tài khoản
    @POST("auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    // 3. API - Quên mật khẩu - Gửi OTP
    @POST("auth/forgot-password")
    Call<Map<String, String>> forgotPassword(@Body Map<String, String> request);

    // 4. API - Xác thực OTP
    @POST("auth/verify-otp")
    Call<Map<String, String>> verifyOtp(@Body Map<String, String> request);

    // 5. API - Reset mật khẩu bằng OTP
    @POST("auth/reset-password")
    Call<Map<String, String>> resetPassword(@Body Map<String, String> request);

    // 6. API - đổi mật khẩu mới
    @POST("auth/change-password")
    Call<Map<String, String>> changePassword(@Body Map<String, String> request);

    // 7. API - Lấy danh sách banner hiển thị
    @GET("banner")
    Call<List<Model_Banner>> getBanners();

    // 8. API - Lấy danh sách sản phẩm bán chạy (limit số lượng sp trả về)
    @GET("sanpham/best-sellers")
    Call<List<Model_Product>> getBestSellers(@Query("limit") int limit);

    // 9. API - Lấy danh mục nổi bật (trang chủ)
    @GET("danhmuc/noibat")
    Call<List<Model_Category>> getDanhMucNoiBat(@Query("limit") int limit, @Query("minSanPham") int minSanPham
    );

    // 10. API - Lấy tất cả danh mục
    @GET("danhmuc/all")
    Call<List<Model_Category>> getAllDanhMuc();

    // 11. API - Lấy tất cả sản phẩm
    @GET("sanpham")
    Call<List<Model_Product>> getAllProducts();

    // 12. API - Lấy profile ( họ tên và điểm tích lũy ) của người dùng
    @GET("user/profile")
    Call<Model_User> getProfile(@Header("Authorization") String token);

    // 13. API - Cập nhật profile người dùng
    @PUT("user/profile")
    Call<Model_User> updateProfile(@Header("Authorization") String token, @Body Model_User user);

    // 14. API - Lấy danh sách địa chỉ của người dùng
    @GET("user/diachi")
    Call<List<Model_DiaChi>> getDanhSachDiaChi(@Header("Authorization") String token);

    // 15. API - Thêm địa chỉ mới
    @POST("user/diachi")
    Call<Map<String, String>> themDiaChi(@Header("Authorization") String token, @Body Model_DiaChi diaChi);

    // 16. API - Cập nhật địa chỉ
    @PUT("user/diachi")
    Call<Map<String, String>> capNhatDiaChi(@Header("Authorization") String token, @Body Model_DiaChi diaChi);

    // 17. API - Xóa địa chỉ theo ID
    @DELETE("user/diachi/{id}")
    Call<Map<String, String>> xoaDiaChi(@Header("Authorization") String token, @Path("id") int id);

    // 18. API - Đặt địa chỉ mặc định
    @PUT("user/diachi/{id}/default")
    Call<Map<String, String>> datDiaChiMacDinh(@Header("Authorization") String token, @Path("id") int id);

    // 19. API - Kiểm tra độ mạnh mật khẩu
    @POST("auth/check-password-strength")
    Call<Map<String, Object>> checkPasswordStrength(@Body Map<String, String> request);

    // 20. API - Gửi tin nhắn tới AI và nhận phản hồi
    @POST("user/chat/send")
    Call<model_ChatResponse> sendMessage(@Header("Authorization") String token, @Body Map<String, String> request
    );

    // 21. API - Gửi tin nhắn và stream phản hồi AI theo thời gian thực (SSE)
    @Streaming
    @POST("user/chat/stream")
    Call<ResponseBody> streamChat(@Header("Authorization") String token, @Body Map<String, String> request
    );

    // 22. API - Lấy phiên chat hiện tại (đang mở) hoặc tạo mới
    @GET("user/chat/current-session")
    Call<model_ChatHistoryResponse> getCurrentSession(@Header("Authorization") String token
    );

    // 23. API - Lấy danh sách tất cả các phiên tư vấn
    @GET("user/chat/sessions")
    Call<model_ChatSessionResponse> getAllSessions(@Header("Authorization") String token
    );

    // 24. API - Lấy lịch sử tin nhắn của một phiên cụ thể
    @GET("user/chat/history/{maPhienTuVan}")
    Call<model_ChatHistoryResponse> getChatHistory(@Header("Authorization") String token, @Path("maPhienTuVan") int maPhienTuVan, @Query("limit") int limit
    );

    // 25. API - Kết thúc phiên tư vấn
    @PUT("user/chat/end/{maPhienTuVan}")
    Call<Map<String, String>> endChatSession(@Header("Authorization") String token, @Path("maPhienTuVan") int maPhienTuVan
    );

    // 26. API - Xóa phiên tư vấn
    @DELETE("user/chat/session/{maPhienTuVan}")
    Call<Map<String, String>> deleteChatSession(@Header("Authorization") String token, @Path("maPhienTuVan") int maPhienTuVan
    );

    // 27. API - Tạo phiên tư vấn mới
    @POST("user/chat/new-session")
    Call<Map<String, Object>> createNewChatSession(@Header("Authorization") String token);

    // 28. API - Lấy danh sách thông báo (phân trang)
    @GET("user/thongbao")
    Call<model_ThongBaoListResponse> getThongBaoList(@Header("Authorization") String token, @Query("page") int page, @Query("size") int size
    );

    // 29. API - Đếm số thông báo chưa đọc
    @GET("user/thongbao/unread-count")
    Call<ResponseBody> getUnreadCountRaw(@Header("Authorization") String token);

    // 30. API - Đánh dấu 1 thông báo đã đọc
    @PUT("user/thongbao/{notificationId}/mark-as-read")
    Call<GeneralResponse> markAsRead(@Header("Authorization") String token, @Path("notificationId") int notificationId
    );

    // 31. API -  Đánh dấu tất cả đã đọc
    @PUT("user/thongbao/mark-all-as-read")
    Call<GeneralResponse> markAllAsRead(@Header("Authorization") String token);

    // 32. API -  Lấy thông báo theo loại
    @GET("user/thongbao/loai/{loaiThongBao}")
    Call<model_ThongBaoListResponse> getThongBaoByLoai(@Header("Authorization") String token, @Path("loaiThongBao") String loaiThongBao, @Query("page") int page, @Query("size") int size
    );

    // 33. API -  Lấy chi tiết thông báo
    @GET("user/thongbao/{notificationId}")
    Call<model_ThongBaoDetailResponse> getThongBaoDetail(@Header("Authorization") String token, @Path("notificationId") int notificationId
    );

    // 34. API - Xóa thông báo
    @DELETE("user/thongbao/{notificationId}")
    Call<GeneralResponse> deleteThongBao(@Header("Authorization") String token, @Path("notificationId") int notificationId
    );

    // 35. API - Xóa tất cả thông báo đã đọc
    @DELETE("user/thongbao/delete-all-read")
    Call<GeneralResponse> deleteAllRead(@Header("Authorization") String token);

    // 36. API - Lấy thông báo chưa đọc
    @GET("user/thongbao/unread")
    Call<model_ThongBaoListResponse> getUnreadNotifications(@Header("Authorization") String token);

    // 37. API - Tìm kiếm thông báo
    @GET("user/thongbao/search")
    Call<model_ThongBaoListResponse> searchThongBao(@Header("Authorization") String token, @Query("keyword") String keyword, @Query("page") int page, @Query("size") int size
    );

    // 38. API - Lấy thông báo mới nhất
    @GET("user/thongbao/latest")
    Call<model_ThongBaoListResponse> getLatestThongBao(@Header("Authorization") String token, @Query("limit") int limit
    );

    // 39. API - Kiểm tra có thông báo mới
    @GET("user/thongbao/has-new")
    Call<model_HasNewResponse> hasNewNotifications(@Header("Authorization") String token, @Query("lastCheckTime") String lastCheckTime
    );

    // 40. API - Lấy thống kê
    @GET("user/thongbao/statistics")
    Call<StatisticsResponse> getStatistics(@Header("Authorization") String token);

    // 49. API - Xóa tất cả thông báo
    @DELETE("user/thongbao/delete-all")
    Call<GeneralResponse> deleteAllThongBao(@Header("Authorization") String token);

    // 50. API - Lấy thông báo theo khoảng thời gian
    @GET("user/thongbao/date-range")
    Call<model_ThongBaoListResponse> getThongBaoByDateRange(@Header("Authorization") String token, @Query("startDate") String startDate, @Query("endDate") String endDate
    );

    // 51. API - Lấy số điểm hiện tại của user
    @GET("user/rewards/balance/{userId}")
    Call<Map<String, Object>> getDiemThuongBalance(@Header("Authorization") String token, @Path("userId") int userId
    );

    // 52. API - Lịch sử tích/tiêu điểm
    @GET("user/rewards/history/{userId}")
    Call<Map<String, Object>> getLichSuDiem(@Header("Authorization") String token, @Path("userId") int userId
    );

    // 53. API - Danh sách quà tặng (có filter)
    @GET("user/gifts")
    Call<Map<String, Object>> getDanhSachQuaTang(@Header("Authorization") String token, @Query("diem") Integer diemFilter
    );

    // 54. API - Chi tiết quà tặng
    @GET("user/gifts/{giftId}")
    Call<Map<String, Object>> getChiTietQuaTang(@Header("Authorization") String token, @Path("giftId") int giftId
    );

    // 55. API - Đổi quà
    @POST("user/gifts/redeem")
    Call<Map<String, Object>> doiQuaTang(@Header("Authorization") String token, @Body Map<String, Object> request
    );

    // 56. API - Lịch sử đổi quà
    @GET("user/gifts/history/{userId}")
    Call<Map<String, Object>> getLichSuDoiQua(@Header("Authorization") String token, @Path("userId") int userId
    );

    // 57. API - Health check reward API
    @GET("user/rewards/ping")
    Call<Map<String, Object>> rewardHealthCheck();

    // 58. API - Lấy danh sách đơn hàng theo trạng thái
    @GET("user/donhang")
    Call<List<Model_ChiTietDonHangRequest>> getDanhSachDonHang(
            @Header("Authorization") String token,
            @Query("trangThai") String trangThai
    );

    // 59. API - Thống kê số lượng đơn hàng theo trạng thái
    @GET("user/donhang/thongke")
    Call<Map<String, Integer>> thongKeDonHang(@Header("Authorization") String token
    );

    // 60. API - Lấy danh mục con theo danh mục cha
    @GET("danhmuc/{id}/con")
    Call<DanhMucSanPhamResponse<List<Model_Category>>> getDanhMucCon(@Path("id") Integer id);

    // 61. API - Lấy chi tiết 1 danh mục
    @GET("danhmuc/{id}")
    Call<DanhMucSanPhamResponse<Model_Category>> getDanhMucById(@Path("id") Integer id);

    // 62. API - Lấy sản phẩm theo danh mục (có sort)
    @GET("sanpham/danhmuc/{id}")
    Call<DanhMucSanPhamResponse<List<Model_Product>>> getSanPhamByDanhMuc(@Path("id") Integer id, @Query("sort") String sort  // "ban-chay", "gia-thap", "gia-cao"
    );

    // 63. API - Lấy chi tiết 1 sản phẩm
    @GET("sanpham/{id}")
    Call<DanhMucSanPhamResponse<Model_Product>> getSanPhamById(@Path("id") Integer id);

    // 64. API - Lấy giỏ hàng theo người dùng
    @GET("user/GioHang/by-user/{maNguoiDung}")
    Call<Map<String, Object>> getGioHangTheoNguoiDung(@Header("Authorization") String token, @Path("maNguoiDung") int maNguoiDung
    );

    // 65. API - Lấy chi tiết giỏ hàng theo mã giỏ hàng
    @GET("user/ChiTietGioHang/{maGioHang}")
    Call<List<Model_ChiTietGioHang>> getChiTietGioHang(@Path("maGioHang") int maGioHang);

    // 66. API - Thêm sản phẩm vào giỏ hàng
    @POST("user/ChiTietGioHang/add")
    Call<Map<String, Object>> addChiTietGioHang(@Body Model_ChiTietGioHangRequest request);

    // 67. API - Xóa sản phẩm khỏi giỏ hàng
    @DELETE("user/ChiTietGioHang/delete/{id}")
    Call<ResponseBody> deleteChiTietGioHang(@Path("id") int id);

    // 68. API - Cập nhật chi tiết giỏ hàng (số lượng, ghi chú, ...)
    @PUT("user/ChiTietGioHang/update/{id}")
    Call<Map<String, Object>> updateChiTietGioHang(@Path("id") int id, @Body Map<String, Object> body
    );

    // 69. API - Tạo đơn hàng mới
    @POST("user/donhang/addorder")
    Call<Map<String, Object>> taoDonHang(
            @Header("Authorization") String token,
            @Body Map<String, Object> donHang
    );

    // 70. API - Hủy đơn hàng -> khi ở trạng thái "đang xử lý"
    @PUT("user/donhang/{maDonHang}/huy")
    Call<Map<String, Object>> huyDonHang(
            @Header("Authorization") String token,
            @Path("maDonHang") int maDonHang
    );

    // 71. API - Tìm kiếm sản phẩm (real-time)
    @GET("user/search/products")
    Call<Map<String, Object>> searchProducts(
            @Header("Authorization") String token,
            @Query("keyword") String keyword,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir
    );

    // 72. API - Lấy từ khóa phổ biến (Tra cứu hàng đầu)
    @GET("user/search/trending")
    Call<Map<String, Object>> getTrendingKeywords(@Header("Authorization") String token, @Query("limit") int limit
    );

    // 73. API - Lấy lịch sử tìm kiếm
    @GET("user/search/history")
    Call<Map<String, Object>> getSearchHistory(@Header("Authorization") String token, @Query("limit") int limit
    );

    // 74. API - Xóa 1 lịch sử tìm kiếm
    @DELETE("user/search/history/{maLichSu}")
    Call<Map<String, Object>> deleteSearchHistory(@Header("Authorization") String token, @Path("maLichSu") int maLichSu
    );

    // 75. API - Xóa tất cả lịch sử tìm kiếm
    @DELETE("user/search/history/clear")
    Call<Map<String, Object>> clearAllSearchHistory(@Header("Authorization") String token
    );

    // 76. API - Lấy sản phẩm thay thế
    @GET("user/search/related-products")
    Call<Map<String, Object>> getRelatedProducts(@Header("Authorization") String token, @Query("maSanPham") int maSanPham
    );

    // 77. API - Xác thực token
    @GET("auth/validate")
    Call<ResponseBody> validateToken(@Header("Authorization") String token);

    // 78. API - Lấy chi tiết đầy đủ của 1 đơn hàng
    @GET("user/donhang/{maDonHang}")
    Call<Model_ChiTietDonHangRequest> getChiTietDonHang(@Header("Authorization") String token, @Path("maDonHang") int maDonHang
    );
}