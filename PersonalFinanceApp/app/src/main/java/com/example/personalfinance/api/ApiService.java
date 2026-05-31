package com.example.personalfinance.api;

import com.example.personalfinance.models.Account;
import com.example.personalfinance.models.AiProductClassificationResult;
import com.example.personalfinance.models.AiProductLog;
import com.example.personalfinance.models.AiScanResult;
import com.example.personalfinance.models.ApiResponse;
import com.example.personalfinance.models.Budget;
import com.example.personalfinance.models.Category;
import com.example.personalfinance.models.LoginRequest;
import com.example.personalfinance.models.Notification;
import com.example.personalfinance.models.OcrRequest;
import com.example.personalfinance.models.ProductClassifyRequest;
import com.example.personalfinance.models.ReportDTO;
import com.example.personalfinance.models.SaveProductLogRequest;
import com.example.personalfinance.models.ScanFeedbackRequest;
import com.example.personalfinance.models.Transaction;
import com.example.personalfinance.models.TransferRequest;
import com.example.personalfinance.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // AUTH
    @POST("api/auth/firebase-login")
    Call<ApiResponse<User>> firebaseLogin(@Body LoginRequest loginRequest);

    // USER
    @GET("api/users/{id}")
    Call<ApiResponse<User>> getUser(@Path("id") int id);

    @PUT("api/users/{id}")
    Call<ApiResponse<User>> updateUser(@Path("id") int id, @Body User user);

    // ACCOUNTS
    @GET("api/accounts")
    Call<ApiResponse<List<Account>>> getAccounts(@Query("userId") int userId);

    @POST("api/accounts")
    Call<ApiResponse<Account>> createAccount(@Body Account account);

    @PUT("api/accounts/{id}")
    Call<ApiResponse<Account>> updateAccount(@Path("id") int id, @Body Account account);

    @DELETE("api/accounts/{id}")
    Call<ApiResponse<Void>> deleteAccount(@Path("id") int id);

    // CATEGORIES
    @GET("api/categories")
    Call<ApiResponse<List<Category>>> getCategories(@Query("userId") int userId);

    @POST("api/categories")
    Call<ApiResponse<Category>> createCategory(@Body Category category);

    @PUT("api/categories/{id}")
    Call<ApiResponse<Category>> updateCategory(@Path("id") int id, @Body Category category);

    @DELETE("api/categories/{id}")
    Call<ApiResponse<Void>> deleteCategory(@Path("id") int id);

    // TRANSACTIONS
    @GET("api/transactions")
    Call<ApiResponse<List<Transaction>>> getTransactions(
            @Query("userId") int userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @POST("api/transactions")
    Call<ApiResponse<Transaction>> createTransaction(@Body Transaction transaction);

    @PUT("api/transactions/{id}")
    Call<ApiResponse<Transaction>> updateTransaction(@Path("id") int id, @Body Transaction transaction);

    @DELETE("api/transactions/{id}")
    Call<ApiResponse<Void>> deleteTransaction(@Path("id") int id);

    // TRANSFERS
    @POST("api/transfers")
    Call<ApiResponse<Void>> transferFunds(@Body TransferRequest request);

    // BUDGETS
    @GET("api/budgets")
    Call<ApiResponse<List<Budget>>> getBudgets(@Query("userId") int userId);

    @POST("api/budgets")
    Call<ApiResponse<Budget>> createBudget(@Body Budget budget);

    @PUT("api/budgets/{id}")
    Call<ApiResponse<Budget>> updateBudget(@Path("id") int id, @Body Budget budget);

    @DELETE("api/budgets/{id}")
    Call<ApiResponse<Void>> deleteBudget(@Path("id") int id);

    // REPORTS
    @GET("api/reports/daily")
    Call<ApiResponse<ReportDTO>> getDailyReport(
            @Query("userId") int userId,
            @Query("date") String date
    );

    @GET("api/reports/monthly")
    Call<ApiResponse<ReportDTO>> getMonthlyReport(
            @Query("userId") int userId,
            @Query("year") int year,
            @Query("month") int month
    );

    @GET("api/reports/by-category")
    Call<ApiResponse<ReportDTO>> getCategoryReport(
            @Query("userId") int userId,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // NOTIFICATIONS
    @GET("api/notifications")
    Call<ApiResponse<List<Notification>>> getNotifications(@Query("userId") int userId);

    @PUT("api/notifications/{id}/read")
    Call<ApiResponse<Notification>> markNotificationAsRead(@Path("id") int id);

    // AI SCAN (OCR)
    @POST("api/ai-scan/classify")
    Call<ApiResponse<AiScanResult>> classifyBill(@Body OcrRequest request);

    @POST("api/ai-scan/feedback")
    Call<ApiResponse<String>> submitScanFeedback(@Body ScanFeedbackRequest request);

    // AI PRODUCT (YOLO)
    @POST("api/ai-product/classify")
    Call<ApiResponse<AiProductClassificationResult>> classifyProduct(@Body ProductClassifyRequest request);

    @POST("api/ai-product/log")
    Call<ApiResponse<AiProductLog>> saveProductLog(@Body SaveProductLogRequest request);

    // RECURRING TRANSACTIONS
    @GET("api/recurring-transactions")
    Call<ApiResponse<List<com.example.personalfinance.models.RecurringTransaction>>> getRecurringTransactions(@Query("userId") int userId);

    @POST("api/recurring-transactions")
    Call<ApiResponse<com.example.personalfinance.models.RecurringTransaction>> createRecurringTransaction(@Body com.example.personalfinance.models.RecurringTransaction dto);

    @PUT("api/recurring-transactions/{id}")
    Call<ApiResponse<com.example.personalfinance.models.RecurringTransaction>> updateRecurringTransaction(@Path("id") int id, @Body com.example.personalfinance.models.RecurringTransaction dto);

    @DELETE("api/recurring-transactions/{id}")
    Call<ApiResponse<Void>> deleteRecurringTransaction(@Path("id") int id);

    @POST("api/recurring-transactions/trigger")
    Call<ApiResponse<Void>> triggerRecurringTransactions();

    // TRANSACTION IMAGES
    @retrofit2.http.Multipart
    @POST("api/transaction-images/upload")
    Call<ApiResponse<Void>> uploadTransactionImage(
            @retrofit2.http.Query("transactionId") int transactionId,
            @retrofit2.http.Part okhttp3.MultipartBody.Part file
    );
}
