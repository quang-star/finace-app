package com.example.personalfinance.fragments.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.personalfinance.adapters.BudgetAdapter;
import com.example.personalfinance.databinding.FragmentBudgetBinding;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.DateUtils;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.viewmodels.BudgetViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private BudgetViewModel viewModel;
    private User currentUser;
    private BudgetAdapter adapter;
    private List<Budget> budgetList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        // Setup RecyclerView
        budgetList = new ArrayList<>();
        adapter = new BudgetAdapter(requireContext(), budgetList);
        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgets.setAdapter(adapter);

        adapter.setOnBudgetClickListener(budget -> {
            Toast.makeText(requireContext(), "Ngân sách: " + budget.getBudgetName(), Toast.LENGTH_SHORT).show();
        });

        binding.btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // FAB & Card to add budget
        binding.cardAddBudget.setOnClickListener(v -> openBudgetSheet());

        binding.fabAdd.setOnClickListener(v -> openBudgetSheet());

        // Register Observers
        observeViewModel();

        // Load data
        loadBudgets();
    }

    private void loadBudgets() {
        viewModel.loadBudgets(currentUser.getUserId());
    }

    private void openBudgetSheet() {
        Calendar now = Calendar.getInstance();
        AddBudgetFragment addFragment = new AddBudgetFragment();
        addFragment.configureBudgetPeriod(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                getBudgetsForMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        );
        addFragment.setOnBudgetSavedListener(this::loadBudgets);
        addFragment.show(getParentFragmentManager(), "AddBudgetFragment");
    }

    private List<Budget> getBudgetsForMonth(int year, int month) {
        List<Budget> monthBudgets = new ArrayList<>();
        for (Budget budget : budgetList) {
            if (isBudgetInMonth(budget, year, month)) {
                monthBudgets.add(budget);
            }
        }
        return monthBudgets;
    }

    private boolean isBudgetInMonth(Budget budget, int year, int month) {
        Date start = parseDate(budget.getStartDate());
        Date end = parseDate(budget.getEndDate());
        if (start == null && end == null) return false;

        Calendar periodStart = Calendar.getInstance();
        periodStart.set(year, month, 1, 0, 0, 0);
        periodStart.set(Calendar.MILLISECOND, 0);

        Calendar periodEnd = Calendar.getInstance();
        periodEnd.set(year, month, periodStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        periodEnd.set(Calendar.MILLISECOND, 999);

        Date budgetStart = start != null ? start : end;
        Date budgetEnd = end != null ? end : start;
        return !budgetStart.after(periodEnd.getTime()) && !budgetEnd.before(periodStart.getTime());
    }

    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return DateUtils.parseApiDate(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void observeViewModel() {
        viewModel.getBudgets().observe(getViewLifecycleOwner(), list -> {
            budgetList.clear();
            if (list != null && !list.isEmpty()) {
                budgetList.addAll(list);
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvBudgets.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvBudgets.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
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
