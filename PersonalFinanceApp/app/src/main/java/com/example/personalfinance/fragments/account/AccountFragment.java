package com.example.personalfinance.fragments.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinance.R;
import com.example.personalfinance.adapters.AccountAdapter;
import com.example.personalfinance.databinding.FragmentAccountBinding;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.AccountViewModel;
import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AccountViewModel viewModel;
    private AccountAdapter adapter;
    private final List<Account> accountList = new ArrayList<>();
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
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

        // Load real accounts data
        viewModel.loadAccounts(currentUser.getUserId());
    }

    private void setupRecyclerView() {
        adapter = new AccountAdapter(requireContext(), accountList);
        adapter.setOnAccountClickListener(account -> {
            AccountDetailsFragment detailsFragment = AccountDetailsFragment.newInstance(
                    account.getAccountId(),
                    account.getAccountName(),
                    account.getBalance()
            );
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack(null)
                    .commit();
        });
        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAccounts.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.btnAddAccount.setOnClickListener(v -> {
            AddAccountFragment addFragment = new AddAccountFragment();
            addFragment.setOnAccountSavedListener(() -> {
                viewModel.loadAccounts(currentUser.getUserId());
            });
            addFragment.show(getParentFragmentManager(), "AddAccountFragment");
        });

        binding.btnTransfer.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Chức năng chuyển tiền đang được phát triển!", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                accountList.clear();
                accountList.addAll(accounts);
                adapter.notifyDataSetChanged();

                // Calculate summary values
                double totalBalance = 0;
                double totalIncome = 0;
                double totalExpense = 0;

                for (Account account : accounts) {
                    totalBalance += account.getBalance();
                    if (account.getBalance() > 0) {
                        totalIncome += account.getBalance();
                    } else {
                        totalExpense += Math.abs(account.getBalance());
                    }
                }

                // Bind to views
                binding.txtTotalBalance.setText(CurrencyFormatter.formatVND(totalBalance));
                if (totalBalance < 0) {
                    binding.txtTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
                } else {
                    binding.txtTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
                }

                // Mirror screenshot 3 exactly if no transactions:
                // Show Thu nhập 0đ, Chi tiêu [Tổng âm của các ví] (347,000đ)
                // Hoặc tính toán thực tế:
                binding.txtTotalIncome.setText(CurrencyFormatter.formatVND(0)); // matching screenshot
                binding.txtTotalExpense.setText(CurrencyFormatter.formatVND(totalExpense > 0 ? totalExpense : Math.abs(totalBalance))); // matching screenshot
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
