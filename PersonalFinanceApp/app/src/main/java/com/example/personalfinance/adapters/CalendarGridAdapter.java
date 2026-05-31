package com.example.personalfinance.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinance.R;
import com.example.personalfinance.databinding.ItemCalendarDayBinding;
import com.example.personalfinance.models.CalendarDay;
import com.example.personalfinance.models.Transaction;
import java.util.Calendar;
import java.util.List;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.CalendarViewHolder> {

    private final Context context;
    private final List<CalendarDay> calendarDays;
    private OnDayClickListener listener;
    private int selectedPosition = -1;
    private int displayYear;
    private int displayMonth;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day, boolean hasTransaction, int position);
    }

    public CalendarGridAdapter(Context context, List<CalendarDay> calendarDays) {
        this.context = context;
        this.calendarDays = calendarDays;
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setDisplayedMonth(int year, int month) {
        displayYear = year;
        displayMonth = month;
    }

    public void setSelectedPosition(int position) {
        int oldPos = selectedPosition;
        selectedPosition = position;
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (selectedPosition >= 0) notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void clearSelection() {
        int oldPos = selectedPosition;
        selectedPosition = -1;
        if (oldPos >= 0) notifyItemChanged(oldPos);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CalendarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);
        holder.bind(day, position);
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        public CalendarViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CalendarDay day, int position) {
            if (day.isPlaceholder()) {
                // Invisible cell for grid alignment
                itemView.setVisibility(View.INVISIBLE);
                itemView.setClickable(false);
                itemView.setEnabled(false);
                return;
            }

            itemView.setVisibility(View.VISIBLE);
            itemView.setClickable(true);
            itemView.setEnabled(true);
            itemView.setAlpha(1f);
            binding.tvDayNumber.setText(String.valueOf(day.getDayNumber()));

            boolean isSelected = (position == selectedPosition);
            List<Transaction> transactions = day.getTransactions();
            boolean isFutureDay = isFutureDay(day.getDayNumber());

            if (isFutureDay) {
                binding.viewDashedBorder.setVisibility(View.GONE);
                binding.imgPlus.setVisibility(View.GONE);
                binding.imgThumbnail.setVisibility(View.GONE);
                binding.circleContainer.setBackgroundResource(R.drawable.bg_circle);
                binding.circleContainer.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.surface_variant)));
                binding.tvDayNumber.setTextColor(context.getColor(R.color.text_hint));
                itemView.setAlpha(0.55f);
                itemView.setEnabled(false);
                itemView.setClickable(false);
                itemView.setOnClickListener(null);
                return;
            }

            binding.circleContainer.setBackgroundTintList(null);

            if (transactions == null || transactions.isEmpty()) {
                // Empty day - show dashed border and plus sign
                binding.imgThumbnail.setVisibility(View.GONE);

                if (isSelected) {
                    binding.viewDashedBorder.setVisibility(View.GONE);
                    binding.imgPlus.setVisibility(View.VISIBLE);
                    binding.imgPlus.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    binding.circleContainer.setBackgroundResource(R.drawable.bg_circle_selected);
                    binding.tvDayNumber.setTextColor(Color.parseColor("#3B82F6")); // Blue highlight text
                } else {
                    binding.viewDashedBorder.setVisibility(View.VISIBLE);
                    binding.imgPlus.setVisibility(View.VISIBLE);
                    binding.imgPlus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.text_hint)));
                    binding.circleContainer.setBackground(null);
                    binding.tvDayNumber.setTextColor(context.getColor(R.color.text_secondary));
                }
            } else {
                // Has transactions - show thumbnail of the transaction
                binding.viewDashedBorder.setVisibility(View.GONE);
                binding.imgPlus.setVisibility(View.GONE);
                binding.imgThumbnail.setVisibility(View.VISIBLE);

                // Use receipt/item icon overlay
                Transaction firstTx = transactions.get(0);
                if (firstTx.getCategoryName() != null && firstTx.getCategoryName().toLowerCase().contains("ăn uống")) {
                    binding.imgThumbnail.setImageResource(R.drawable.ic_transaction);
                    binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.income_green));
                } else if (firstTx.getCategoryName() != null && (firstTx.getCategoryName().toLowerCase().contains("sức khỏe") || firstTx.getCategoryName().toLowerCase().contains("y tế"))) {
                    binding.imgThumbnail.setImageResource(R.drawable.ic_profile);
                    binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.expense_red));
                } else {
                    binding.imgThumbnail.setImageResource(R.drawable.ic_budget);
                    binding.imgThumbnail.setBackgroundColor(context.getColor(R.color.primary));
                }

                if (isSelected) {
                    binding.circleContainer.setBackgroundResource(R.drawable.bg_circle_selected_ring);
                    binding.tvDayNumber.setTextColor(Color.parseColor("#3B82F6")); // Blue highlight text
                } else {
                    binding.circleContainer.setBackground(null);
                    binding.tvDayNumber.setTextColor(context.getColor(R.color.text_secondary));
                }
            }

            itemView.setOnClickListener(v -> {
                setSelectedPosition(position);
                if (listener != null) {
                    listener.onDayClick(day, transactions != null && !transactions.isEmpty(), position);
                }
            });
        }

        private boolean isFutureDay(int dayNumber) {
            Calendar cellDate = Calendar.getInstance();
            cellDate.set(displayYear, displayMonth, dayNumber, 0, 0, 0);
            cellDate.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return cellDate.after(today);
        }
    }
}
