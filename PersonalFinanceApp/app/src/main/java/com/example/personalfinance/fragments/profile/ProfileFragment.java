package com.example.personalfinance.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.personalfinance.activities.LoginActivity;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.FragmentProfileBinding;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.fragments.category.CategoryListFragment;
import com.example.personalfinance.fragments.account.AccountFragment;
import com.example.personalfinance.fragments.budget.BudgetFragment;
import com.example.personalfinance.viewmodels.HomeViewModel;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private HomeViewModel homeViewModel;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        // Initialize HomeViewModel to fetch monthly stats
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Set User details
        binding.tvUserName.setText(currentUser.getFullName());
        binding.tvUserEmail.setText(currentUser.getEmail());

        // Set dynamic date placeholder based on registration if any, or static current month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        binding.tvUserDate.setText("📅 thg " + month + " " + year);

        // Setup Option Click Listeners
        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng chỉnh sửa thông tin đang phát triển!", Toast.LENGTH_SHORT).show();
        });

        binding.btnShareProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đang sao chép liên kết chia sẻ hồ sơ!", Toast.LENGTH_SHORT).show();
        });

        binding.cardUpgrade.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Cảm ơn bạn đã nâng cấp lên bản Premium cao cấp!", Toast.LENGTH_LONG).show();
        });

        binding.btnUpgradeAction.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Cảm ơn bạn đã nâng cấp lên bản Premium cao cấp!", Toast.LENGTH_LONG).show();
        });

        binding.btnAppleId.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tài khoản Apple ID: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
        });



        binding.btnCategories.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CategoryListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Hide multi-wallet management
        binding.btnManageAccounts.setVisibility(View.GONE);

        binding.btnManageBudgets.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new BudgetFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnLanguage.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Ngôn ngữ hiện tại: Tiếng Việt 🇻🇳", Toast.LENGTH_SHORT).show();
        });

        binding.btnTheme.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Giao diện hiện tại: Tối 🌙", Toast.LENGTH_SHORT).show();
        });

        binding.btnCurrency.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tiền tệ hiện tại: VND 🇻🇳", Toast.LENGTH_SHORT).show();
        });



        // Double logout hooks to be perfectly safe
        binding.btnLogoutRow.setOnClickListener(v -> handleLogout());
        binding.btnLogout.setOnClickListener(v -> handleLogout());

        // Register Observers for overview metrics
        observeViewModel();

        // Load data
        loadMetrics(month, year);
    }

    private void loadMetrics(int month, int year) {
        homeViewModel.fetchDashboardData(currentUser.getUserId(), month, year);
    }

    private void observeViewModel() {
        // Observe monthly report totals
        homeViewModel.getMonthlyReport().observe(getViewLifecycleOwner(), report -> {
            if (report != null) {
                binding.tvTotalIncome.setText(CurrencyFormatter.formatVND(report.getTotalIncome()));
                binding.tvTotalExpense.setText(CurrencyFormatter.formatVND(report.getTotalExpense()));
                binding.tvNetAmount.setText(CurrencyFormatter.formatVND(report.getNetAmount()));

                // Style the balance amount based on positive/negative
                if (report.getNetAmount() < 0) {
                    binding.tvNetAmount.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_error));
                } else {
                    binding.tvNetAmount.setTextColor(getResources().getColor(com.example.personalfinance.R.color.income_green));
                }
            }
        });

        // Observe monthly transaction list to calculate count
        homeViewModel.getMonthlyTransactions().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                binding.tvTransactionCount.setText(String.valueOf(list.size()));
            } else {
                binding.tvTransactionCount.setText("0");
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        SharedPrefManager.getInstance(requireContext()).clear();

        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
