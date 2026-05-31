package com.example.personalfinance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ItemBudgetBinding;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.utils.CurrencyFormatter;
import com.example.personalfinance.utils.DateUtils;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final Context context;
    private final List<Budget> budgets;
    private OnBudgetClickListener clickListener;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    public BudgetAdapter(Context context, List<Budget> budgets) {
        this.context = context;
        this.budgets = budgets;
    }

    public void setOnBudgetClickListener(OnBudgetClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBudgetBinding binding = ItemBudgetBinding.inflate(LayoutInflater.from(context), parent, false);
        return new BudgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetBinding binding;

        public BudgetViewHolder(ItemBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Budget budget) {
            String title = budget.getCategoryName() != null && !budget.getCategoryName().isEmpty() 
                    ? budget.getCategoryName() 
                    : budget.getBudgetName();
            binding.tvCategoryName.setText(title);

            String dates = DateUtils.formatDateForDisplay(budget.getStartDate()) + " - " + DateUtils.formatDateForDisplay(budget.getEndDate());
            binding.tvDateRange.setText(dates);

            int percent = (int) budget.getPercentUsed();
            binding.tvPercentage.setText(percent + "%");
            binding.progressBar.setProgress(Math.min(percent, 100));

            String progressText = CurrencyFormatter.formatVND(budget.getSpentAmount()) + " / " + CurrencyFormatter.formatVND(budget.getAmountLimit());
            binding.tvProgressAmount.setText(progressText);

            // Style progress and warnings based on budget consumption
            if (percent >= 100 || (budget.getExceeded() != null && budget.getExceeded())) {
                binding.progressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.progress_danger));
                binding.tvPercentage.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
                binding.tvWarning.setText("Đã vượt ngân sách!");
                binding.tvWarning.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            } else if (percent >= 80) {
                binding.progressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.progress_warning));
                binding.tvPercentage.setTextColor(ContextCompat.getColor(context, R.color.secondary_variant));
                binding.tvWarning.setText("Đã dùng >80% hạn mức!");
                binding.tvWarning.setTextColor(ContextCompat.getColor(context, R.color.secondary_variant));
            } else {
                binding.progressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.progress_normal));
                binding.tvPercentage.setTextColor(ContextCompat.getColor(context, R.color.primary));
                String remaining = "Còn lại: " + CurrencyFormatter.formatVND(budget.getRemainingAmount());
                binding.tvWarning.setText(remaining);
                binding.tvWarning.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBudgetClick(budget);
                }
            });
        }
    }
}
