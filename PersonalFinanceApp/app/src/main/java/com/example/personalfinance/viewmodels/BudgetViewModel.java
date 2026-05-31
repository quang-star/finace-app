package com.example.personalfinance.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.repositories.AccountRepository;
import com.example.personalfinance.repositories.BudgetRepository;

import java.util.List;

public class BudgetViewModel extends ViewModel {

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository; // to fetch categories for AddBudget

    private final MutableLiveData<List<Budget>> budgets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Budget> budgetCreated = new MutableLiveData<>();
    private final MutableLiveData<Integer> budgetsCreated = new MutableLiveData<>();
    private final MutableLiveData<Boolean> budgetDeleted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BudgetViewModel() {
        this.budgetRepository = new BudgetRepository();
        this.accountRepository = new AccountRepository();
    }

    public LiveData<List<Budget>> getBudgets() {
        return budgets;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Budget> getBudgetCreated() {
        return budgetCreated;
    }

    public LiveData<Integer> getBudgetsCreated() {
        return budgetsCreated;
    }

    public LiveData<Boolean> getBudgetDeleted() {
        return budgetDeleted;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadBudgets(int userId) {
        isLoading.setValue(true);
        budgetRepository.getBudgets(userId, new BudgetRepository.ApiCallback<List<Budget>>() {
            @Override
            public void onSuccess(List<Budget> result) {
                budgets.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải ngân sách: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void loadCategories(int userId) {
        isLoading.setValue(true);
        accountRepository.getCategories(userId, new AccountRepository.ApiCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi tải danh mục: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void createBudget(Budget budget) {
        isLoading.setValue(true);
        budgetRepository.createBudget(budget, new BudgetRepository.ApiCallback<Budget>() {
            @Override
            public void onSuccess(Budget result) {
                budgetCreated.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi thêm ngân sách: " + error);
                isLoading.postValue(false);
            }
        });
    }

    public void createBudgets(List<Budget> budgetList) {
        if (budgetList == null || budgetList.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập ngân sách cho ít nhất một danh mục");
            return;
        }

        isLoading.setValue(true);
        createBudgetAtIndex(budgetList, 0);
    }

    public void saveBudgets(List<Budget> budgetsToCreate, List<Budget> budgetsToUpdate) {
        int createCount = budgetsToCreate != null ? budgetsToCreate.size() : 0;
        int updateCount = budgetsToUpdate != null ? budgetsToUpdate.size() : 0;

        if (createCount + updateCount == 0) {
            errorMessage.setValue("Vui lÃ²ng nháº­p ngÃ¢n sÃ¡ch cho Ã­t nháº¥t má»™t danh má»¥c");
            return;
        }

        isLoading.setValue(true);
        saveBudgetAtIndex(budgetsToCreate, budgetsToUpdate, 0, 0);
    }

    private void createBudgetAtIndex(List<Budget> budgetList, int index) {
        if (index >= budgetList.size()) {
            budgetsCreated.postValue(budgetList.size());
            isLoading.postValue(false);
            return;
        }

        budgetRepository.createBudget(budgetList.get(index), new BudgetRepository.ApiCallback<Budget>() {
            @Override
            public void onSuccess(Budget result) {
                createBudgetAtIndex(budgetList, index + 1);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi thêm ngân sách: " + error);
                isLoading.postValue(false);
            }
        });
    }

    private void saveBudgetAtIndex(List<Budget> budgetsToCreate, List<Budget> budgetsToUpdate, int createIndex, int updateIndex) {
        int createCount = budgetsToCreate != null ? budgetsToCreate.size() : 0;
        int updateCount = budgetsToUpdate != null ? budgetsToUpdate.size() : 0;

        if (createIndex < createCount) {
            budgetRepository.createBudget(budgetsToCreate.get(createIndex), new BudgetRepository.ApiCallback<Budget>() {
                @Override
                public void onSuccess(Budget result) {
                    saveBudgetAtIndex(budgetsToCreate, budgetsToUpdate, createIndex + 1, updateIndex);
                }

                @Override
                public void onError(String error) {
                    errorMessage.postValue("Lá»—i thÃªm ngÃ¢n sÃ¡ch: " + error);
                    isLoading.postValue(false);
                }
            });
            return;
        }

        if (updateIndex < updateCount) {
            Budget budget = budgetsToUpdate.get(updateIndex);
            if (budget.getBudgetId() == null) {
                saveBudgetAtIndex(budgetsToCreate, budgetsToUpdate, createIndex, updateIndex + 1);
                return;
            }

            budgetRepository.updateBudget(budget.getBudgetId(), budget, new BudgetRepository.ApiCallback<Budget>() {
                @Override
                public void onSuccess(Budget result) {
                    saveBudgetAtIndex(budgetsToCreate, budgetsToUpdate, createIndex, updateIndex + 1);
                }

                @Override
                public void onError(String error) {
                    errorMessage.postValue("Lá»—i sá»­a ngÃ¢n sÃ¡ch: " + error);
                    isLoading.postValue(false);
                }
            });
            return;
        }

        budgetsCreated.postValue(createCount + updateCount);
        isLoading.postValue(false);
    }

    public void deleteBudget(int id) {
        isLoading.setValue(true);
        budgetRepository.deleteBudget(id, new BudgetRepository.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                budgetDeleted.postValue(true);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Lỗi xóa ngân sách: " + error);
                budgetDeleted.postValue(false);
                isLoading.postValue(false);
            }
        });
    }
}
