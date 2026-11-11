plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.hethongbanthuoc"
    compileSdk = 36

    buildFeatures {
        buildConfig = true  // FIX #5: Enable BuildConfig for BASE_URL switching
    }

    defaultConfig {
        applicationId = "com.example.hethongbanthuoc"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/api/\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // FIX #5: Đặt BASE_URL production ở đây khi deploy
            buildConfigField("String", "BASE_URL", "\"https://your-production-domain.com/api/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)

    // FIX #3: Jetpack Security — EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Thư viện DotsIndicator
    implementation("com.tbuonomo:dotsindicator:4.3")
    implementation("com.eftimoff:android-viewpager-transformers:1.0.1@aar")
    implementation("com.airbnb.android:lottie:6.6.0")

    // Các thư viện phục vụ gọi API
    // Retrofit for API calls - RETROFIT THỰC HIỆN CHỨC NĂNG GỌI API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp logging interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    // OkHttp core (explicit version pin)
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    // Gson for JSON parsing - thư viện thực hiện chức ănng chuyển đổi giữa JSON và các đối tượng JAVA
    implementation("com.google.code.gson:gson:2.8.8")

    // FIX #4: Đã xoá jBCrypt — password hashing chỉ được thực hiện ở server.
    // Client không bao giờ hash mật khẩu trước khi gửi đi.
    // implementation("org.mindrot:jbcrypt:0.4")  <-- REMOVED

    // Glide - load ảnh từ URL
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    //SwipeRefreshLayout - load dữ liệu
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation("com.google.android.material:material:1.12.0")

}