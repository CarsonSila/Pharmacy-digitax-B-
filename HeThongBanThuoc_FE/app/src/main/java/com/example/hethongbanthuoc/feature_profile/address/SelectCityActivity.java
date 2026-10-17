package com.example.hethongbanthuoc.feature_profile.address;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_profile.models.Model_District;
import com.example.hethongbanthuoc.feature_profile.models.Model_Province;
import com.example.hethongbanthuoc.feature_profile.models.Model_Ward;
import com.example.hethongbanthuoc.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectCityActivity extends AppCompatActivity {

    private static final String TAG = "SelectCityActivity";

    private ImageView btnBack;
    private LinearLayout btnSelectProvince, btnSelectDistrict, btnSelectWard;
    private TextView tvTitle, tvListHeader, tvProvince, tvDistrict, tvWard;
    private View iconProvince, iconDistrict, iconWard;
    private EditText edtSearch;
    private RecyclerView recyclerView;

    private List<Model_Province> provinces = new ArrayList<>();
    private List<Model_District> districts = new ArrayList<>();
    private List<Model_Ward> wards = new ArrayList<>();

    private Model_Province selectedProvince;
    private Model_District selectedDistrict;

    private enum Step { PROVINCE, DISTRICT, WARD }
    private Step currentStep = Step.PROVINCE;

    // Biến để tránh vòng lặp TextWatcher
    private boolean isResettingSearch = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        initViews();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupBackButton();
        setupSystemBackButton();
        loadJSONData();
        showProvinces();
        setupSearchFunction();
        setupSelectionButtons();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSelectProvince = findViewById(R.id.btnSelectProvince);
        btnSelectDistrict = findViewById(R.id.btnSelectDistrict);
        btnSelectWard = findViewById(R.id.btnSelectWard);
        tvTitle = findViewById(R.id.tvTitle);
        tvListHeader = findViewById(R.id.tvListHeader);
        tvProvince = findViewById(R.id.tvProvince);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvWard = findViewById(R.id.tvWard);
        iconProvince = findViewById(R.id.iconProvince);
        iconDistrict = findViewById(R.id.iconDistrict);
        iconWard = findViewById(R.id.iconWard);
        edtSearch = findViewById(R.id.edtSearch);
        recyclerView = findViewById(R.id.recyclerViewLocations);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SelectCityActivity.this, AddAndressActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupSystemBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentStep == Step.DISTRICT) showProvinces();
                else if (currentStep == Step.WARD) showDistricts(selectedProvince);
                else finish();
            }
        });
    }

    private void loadJSONData() {
        Log.d(TAG, "loadJSONData: Starting to load JSON files");
        try {
            provinces = loadListFromAssets("tinh_tp.json", Model_Province.class);
            districts = loadListFromAssets("quan_huyen.json", Model_District.class);
            wards = loadListFromAssets("xa_phuong.json", Model_Ward.class);

            Log.d(TAG, "Loaded " + provinces.size() + " provinces");
            Log.d(TAG, "Loaded " + districts.size() + " districts");
            Log.d(TAG, "Loaded " + wards.size() + " wards");
        } catch (Exception e) {
            Log.e(TAG, "Error loading JSON", e);
        }
    }

    private <T> List<T> loadListFromAssets(String fileName, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            InputStream is = getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            is.close();

            String jsonString = sb.toString().trim();
            Gson gson = new Gson();

            if (jsonString.startsWith("{")) {
                JsonObject obj = gson.fromJson(jsonString, JsonObject.class);
                for (String key : obj.keySet()) {
                    JsonElement element = obj.get(key);
                    list.add(gson.fromJson(element, clazz));
                }
            } else if (jsonString.startsWith("[")) {
                list = gson.fromJson(jsonString,
                        com.google.gson.reflect.TypeToken.getParameterized(List.class, clazz).getType());
            }

            Log.d(TAG, "Loaded " + fileName + " with " + list.size() + " items");
        } catch (Exception e) {
            Log.e(TAG, "Error reading " + fileName, e);
        }
        return list;
    }

    private void showProvinces() {
        currentStep = Step.PROVINCE;
        tvListHeader.setText("Tỉnh / Thành phố");
        btnSelectDistrict.setVisibility(View.GONE);
        btnSelectWard.setVisibility(View.GONE);
        selectedDistrict = null;
        tvDistrict.setText("Chọn Quận/Huyện");
        tvWard.setText("Chọn Phường/Xã");

        resetSearchEditText();

        updateSelectionUI();

        List<String> names = provinces.stream()
                .map(p -> p.name_with_type)
                .collect(Collectors.toList());

        setAdapter(names, name -> {
            selectedProvince = provinces.stream()
                    .filter(p -> p.name_with_type.equals(name))
                    .findFirst()
                    .orElse(null);

            if (selectedProvince != null) {
                tvProvince.setText(name);
                btnSelectDistrict.setVisibility(View.VISIBLE);
                updateSelectionUI();
                showDistricts(selectedProvince);
            }
        });
    }

    private void showDistricts(Model_Province province) {
        currentStep = Step.DISTRICT;
        tvListHeader.setText("Quận / Huyện");
        btnSelectWard.setVisibility(View.GONE);
        tvWard.setText("Chọn Phường/Xã");

        resetSearchEditText();
        updateSelectionUI();

        List<Model_District> filtered = districts.stream()
                .filter(d -> d.parent_code.equals(province.code))
                .collect(Collectors.toList());

        List<String> names = filtered.stream()
                .map(d -> d.name_with_type)
                .collect(Collectors.toList());

        setAdapter(names, name -> {
            selectedDistrict = filtered.stream()
                    .filter(d -> d.name_with_type.equals(name))
                    .findFirst()
                    .orElse(null);

            if (selectedDistrict != null) {
                tvDistrict.setText(name);
                btnSelectWard.setVisibility(View.VISIBLE);
                updateSelectionUI();
                showWards(selectedDistrict);
            }
        });
    }

    private void showWards(Model_District district) {
        currentStep = Step.WARD;
        tvListHeader.setText("Phường / Xã");

        resetSearchEditText();
        updateSelectionUI();

        List<Model_Ward> filtered = wards.stream()
                .filter(w -> w.parent_code.equals(district.code))
                .collect(Collectors.toList());

        List<String> names = filtered.stream()
                .map(w -> w.name_with_type)
                .collect(Collectors.toList());

        setAdapter(names, name -> {
            Model_Ward selectedWard = filtered.stream()
                    .filter(w -> w.name_with_type.equals(name))
                    .findFirst()
                    .orElse(null);

            if (selectedWard != null) {
                tvWard.setText(name);
                updateSelectionUI();

                String fullAddress = selectedWard.name_with_type + ", "
                        + district.name_with_type + ", "
                        + selectedProvince.name_with_type;

                Intent result = new Intent();
                result.putExtra("selected_address", fullAddress);
                result.putExtra("province", selectedProvince.name_with_type);
                result.putExtra("district", district.name_with_type);
                result.putExtra("ward", selectedWard.name_with_type);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    private void resetSearchEditText() {
        isResettingSearch = true;
        edtSearch.setText("");
        isResettingSearch = false;
    }

    private void updateSelectionUI() {
        iconProvince.setBackgroundResource(R.drawable.ic_radio_selected);
        iconDistrict.setBackgroundResource(R.drawable.ic_radio_selected);
        iconWard.setBackgroundResource(R.drawable.ic_radio_selected);
    }

    private void setupSearchFunction() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isResettingSearch) filterList(s.toString());
            }
        });
    }

    private void setupSelectionButtons() {
        btnSelectProvince.setOnClickListener(v -> { if (currentStep != Step.PROVINCE) showProvinces(); });
        btnSelectDistrict.setOnClickListener(v -> { if (currentStep != Step.DISTRICT && selectedProvince != null) showDistricts(selectedProvince); });
        btnSelectWard.setOnClickListener(v -> { if (currentStep != Step.WARD && selectedDistrict != null) showWards(selectedDistrict); });
    }

    private void setAdapter(List<String> list, Adapter_Location.OnItemClickListener listener) {
        Adapter_Location adapter = new Adapter_Location(list, listener);
        recyclerView.setAdapter(adapter);
    }

    private void filterList(String keyword) {
        if (keyword.trim().isEmpty()) {
            if (currentStep == Step.PROVINCE) showProvinces();
            else if (currentStep == Step.DISTRICT && selectedProvince != null) showDistricts(selectedProvince);
            else if (currentStep == Step.WARD && selectedDistrict != null) showWards(selectedDistrict);
            return;
        }

        List<String> filtered = new ArrayList<>();
        Adapter_Location.OnItemClickListener listener = null;

        if (currentStep == Step.PROVINCE) {
            filtered = provinces.stream()
                    .map(p -> p.name_with_type)
                    .filter(n -> n.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());

            listener = name -> {
                selectedProvince = provinces.stream()
                        .filter(p -> p.name_with_type.equals(name))
                        .findFirst()
                        .orElse(null);
                if (selectedProvince != null) {
                    tvProvince.setText(name);
                    btnSelectDistrict.setVisibility(View.VISIBLE);
                    updateSelectionUI();
                    showDistricts(selectedProvince);
                }
            };
        } else if (currentStep == Step.DISTRICT && selectedProvince != null) {
            filtered = districts.stream()
                    .filter(d -> d.parent_code.equals(selectedProvince.code))
                    .map(d -> d.name_with_type)
                    .filter(n -> n.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());

            listener = name -> {
                selectedDistrict = districts.stream()
                        .filter(d -> d.name_with_type.equals(name) && d.parent_code.equals(selectedProvince.code))
                        .findFirst()
                        .orElse(null);
                if (selectedDistrict != null) {
                    tvDistrict.setText(name);
                    btnSelectWard.setVisibility(View.VISIBLE);
                    updateSelectionUI();
                    showWards(selectedDistrict);
                }
            };
        } else if (currentStep == Step.WARD && selectedDistrict != null) {
            filtered = wards.stream()
                    .filter(w -> w.parent_code.equals(selectedDistrict.code))
                    .map(w -> w.name_with_type)
                    .filter(n -> n.toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());

            listener = name -> {
                Model_Ward selectedWard = wards.stream()
                        .filter(w -> w.name_with_type.equals(name) && w.parent_code.equals(selectedDistrict.code))
                        .findFirst()
                        .orElse(null);
                if (selectedWard != null) {
                    tvWard.setText(name);
                    updateSelectionUI();

                    String fullAddress = selectedWard.name_with_type + ", "
                            + selectedDistrict.name_with_type + ", "
                            + selectedProvince.name_with_type;

                    Intent result = new Intent();
                    result.putExtra("selected_address", fullAddress);
                    result.putExtra("province", selectedProvince.name_with_type);
                    result.putExtra("district", selectedDistrict.name_with_type);
                    result.putExtra("ward", selectedWard.name_with_type);
                    setResult(RESULT_OK, result);
                    finish();
                }
            };
        }

        setAdapter(filtered, listener);
    }
}
