package com.example.hethongbanthuoc.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    //  KHAI BÁO BIẾN TOÀN CỤC
    private static final String PREFS_NAME = "my_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_ROLE = "role";
    private static final String KEY_USER_ID = "user_id";

    // NHÓM 1: QUẢN LÝ TOKEN (Lưu / Lấy token đăng nhập)

    /*
     * Hàm lưu token sau khi đăng nhập thành công.
     * Token này được BE trả về sau khi xác thực.
     */
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    /*
     * Hàm lấy token để sử dụng trong các request hoặc auto login.
     */
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_TOKEN, null);
    }

    // NHÓM 2: QUẢN LÝ ID NGƯỜI DÙNG

    /*
     * Lưu mã người dùng (UserID) sau khi đăng nhập thành công.
     * Dùng để truy xuất thông tin người dùng trong toàn hệ thống.
     */
    public static void saveUserId(Context context, int userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    /*
     * Lấy mã người dùng trong các Activity khác.
     * @return ID người dùng, mặc định -1 nếu chưa có.
     */
    public static int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // NHÓM 3: QUẢN LÝ VAI TRÒ NGƯỜI DÙNG (ROLE)

    /*
     * Lưu vai trò người dùng (ADMIN / USER) để phục vụ điều hướng phân quyền.
     */
    public static void saveRole(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    /*
     * Lấy vai trò người dùng đã lưu trong phiên làm việc.
     */
    public static String getRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ROLE, "");
    }

    /*
     * Xóa vai trò khi người dùng đăng xuất hoặc thay đổi tài khoản.
     */
    public static void clearRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ROLE).apply();
    }

    // NHÓM 4: QUẢN LÝ CHẾ ĐỘ “NHỚ TÔI” (REMEMBER ME)
    /*
     * Lưu trạng thái “Remember Me” và thông tin đăng nhập tương ứng.
     * Nếu người dùng tick RememberMe → lưu email + password.
     * Nếu không → xóa thông tin này khỏi SharedPreferences.
     */
    public static void saveRememberMe(Context context, boolean remember, String email, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        if (remember) {
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
        }
        editor.apply();
    }

    /*
     * Kiểm tra người dùng có bật RememberMe hay không.
     */
    public static boolean getRememberMe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    /*
     * Lấy email đã lưu khi RememberMe bật.
     */
    public static String getSavedEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, "");
    }

    /*
     * Lấy mật khẩu đã lưu khi RememberMe bật.
     */
    public static String getSavedPassword(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PASSWORD, "");
    }

    // NHÓM 5: ĐĂNG XUẤT VÀ XÓA DỮ LIỆU PHIÊN

    /*
     * Thực hiện đăng xuất:
     *  - Xóa token và vai trò hiện tại.
     *  - Nếu không bật RememberMe → xóa luôn email và password.
     */
    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean remember = prefs.getBoolean(KEY_REMEMBER_ME, false);
        SharedPreferences.Editor editor = prefs.edit();

        // Xóa token và role khi logout
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_ROLE);

        // Nếu không tick RememberMe thì xóa luôn email + password
        if (!remember) {
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
        }

        editor.apply();
    }

    /*
     * Xóa hoàn toàn dữ liệu phiên (token, role, remember, email, password, userId).
     * Dùng trong trường hợp cần reset toàn bộ thông tin người dùng.
     */
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_REMEMBER_ME)
                .remove(KEY_EMAIL)
                .remove(KEY_PASSWORD)
                .remove(KEY_ROLE)
                .remove(KEY_USER_ID)
                .apply();
    }

    // NHÓM 6: RESET TOÀN BỘ PHIÊN ĐĂNG NHẬP (THÊM MỚI)
    /*
     * clearSession(): Dùng khi token hết hạn hoặc bị từ chối.
     * → Xóa toàn bộ dữ liệu người dùng: token, email, password, role, userId, RememberMe.
     */
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();  // Xóa sạch toàn bộ dữ liệu trong SharedPreferences
        editor.apply();
    }
}
