package com.example.financebackend.controller;

import com.example.financebackend.dto.ApiResponse;
import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.dto.TransferRequest;
import com.example.financebackend.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransfer(@RequestBody TransferRequest request) {
        try {
            LocalDate date = request.getTransferDate() != null ? request.getTransferDate() : LocalDate.now();
            TransactionDTO result = transferService.createTransfer(
                    request.getUserId(),
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    date,
                    request.getNote()
            );
            return ResponseEntity.ok(ApiResponse.success("Transfer completed successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
