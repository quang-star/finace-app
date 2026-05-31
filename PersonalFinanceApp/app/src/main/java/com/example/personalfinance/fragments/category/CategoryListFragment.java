package com.example.personalfinance.fragments.category;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalfinance.R;
import com.example.personalfinance.databinding.FragmentCategoryListBinding;
import com.example.personalfinance.databinding.ItemCategorySettingBinding;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.AccountViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment {

    private FragmentCategoryListBinding binding;
    private AccountViewModel viewModel;
    private User currentUser;

    private CategoryAdapter adapter;
    private final List<Category> allCategories = new ArrayList<>();
    private final List<Category> filteredCategories = new ArrayList<>();
    private String selectedType = "EXPENSE"; // "EXPENSE" or "INCOME"
    private String selectedColorHex = "#3B82F6"; // Default Blue

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        loadCategories();
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(requireContext(), filteredCategories);
        adapter.setOnCategoryClickListener(cat -> {
            Toast.makeText(requireContext(), "Danh mục: " + cat.getCategoryName(), Toast.LENGTH_SHORT).show();
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnDone.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.tabExpense.setOnClickListener(v -> {
            selectedType = "EXPENSE";
            selectTabVisuals(selectedType);
            filterAndDisplayCategories();
        });

        binding.tabIncome.setOnClickListener(v -> {
            selectedType = "INCOME";
            selectTabVisuals(selectedType);
            filterAndDisplayCategories();
        });

        binding.btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void selectTabVisuals(String type) {
        // Clear all visuals
        binding.tabExpense.setBackground(null);
        binding.tabExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        binding.tabIncome.setBackground(null);
        binding.tabIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        // Highlight selected
        TextView selectedView = "EXPENSE".equals(type) ? binding.tabExpense : binding.tabIncome;
        selectedView.setBackgroundResource(R.drawable.bg_button_rounded);
        selectedView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E2E33")));
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
    }

    private void loadCategories() {
        viewModel.loadCategories(currentUser.getUserId());
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            allCategories.clear();
            if (categories != null) {
                allCategories.addAll(categories);
            }
            filterAndDisplayCategories();
        });

        viewModel.getCategoryCreated().observe(getViewLifecycleOwner(), newCategory -> {
            if (newCategory != null) {
                Toast.makeText(requireContext(), "Đã thêm danh mục: " + newCategory.getCategoryName(), Toast.LENGTH_SHORT).show();
                loadCategories();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndDisplayCategories() {
        filteredCategories.clear();
        for (Category cat : allCategories) {
            if (selectedType.equalsIgnoreCase(cat.getCategoryType())) {
                filteredCategories.add(cat);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.BottomSheetDialogTheme);
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText edtCategoryName = dialogView.findViewById(R.id.edtCategoryName);
        View colorOrange = dialogView.findViewById(R.id.colorOrange);
        View colorBlue = dialogView.findViewById(R.id.colorBlue);
        View colorGreen = dialogView.findViewById(R.id.colorGreen);
        View colorIndigo = dialogView.findViewById(R.id.colorIndigo);
        View colorPink = dialogView.findViewById(R.id.colorPink);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView btnSave = dialogView.findViewById(R.id.btnSave);

        // Initial color selection visual
        selectedColorHex = "#3B82F6"; // Default Blue
        selectColorDot(selectedColorHex, colorOrange, colorBlue, colorGreen, colorIndigo, colorPink);

        colorOrange.setOnClickListener(v -> selectColorDot("#F97316", colorOrange, colorBlue, colorGreen, colorIndigo, colorPink));
        colorBlue.setOnClickListener(v -> selectColorDot("#3B82F6", colorOrange, colorBlue, colorGreen, colorIndigo, colorPink));
        colorGreen.setOnClickListener(v -> selectColorDot("#10B981", colorOrange, colorBlue, colorGreen, colorIndigo, colorPink));
        colorIndigo.setOnClickListener(v -> selectColorDot("#6366F1", colorOrange, colorBlue, colorGreen, colorIndigo, colorPink));
        colorPink.setOnClickListener(v -> selectColorDot("#EC4899", colorOrange, colorBlue, colorGreen, colorIndigo, colorPink));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = edtCategoryName.getText().toString().trim();
            if (name.isEmpty()) {
                edtCategoryName.setError("Tên danh mục không được để trống");
                return;
            }

            Category category = new Category();
            category.setUserId(currentUser.getUserId());
            category.setCategoryName(name);
            category.setCategoryType(selectedType);
            category.setColor(selectedColorHex);
            category.setIcon("ic_transaction");
            category.setIsDefault(false);

            viewModel.createCategory(category);
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void selectColorDot(String colorHex, View orange, View blue, View green, View indigo, View pink) {
        selectedColorHex = colorHex;
        orange.setAlpha(colorHex.equals("#F97316") ? 1.0f : 0.4f);
        blue.setAlpha(colorHex.equals("#3B82F6") ? 1.0f : 0.4f);
        green.setAlpha(colorHex.equals("#10B981") ? 1.0f : 0.4f);
        indigo.setAlpha(colorHex.equals("#6366F1") ? 1.0f : 0.4f);
        pink.setAlpha(colorHex.equals("#EC4899") ? 1.0f : 0.4f);
    }

    private static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private final Context context;
        private final List<Category> list;
        private OnCategoryClickListener listener;

        public interface OnCategoryClickListener {
            void onCategoryClick(Category category);
        }

        public CategoryAdapter(Context context, List<Category> list) {
            this.context = context;
            this.list = list;
        }

        public void setOnCategoryClickListener(OnCategoryClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCategorySettingBinding binding = ItemCategorySettingBinding.inflate(
                    LayoutInflater.from(context), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Category cat = list.get(position);
            holder.binding.tvCategoryName.setText(cat.getCategoryName());

            int colorVal = Color.parseColor("#3B82F6"); // Default Blue
            if (cat.getColor() != null && !cat.getColor().isEmpty()) {
                try {
                    colorVal = Color.parseColor(cat.getColor());
                } catch (Exception ignored) {}
            }
            holder.binding.viewCategoryColor.setBackgroundTintList(ColorStateList.valueOf(colorVal));

            // Icons
            int iconRes = R.drawable.ic_transaction; // Fallback
            String name = cat.getCategoryName().toLowerCase();
            if (name.contains("ăn") || name.contains("uống") || name.contains("cà phê") || name.contains("food") || name.contains("bún") || name.contains("phở")) {
                iconRes = R.drawable.ic_transaction;
            } else if (name.contains("sức khỏe") || name.contains("y tế") || name.contains("bệnh") || name.contains("khám")) {
                iconRes = R.drawable.ic_profile;
            } else if (name.contains("mua sắm") || name.contains("shopping") || name.contains("quần áo") || name.contains("mỹ phẩm")) {
                iconRes = R.drawable.ic_budget;
            } else if (name.contains("di chuyển") || name.contains("xăng") || name.contains("xe") || name.contains("grab") || name.contains("taxi")) {
                iconRes = R.drawable.ic_scan;
            } else if (name.contains("nhà") || name.contains("điện") || name.contains("nước") || name.contains("thuê")) {
                iconRes = R.drawable.ic_home;
            }

            holder.binding.ivCategoryIcon.setImageResource(iconRes);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(cat);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemCategorySettingBinding binding;

            public ViewHolder(ItemCategorySettingBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
