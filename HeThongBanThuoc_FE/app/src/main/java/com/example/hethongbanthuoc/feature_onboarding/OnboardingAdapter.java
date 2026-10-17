package com.example.hethongbanthuoc.feature_onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.hethongbanthuoc.feature_onboarding.models.Model_Onboarding;
import com.example.hethongbanthuoc.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<Model_Onboarding> onboardingItems;

    public OnboardingAdapter(List<Model_Onboarding> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_onboarding,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final LottieAnimationView animationView;
        private final TextView textTitle, textDescription;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            animationView = itemView.findViewById(R.id.animationView);
            textTitle = itemView.findViewById(R.id.txtTitle);
            textDescription = itemView.findViewById(R.id.txtDescription);
        }

        void bind(Model_Onboarding item) {
            animationView.setAnimation(item.getImageResId()); // JSON trong res/raw
            animationView.playAnimation(); // chạy tự động
            textTitle.setText(item.getTitle());
            textDescription.setText(item.getDescription());
        }
    }
}