package com.example.personalfinance.fragments.budget;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.personalfinance.R;
import com.example.personalfinance.databinding.FragmentAddBudgetBinding;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.Constants;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.BudgetViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddBudgetFragment extends BottomSheetDialogFragment {

    private static final int MAX_CATEGORY_COUNT = 7;

    private FragmentAddBudgetBinding binding;
    private BudgetViewModel viewModel;
    private User currentUser;
    private final Calendar calendar = Calendar.getInstance();
    private final List<CategoryBudgetRow> categoryRows = new ArrayList<>();
    private final List<Budget> existingBudgets = new ArrayList<>();
    private final DecimalFormat amountFormatter = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.GERMANY));

    private String startDateStr;
    private String endDateStr;
    private boolean updatingTotal;
    private boolean distributingTotal;
    private OnBudgetSavedListener savedListener;

    public interface OnBudgetSavedListener {
        void onBudgetSaved();
    }

    public void setOnBudgetSavedListener(OnBudgetSavedListener listener) {
        this.savedListener = listener;
    }

    public void configureBudgetPeriod(int year, int zeroBasedMonth, List<Budget> budgets) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, zeroBasedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        existingBudgets.clear();
        if (budgets != null) {
            existingBudgets.addAll(budgets);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        startDateStr = formatMonthBoundary(true);
        endDateStr = formatMonthBoundary(false);

        bindInitialUi();
        observeViewModel();
        viewModel.loadCategories(currentUser.getUserId());
    }

    private void bindInitialUi() {
        boolean editMode = !existingBudgets.isEmpty();
        binding.tvSheetTitle.setText(editMode ? R.string.label_update_monthly_budget : R.string.ui_them_ngan_sach_thang);
        binding.btnSave.setText(editMode ? R.string.label_update_budget : R.string.label_save_budget);
        binding.tvMonth.setText(DateUtils.formatMonthTitle(calendar.getTime()));
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> saveBudgets());
        binding.btnReset.setOnClickListener(v -> resetAmounts());
        binding.edtTotalBudget.setHint("0 đ");
        binding.edtTotalBudget.setSelectAllOnFocus(true);
        binding.edtTotalBudget.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                distributeTotalToCategories(parseAmount(binding.edtTotalBudget.getText().toString()));
                return true;
            }
            return false;
        });
        binding.edtTotalBudget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (updatingTotal || distributingTotal) return;
                double total = parseAmount(s.toString());
                setFormattedText(binding.edtTotalBudget, total);
                distributeTotalToCategories(total);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                renderCategoryRows(list);
            }
        });

        viewModel.getBudgetsCreated().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(requireContext(), "Lưu ngân sách thành công!", Toast.LENGTH_SHORT).show();
                if (savedListener != null) savedListener.onBudgetSaved();
                dismiss();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderCategoryRows(List<Category> categories) {
        binding.categoryContainer.removeAllViews();
        categoryRows.clear();

        Map<Integer, Budget> existingBudgetByCategory = new HashMap<>();
        for (Budget budget : existingBudgets) {
            if (budget.getCategoryId() != null) {
                existingBudgetByCategory.put(budget.getCategoryId(), budget);
            }
        }

        for (Category category : categories) {
            if (!Constants.TYPE_EXPENSE.equalsIgnoreCase(category.getCategoryType())) {
                continue;
            }
            if (categoryRows.size() >= MAX_CATEGORY_COUNT) {
                break;
            }
            addCategoryRow(category, existingBudgetByCategory.get(category.getCategoryId()));
        }

        if (categoryRows.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText(R.string.label_add_expense_categories_first);
            emptyView.setTextColor(getColor(R.color.text_secondary));
            emptyView.setTextSize(14);
            binding.categoryContainer.addView(emptyView);
        } else {
            updateTotalFromRows();
        }
    }

    private void addCategoryRow(Category category, Budget existingBudget) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundResource(R.drawable.bg_rounded_card);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dp(8));
        binding.categoryContainer.addView(row, rowParams);

        TextView badge = new TextView(requireContext());
        badge.setGravity(Gravity.CENTER);
        badge.setText(getCategoryInitial(category));
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(14);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setBackground(makeCircleDrawable(category.getColor()));
        row.addView(badge, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView nameView = new TextView(requireContext());
        nameView.setText(category.getCategoryName());
        nameView.setTextColor(getColor(R.color.text_primary));
        nameView.setTextSize(13);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        nameParams.setMargins(dp(12), 0, dp(10), 0);
        row.addView(nameView, nameParams);

        LinearLayout limitsStack = new LinearLayout(requireContext());
        limitsStack.setOrientation(LinearLayout.VERTICAL);
        row.addView(limitsStack, new LinearLayout.LayoutParams(dp(150), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView monthLabel = new TextView(requireContext());
        monthLabel.setText(R.string.label_budget_month);
        monthLabel.setTextColor(getColor(R.color.text_secondary));
        monthLabel.setTextSize(11);
        limitsStack.addView(monthLabel);

        LinearLayout amountBox = new LinearLayout(requireContext());
        amountBox.setOrientation(LinearLayout.HORIZONTAL);
        amountBox.setGravity(Gravity.CENTER_VERTICAL);
        amountBox.setPadding(dp(10), 0, dp(10), 0);
        amountBox.setBackgroundResource(R.drawable.bg_input_field);
        limitsStack.addView(amountBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        EditText amountInput = new EditText(requireContext());
        amountInput.setBackgroundColor(Color.TRANSPARENT);
        amountInput.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        amountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        amountInput.setSelectAllOnFocus(true);
        amountInput.setSingleLine(true);
        amountInput.setTextColor(getColor(R.color.text_primary));
        amountInput.setTextSize(13);
        amountInput.setTypeface(Typeface.DEFAULT_BOLD);
        amountBox.addView(amountInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView currency = new TextView(requireContext());
        currency.setText(" đ");
        currency.setTextColor(getColor(R.color.text_primary));
        currency.setTextSize(13);
        amountBox.addView(currency);

        TextView dailyLabel = new TextView(requireContext());
        dailyLabel.setText(R.string.label_daily_max);
        dailyLabel.setTextColor(getColor(R.color.text_secondary));
        dailyLabel.setTextSize(11);
        LinearLayout.LayoutParams dailyLabelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dailyLabelParams.setMargins(0, dp(6), 0, 0);
        limitsStack.addView(dailyLabel, dailyLabelParams);

        LinearLayout dailyBox = new LinearLayout(requireContext());
        dailyBox.setOrientation(LinearLayout.HORIZONTAL);
        dailyBox.setGravity(Gravity.CENTER_VERTICAL);
        dailyBox.setPadding(dp(10), 0, dp(10), 0);
        dailyBox.setBackgroundResource(R.drawable.bg_input_field);
        limitsStack.addView(dailyBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        EditText dailyLimitInput = new EditText(requireContext());
        dailyLimitInput.setBackgroundColor(Color.TRANSPARENT);
        dailyLimitInput.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        dailyLimitInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        dailyLimitInput.setSelectAllOnFocus(true);
        dailyLimitInput.setSingleLine(true);
        dailyLimitInput.setTextColor(getColor(R.color.text_primary));
        dailyLimitInput.setTextSize(13);
        dailyLimitInput.setTypeface(Typeface.DEFAULT_BOLD);
        dailyBox.addView(dailyLimitInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        TextView dailyCurrency = new TextView(requireContext());
        dailyCurrency.setText(" đ");
        dailyCurrency.setTextColor(getColor(R.color.text_primary));
        dailyCurrency.setTextSize(13);
        dailyBox.addView(dailyCurrency);

        if (existingBudget != null && existingBudget.getAmountLimit() > 0) {
            setFormattedText(amountInput, existingBudget.getAmountLimit());
        }
        if (existingBudget != null && existingBudget.getDailyAmountLimit() > 0) {
            setFormattedText(dailyLimitInput, existingBudget.getDailyAmountLimit());
        }

        CategoryBudgetRow categoryBudgetRow = new CategoryBudgetRow(category, existingBudget, amountInput, dailyLimitInput);
        categoryRows.add(categoryBudgetRow);

        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (distributingTotal) return;
                setFormattedText(amountInput, parseAmount(s.toString()));
                updateTotalFromRows();
            }
        });

        dailyLimitInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                setFormattedText(dailyLimitInput, parseAmount(s.toString()));
            }
        });
    }

    private void distributeTotalToCategories(double total) {
        if (categoryRows.isEmpty()) return;

        distributingTotal = true;
        double perCategory = total / categoryRows.size();
        for (CategoryBudgetRow row : categoryRows) {
            setFormattedText(row.amountInput, perCategory);
        }
        distributingTotal = false;
    }

    private void updateTotalFromRows() {
        double total = 0;
        for (CategoryBudgetRow row : categoryRows) {
            total += parseAmount(row.amountInput.getText().toString());
        }
        updatingTotal = true;
        setFormattedText(binding.edtTotalBudget, total);
        updatingTotal = false;
    }

    private void resetAmounts() {
        distributingTotal = true;
        for (CategoryBudgetRow row : categoryRows) {
            row.amountInput.setText("");
        }
        distributingTotal = false;

        updatingTotal = true;
        binding.edtTotalBudget.setText("");
        updatingTotal = false;
    }

    private void saveBudgets() {
        if (categoryRows.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng thêm danh mục chi tiêu trước", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Budget> budgetsToCreate = new ArrayList<>();
        List<Budget> budgetsToUpdate = new ArrayList<>();
        String monthLabel = DateUtils.formatMonthYear(calendar.getTime());

        for (CategoryBudgetRow row : categoryRows) {
            double amount = parseAmount(row.amountInput.getText().toString());
            if (amount <= 0) continue;

            Budget budget = row.existingBudget != null ? row.existingBudget : new Budget();
            budget.setUserId(currentUser.getUserId());
            budget.setCategoryId(row.category.getCategoryId());
            budget.setBudgetName("Ngân sách " + row.category.getCategoryName() + " " + monthLabel);
            budget.setAmountLimit(amount);
            budget.setDailyAmountLimit(parseAmount(row.dailyLimitInput.getText().toString()));
            budget.setStartDate(startDateStr);
            budget.setEndDate(endDateStr);

            if (row.existingBudget != null) {
                budgetsToUpdate.add(budget);
            } else {
                budgetsToCreate.add(budget);
            }
        }

        if (budgetsToCreate.isEmpty() && budgetsToUpdate.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập ngân sách cho ít nhất một danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);
        viewModel.saveBudgets(budgetsToCreate, budgetsToUpdate);
    }

    private String formatMonthBoundary(boolean firstDay) {
        Calendar selected = (Calendar) calendar.clone();
        selected.set(Calendar.DAY_OF_MONTH, firstDay ? 1 : selected.getActualMaximum(Calendar.DAY_OF_MONTH));
        return DateUtils.formatApiDate(selected.getTime());
    }

    private void setFormattedText(EditText editText, double value) {
        String formatted = value > 0 ? amountFormatter.format(Math.round(value)) : "";
        if (formatted.equals(editText.getText().toString())) return;
        editText.setText(formatted);
        editText.setSelection(editText.getText().length());
    }

    private double parseAmount(String value) {
        if (value == null) return 0;
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getCategoryInitial(Category category) {
        String name = category.getCategoryName();
        if (name == null || name.trim().isEmpty()) return "?";
        return name.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    private GradientDrawable makeCircleDrawable(String colorValue) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(parseColor(colorValue));
        return drawable;
    }

    private int parseColor(String colorValue) {
        if (colorValue == null || colorValue.trim().isEmpty()) {
            return getColor(R.color.primary);
        }
        try {
            return Color.parseColor(colorValue);
        } catch (IllegalArgumentException e) {
            return getColor(R.color.primary);
        }
    }

    private int getColor(int resId) {
        return androidx.core.content.ContextCompat.getColor(requireContext(), resId);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class CategoryBudgetRow {
        final Category category;
        final Budget existingBudget;
        final EditText amountInput;
        final EditText dailyLimitInput;

        CategoryBudgetRow(Category category, Budget existingBudget, EditText amountInput, EditText dailyLimitInput) {
            this.category = category;
            this.existingBudget = existingBudget;
            this.amountInput = amountInput;
            this.dailyLimitInput = dailyLimitInput;
        }
    }
}
