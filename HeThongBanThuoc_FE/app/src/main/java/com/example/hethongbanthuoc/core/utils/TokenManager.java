package com.example.hethongbanthuoc.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Quản lý token và phiên đăng nhập.
 *
 * FIX #3: Đã xoá lưu mật khẩu plaintext.
 *         "Remember Me" chỉ lưu email (để gợi ý tên đăng nhập), token và refreshToken.
 *         Việc tự động đăng nhập lại thực hiện qua refreshToken, không qua mật khẩu.
 *
 * FIX #3 (nâng cao): Với production, thay SharedPreferences bằng EncryptedSharedPreferences:
 *   MasterKey masterKey = new MasterKey.Builder(context)
 *       .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
 *   SharedPreferences prefs = EncryptedSharedPreferences.create(context, "my_prefs", masterKey,
 *       EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
 *       EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
 */
public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREFS_NAME    = "my_prefs";
    private static final String KEY_TOKEN         = "token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_REMEMBER_ME   = "remember_me";
    private static final String KEY_EMAIL         = "email";
    private static final String KEY_ROLE          = "role";
    private static final String KEY_USER_ID       = "user_id";

    // ─────────────────────────────────────────────
    // NHÓM 1: ACCESS TOKEN
    // ─────────────────────────────────────────────

    public static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, null);
    }

    // ─────────────────────────────────────────────
    // NHÓM 2: REFRESH TOKEN
    // ─────────────────────────────────────────────

    public static void saveRefreshToken(Context context, String refreshToken) {
        getPrefs(context).edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    public static String getRefreshToken(Context context) {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null);
    }

    // ─────────────────────────────────────────────
    // NHÓM 3: USER ID
    // ─────────────────────────────────────────────

    public static void saveUserId(Context context, int userId) {
        getPrefs(context).edit().putInt(KEY_USER_ID, userId).apply();
    }

    /** @return ID người dùng, mặc định -1 nếu chưa có */
    public static int getUserId(Context context) {
        return getPrefs(context).getInt(KEY_USER_ID, -1);
    }

    // ─────────────────────────────────────────────
    // NHÓM 4: ROLE
    // ─────────────────────────────────────────────

    public static void saveRole(Context context, String role) {
        getPrefs(context).edit().putString(KEY_ROLE, role).apply();
    }

    public static String getRole(Context context) {
        return getPrefs(context).getString(KEY_ROLE, "");
    }

    public static void clearRole(Context context) {
        getPrefs(context).edit().remove(KEY_ROLE).apply();
    }

    // ─────────────────────────────────────────────
    // NHÓM 5: REMEMBER ME
    // FIX #3: Chỉ lưu email (để điền sẵn vào form), KHÔNG lưu mật khẩu.
    //         Auto-login sử dụng refreshToken.
    // ─────────────────────────────────────────────

    /**
     * Bật/tắt chế độ "Remember Me".
     * Chỉ lưu email để gợi ý, không lưu mật khẩu.
     */
    public static void saveRememberMe(Context context, boolean remember, String email) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        if (remember && email != null) {
            editor.putString(KEY_EMAIL, email);
        } else {
            editor.remove(KEY_EMAIL);
        }
        editor.apply();
    }

    public static boolean getRememberMe(Context context) {
        return getPrefs(context).getBoolean(KEY_REMEMBER_ME, false);
    }

    public static String getSavedEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, "");
    }

    // ─────────────────────────────────────────────
    // NHÓM 6: ĐĂNG XUẤT
    // ─────────────────────────────────────────────

    /**
     * Đăng xuất bình thường:
     *  - Xóa access token, refresh token, role.
     *  - Nếu không bật RememberMe → xóa luôn email.
     */
    public static void logout(Context context) {
        boolean remember = getRememberMe(context);
        SharedPreferences.Editor editor = getPrefs(context).edit();

        editor.remove(KEY_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_ROLE);
        editor.remove(KEY_USER_ID);

        if (!remember) {
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_REMEMBER_ME);
        }

        editor.apply();
    }

    /**
     * Xóa toàn bộ phiên (token hết hạn, bị kick, v.v.)
     */
    public static void clearSession(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    /**
     * @deprecated Dùng {@link #clearSession(Context)} thay thế.
     */
    @Deprecated
    public static void clearToken(Context context) {
        clearSession(context);
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────────

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
