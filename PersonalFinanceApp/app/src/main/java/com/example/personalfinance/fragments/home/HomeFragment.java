package com.example.personalfinance.fragments.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.personalfinance.R;
import com.example.personalfinance.activities.MainActivity;
import com.example.personalfinance.activities.ScanBillActivity;
import com.example.personalfinance.activities.ScanProductActivity;
import com.example.personalfinance.adapters.CalendarGridAdapter;
import com.example.personalfinance.adapters.HorizontalAccountAdapter;
import com.example.personalfinance.databinding.FragmentHomeBinding;
import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.CalendarDay;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.models.User;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.SharedPrefManager;
import com.example.personalfinance.fragments.account.AddAccountFragment;
import com.example.personalfinance.fragments.account.AccountDetailsFragment;
import com.example.personalfinance.fragments.transaction.AddTransactionFragment;
import com.example.personalfinance.fragments.profile.ProfileFragment;
import com.example.personalfinance.fragments.transaction.DayDetailFragment;
import com.example.personalfinance.viewmodels.AccountViewModel;
import com.example.personalfinance.viewmodels.BudgetViewModel;
import com.example.personalfinance.viewmodels.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private AccountViewModel accountViewModel;
    private BudgetViewModel budgetViewModel;
    private User currentUser;
    
    private CalendarGridAdapter calendarAdapter;
    private HorizontalAccountAdapter horizontalAccountAdapter;
    private final List<Account> accountList = new ArrayList<>();
    private final List<CalendarDay> calendarDays = new ArrayList<>();
    private final Calendar currentCalendar = Calendar.getInstance();
    private boolean isMonthMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        if (currentUser == null) return;

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

        setupDynamicGreeting();
        setupCalendarRecyclerView();
        setupHorizontalWallets();
        setupClickListeners();
        observeViewModel();

        loadDataForSelectedMonth();
        updateTabStyles();
        accountViewModel.loadAccounts(currentUser.getUserId());
        budgetViewModel.loadBudgets(currentUser.getUserId());
    }

    private void setupDynamicGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng ☀️";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều ☀️";
        } else {
            greeting = "Chúc ngủ ngon 🌙";
        }
        binding.tvGreeting.setText(greeting);
        binding.tvUsername.setText(currentUser.getFullName());
    }

    private void setupCalendarRecyclerView() {
        calendarAdapter = new CalendarGridAdapter(requireContext(), calendarDays);
        calendarAdapter.setOnDayClickListener((day, hasTransaction, position) -> {
            calendarAdapter.setSelectedPosition(position);

            // Update currentCalendar's day
            currentCalendar.set(Calendar.DAY_OF_MONTH, day.getDayNumber());
            updateTabStyles();

            // If in Day mode, fetch daily report
            if (!isMonthMode) {
                String dayDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentCalendar.getTime());
                viewModel.fetchDailyReport(currentUser.getUserId(), dayDateStr);
            }

            int month = currentCalendar.get(Calendar.MONTH) + 1;
            String dayStr = day.getDayNumber() < 10 ? "0" + day.getDayNumber() : String.valueOf(day.getDayNumber());
            String monthStr = month < 10 ? "0" + month : String.valueOf(month);

            binding.btnSelectedDayPill.setText("Đã chọn " + dayStr + "/" + monthStr + " ✓");
            binding.btnSelectedDayPill.setVisibility(View.VISIBLE);

            binding.btnSelectedDayPill.setOnClickListener(v -> {
                binding.btnSelectedDayPill.setVisibility(View.GONE);
                calendarAdapter.clearSelection();

                Calendar selectCal = (Calendar) currentCalendar.clone();
                selectCal.set(Calendar.DAY_OF_MONTH, day.getDayNumber());

                String selectedDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectCal.getTime());
                String dayTitle = new SimpleDateFormat("EEEE, 'ngày' d 'thg' M, yyyy", new Locale("vi", "VN")).format(selectCal.getTime());
                if (dayTitle.length() > 0) {
                    dayTitle = dayTitle.substring(0, 1).toUpperCase() + dayTitle.substring(1);
                }

                DayDetailFragment detailFragment = DayDetailFragment.newInstance(selectedDateStr, dayTitle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });
        });

        binding.rvCalendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendarGrid.setAdapter(calendarAdapter);
    }

    private void setupHorizontalWallets() {
        horizontalAccountAdapter = new HorizontalAccountAdapter(requireContext(), accountList);
        horizontalAccountAdapter.setOnAccountClickListener(account -> {
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

        binding.rvHorizontalWallets.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.rvHorizontalWallets.setAdapter(horizontalAccountAdapter);

        binding.btnQuickAddAccount.setOnClickListener(v -> {
            AddAccountFragment addFragment = new AddAccountFragment();
            addFragment.setOnAccountSavedListener(() -> {
                accountViewModel.loadAccounts(currentUser.getUserId());
            });
            addFragment.show(getParentFragmentManager(), "AddAccountFragment");
        });
    }

    private void setupClickListeners() {
        binding.fabAdd.setOnClickListener(v -> showAddOptionsBottomSheet(-1));

        binding.btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadDataForSelectedMonth();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadDataForSelectedMonth();
        });

        binding.btnResetMonth.setOnClickListener(v -> {
            currentCalendar.setTimeInMillis(System.currentTimeMillis());
            loadDataForSelectedMonth();
        });

        binding.ivAvatar.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnDayTab.setOnClickListener(v -> {
            isMonthMode = false;
            updateTabStyles();
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentCalendar.getTime());
            viewModel.fetchDailyReport(currentUser.getUserId(), dateStr);
        });

        binding.btnMonthTab.setOnClickListener(v -> {
            isMonthMode = true;
            updateTabStyles();
            int month = currentCalendar.get(Calendar.MONTH) + 1;
            int year = currentCalendar.get(Calendar.YEAR);
            viewModel.fetchDashboardData(currentUser.getUserId(), month, year);
        });
    }

    private void loadDataForSelectedMonth() {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        
        // Update Title matching CapMoney format exactly: "tháng 4 2026"
        binding.tvMonthTitle.setText("tháng " + month + " " + year);

        if (calendarAdapter != null) {
            calendarAdapter.clearSelection();
        }
        if (binding != null && binding.btnSelectedDayPill != null) {
            binding.btnSelectedDayPill.setVisibility(View.GONE);
        }

        // Show a beautiful, empty/placeholder calendar grid first so that it is visible immediately!
        generateCalendarGrid(new ArrayList<>());
        updateTabStyles();

        viewModel.fetchDashboardData(currentUser.getUserId(), month, year);
        if (!isMonthMode) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentCalendar.getTime());
            viewModel.fetchDailyReport(currentUser.getUserId(), dateStr);
        }
    }

    private void observeViewModel() {
        viewModel.getMonthlyReport().observe(getViewLifecycleOwner(), report -> {
            if (isMonthMode && report != null) {
                binding.tvExpenseAmount.setText(CurrencyFormatter.formatVND(report.getTotalExpense()));
                binding.tvIncomeAmount.setText(CurrencyFormatter.formatVND(report.getTotalIncome()));
                
                // Update green badge "Hôm nay chưa chi tiêu"
                if (report.getTotalExpense() > 0) {
                    binding.tvBadgeText.setText("Hôm nay đã tiêu " + CurrencyFormatter.formatVND(report.getTotalExpense()));
                } else {
                    binding.tvBadgeText.setText("Hôm nay chưa chi tiêu");
                }
            }
        });

        viewModel.getDailyReport().observe(getViewLifecycleOwner(), report -> {
            if (!isMonthMode && report != null) {
                binding.tvExpenseAmount.setText(CurrencyFormatter.formatVND(report.getTotalExpense()));
                binding.tvIncomeAmount.setText(CurrencyFormatter.formatVND(report.getTotalIncome()));
                
                if (report.getTotalExpense() > 0) {
                    binding.tvBadgeText.setText("Hôm nay đã tiêu " + CurrencyFormatter.formatVND(report.getTotalExpense()));
                } else {
                    binding.tvBadgeText.setText("Hôm nay chưa chi tiêu");
                }
            }
        });

        viewModel.getMonthlyTransactions().observe(getViewLifecycleOwner(), transactions -> {
            generateCalendarGrid(transactions);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        accountViewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null) {
                accountList.clear();
                accountList.addAll(accounts);
                horizontalAccountAdapter.notifyDataSetChanged();
            }
        });

        budgetViewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null && !budgets.isEmpty()) {
                double totalLimit = 0;
                double totalSpent = 0;
                for (Budget b : budgets) {
                    totalLimit += b.getAmountLimit();
                    totalSpent += b.getSpentAmount();
                }

                binding.tvBudgetTotal.setText("Hạn mức: " + CurrencyFormatter.formatVND(totalLimit));
                binding.tvBudgetSpent.setText("Đã dùng: " + CurrencyFormatter.formatVND(totalSpent));

                int percent = 0;
                if (totalLimit > 0) {
                    percent = (int) Math.min(100, (totalSpent / totalLimit) * 100);
                }
                binding.tvBudPercent.setText(percent + "%");
                binding.progressBudget.setProgress(percent);
            } else {
                binding.tvBudgetTotal.setText("Hạn mức: 0đ");
                binding.tvBudgetSpent.setText("Đã dùng: 0đ");
                binding.tvBudPercent.setText("0%");
                binding.progressBudget.setProgress(0);
            }
        });
    }

    private void generateCalendarGrid(List<Transaction> transactions) {
        calendarDays.clear();
        calendarAdapter.setDisplayedMonth(
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH)
        );

        // Group transactions by dayOfMonth
        Map<Integer, List<Transaction>> groupedTx = new HashMap<>();
        if (transactions != null) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.US);
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.US);
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.US);
            
            int selectedMonthVal = currentCalendar.get(Calendar.MONTH) + 1;
            int selectedYearVal = currentCalendar.get(Calendar.YEAR);

            for (Transaction tx : transactions) {
                try {
                    // Transaction date is expected in "yyyy-MM-dd"
                    java.util.Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(tx.getTransactionDate());
                    if (date != null) {
                        int txMonth = Integer.parseInt(monthFormat.format(date));
                        int txYear = Integer.parseInt(yearFormat.format(date));
                        
                        if (txMonth == selectedMonthVal && txYear == selectedYearVal) {
                            int day = Integer.parseInt(dayFormat.format(date));
                            if (!groupedTx.containsKey(day)) {
                                groupedTx.put(day, new ArrayList<>());
                            }
                            groupedTx.get(day).add(tx);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Get total days in month & first day of week
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Android Calendar: Sun=1, Mon=2 ... Sat=7
        // CapMoney grid: T2/Mon=0, T3/Tue=1 ... CN/Sun=6
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int offset;
        if (firstDayOfWeek == Calendar.SUNDAY) {
            offset = 6; // Sunday is the 7th column
        } else {
            offset = firstDayOfWeek - 2; // Monday is index 0, Tue 1...
        }

        // Add empty placeholders for offset days
        for (int i = 0; i < offset; i++) {
            calendarDays.add(new CalendarDay(0, true));
        }

        // Add real days of month
        int maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int dayNum = 1; dayNum <= maxDays; dayNum++) {
            CalendarDay calDay = new CalendarDay(dayNum, false);
            if (groupedTx.containsKey(dayNum)) {
                calDay.setTransactions(groupedTx.get(dayNum));
            }
            calendarDays.add(calDay);
        }

        calendarAdapter.notifyDataSetChanged();
    }

    private void showAddOptionsBottomSheet(int prefilledDay) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_options);

        LinearLayout btnManual = bottomSheetDialog.findViewById(R.id.btnOptionManual);
        LinearLayout btnOcr = bottomSheetDialog.findViewById(R.id.btnOptionOcr);
        LinearLayout btnYolo = bottomSheetDialog.findViewById(R.id.btnOptionYolo);

        if (btnManual != null) {
            btnManual.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                AddTransactionFragment addFragment = new AddTransactionFragment();
                if (prefilledDay > 0) {
                    Bundle bundle = new Bundle();
                    // Generate dynamic selected date in format "yyyy-MM-dd"
                    Calendar selectCal = (Calendar) currentCalendar.clone();
                    selectCal.set(Calendar.DAY_OF_MONTH, prefilledDay);
                    String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectCal.getTime());
                    bundle.putString("prefilled_date", dateStr);
                    addFragment.setArguments(bundle);
                }
                addFragment.show(getParentFragmentManager(), "AddTransactionFragment");
            });
        }

        if (btnOcr != null) {
            btnOcr.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(requireContext(), ScanBillActivity.class));
            });
        }

        if (btnYolo != null) {
            btnYolo.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(requireContext(), ScanProductActivity.class));
            });
        }

        bottomSheetDialog.show();
    }

    private void updateTabStyles() {
        if (!isAdded() || getContext() == null) return;
        
        int activeBgColor = Color.parseColor("#2E2E33");
        int inactiveBgColor = Color.TRANSPARENT;
        int activeTextColor = requireContext().getColor(R.color.text_primary);
        int inactiveTextColor = requireContext().getColor(R.color.text_secondary);
        
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        
        binding.tvDayBubbleText.setText(String.valueOf(day));
        binding.tvMonthBubbleText.setText(String.valueOf(month));

        if (isMonthMode) {
            // Month is active
            binding.btnMonthTab.setBackgroundTintList(ColorStateList.valueOf(activeBgColor));
            binding.tvMonthLabel.setTextColor(activeTextColor);
            binding.ivMonthIcon.setImageTintList(ColorStateList.valueOf(activeTextColor));
            binding.tvMonthBubbleText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3B82F6"))); // blue bubble

            binding.btnDayTab.setBackgroundTintList(ColorStateList.valueOf(inactiveBgColor));
            binding.tvDayLabel.setTextColor(inactiveTextColor);
            binding.tvDayBubbleText.setBackgroundTintList(ColorStateList.valueOf(activeBgColor)); // dark bubble
        } else {
            // Day is active
            binding.btnDayTab.setBackgroundTintList(ColorStateList.valueOf(activeBgColor));
            binding.tvDayLabel.setTextColor(activeTextColor);
            binding.tvDayBubbleText.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3B82F6"))); // blue bubble

            binding.btnMonthTab.setBackgroundTintList(ColorStateList.valueOf(inactiveBgColor));
            binding.tvMonthLabel.setTextColor(inactiveTextColor);
            binding.ivMonthIcon.setImageTintList(ColorStateList.valueOf(inactiveTextColor));
            binding.tvMonthBubbleText.setBackgroundTintList(ColorStateList.valueOf(activeBgColor)); // dark bubble
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
