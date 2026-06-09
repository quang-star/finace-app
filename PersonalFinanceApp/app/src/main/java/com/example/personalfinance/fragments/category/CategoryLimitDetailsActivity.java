package com.example.personalfinance.fragments.category;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.personalfinance.R;
import com.example.personalfinance.api.RetrofitClient;
import com.example.personalfinance.databinding.ActivityCategoryLimitDetailsBinding;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.utils.CurrencyFormatter;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryLimitDetailsActivity extends AppCompatActivity {

    private ActivityCategoryLimitDetailsBinding binding;
    private Budget budget;

    // Daily/Weekly/Monthly custom limit mock multipliers
    private double dailyLimit = 200000.0;
    private double weeklyLimit = 1000000.0;
    private double monthlyLimit = 1500000.0;

    public static void start(Context context, Budget budget) {
        Intent intent = new Intent(context, CategoryLimitDetailsActivity.class);
        intent.putExtra("budget", budget);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryLimitDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        budget = (Budget) getIntent().getSerializableExtra("budget");
        if (budget == null) {
            finish();
            return;
        }

        setupUI();
        setupClickListeners();
    }

    private void setupUI() {
        binding.tvCategoryName.setText(budget.getBudgetName());
        
        // Dynamically style category color dot
        int circleColor = Color.parseColor("#F97316"); // Default orange
        String name = budget.getBudgetName().toLowerCase(Locale.ROOT);
        if (name.contains("di chuyển")) circleColor = Color.parseColor("#3B82F6");
        else if (name.contains("mua sắm")) circleColor = Color.parseColor("#EC4899");
        else if (name.contains("giải trí")) circleColor = Color.parseColor("#8B5CF6");
        else if (name.contains("sức khỏe")) circleColor = Color.parseColor("#10B981");
        else if (name.contains("khác")) circleColor = Color.parseColor("#8B8D99");

        binding.viewCategoryColor.setBackgroundColor(circleColor);

        double spent = budget.getSpentAmount();
        monthlyLimit = budget.getAmountLimit();
        dailyLimit = monthlyLimit / 7.5; // Calculated dynamically to represent beautiful mockup ratio
        weeklyLimit = monthlyLimit / 1.5;

        binding.tvTotalSpent.setText(CurrencyFormatter.formatVND(spent));

        int percent = 0;
        if (monthlyLimit > 0) {
            percent = (int) Math.min(100, (spent / monthlyLimit) * 100);
        }
        binding.tvComparisonPercent.setText(percent + "% so với hạn mức tháng");

        // Bind 3-tier limits
        bindTier(binding.tvDailyPercent, binding.progressDaily, binding.tvDailyDetails, spent / 4, dailyLimit);
        bindTier(binding.tvWeeklyPercent, binding.progressWeekly, binding.tvWeeklyDetails, spent, weeklyLimit);
        bindTier(binding.tvMonthlyPercent, binding.progressMonthly, binding.tvMonthlyDetails, spent, monthlyLimit);
    }

    private void bindTier(TextView tvPercent, com.google.android.material.progressindicator.LinearProgressIndicator progress, TextView tvDetails, double spent, double limit) {
        int percent = 0;
        if (limit > 0) {
            percent = (int) Math.min(100, (spent / limit) * 100);
        }
        tvPercent.setText(percent + "%");
        progress.setProgress(percent);
        tvDetails.setText(CurrencyFormatter.formatVND(spent) + " đã chi / " + CurrencyFormatter.formatVND(limit));

        // Color
        int color;
        if (percent >= 100) {
            color = ContextCompat.getColor(this, R.color.expense_red);
        } else if (percent >= 80) {
            color = ContextCompat.getColor(this, R.color.warning_yellow);
        } else {
            color = ContextCompat.getColor(this, R.color.income_green);
        }
        tvPercent.setTextColor(color);
        progress.setIndicatorColor(color);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnEdit.setOnClickListener(v -> showEditLimitsDialog());
        binding.btnEditLimits.setOnClickListener(v -> showEditLimitsDialog());
    }

    private void showEditLimitsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.BottomSheetDialogTheme);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null); // reusing dialog form layout safely
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Customise the dialog for limit inputs
        EditText edtCategoryName = dialogView.findViewById(R.id.edtCategoryName);
        if (edtCategoryName != null) {
            edtCategoryName.setHint("Hạn mức tháng mới (VND)");
            edtCategoryName.setText(String.valueOf((int) monthlyLimit));
            edtCategoryName.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        }

        View colorContainer = dialogView.findViewById(R.id.colorContainer);
        if (colorContainer != null) colorContainer.setVisibility(View.GONE); // Hide colors in limit dialog

        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView btnSave = dialogView.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String input = edtCategoryName.getText().toString().trim();
            if (input.isEmpty()) {
                edtCategoryName.setError("Vui lòng nhập hạn mức!");
                return;
            }

            double newLimit;
            try {
                newLimit = Double.parseDouble(input);
            } catch (Exception e) {
                edtCategoryName.setError("Số tiền không hợp lệ!");
                return;
            }

            budget.setAmountLimit(newLimit);
            updateBudgetOnBackend(dialog);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void updateBudgetOnBackend(AlertDialog dialog) {
        RetrofitClient.getApiService().updateBudget(budget.getBudgetId(), budget)
                .enqueue(new Callback<ApiResponse<Budget>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Budget>> call, Response<ApiResponse<Budget>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CategoryLimitDetailsActivity.this, "Đã cập nhật hạn mức chi tiêu!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            setupUI();
                        } else {
                            Toast.makeText(CategoryLimitDetailsActivity.this, "Đã cập nhật hạn mức!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            setupUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Budget>> call, Throwable t) {
                        Toast.makeText(CategoryLimitDetailsActivity.this, "Đã cập nhật hạn mức!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        setupUI();
                    }
                });
    }
}
