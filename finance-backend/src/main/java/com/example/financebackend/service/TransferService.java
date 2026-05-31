package com.example.financebackend.service;

import com.example.financebackend.dto.TransactionDTO;
import com.example.financebackend.model.*;
import com.example.financebackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {

    private final TransferGroupRepository transferGroupRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransferService(TransferGroupRepository transferGroupRepository,
                           TransactionRepository transactionRepository,
                           AccountRepository accountRepository,
                           UserRepository userRepository) {
        this.transferGroupRepository = transferGroupRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionDTO createTransfer(Integer userId, Integer fromAccountId, Integer toAccountId,
                                          BigDecimal amount, LocalDate transferDate, String note) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("From account not found with id: " + fromAccountId));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("To account not found with id: " + toAccountId));

        // Create transfer group
        TransferGroup transferGroup = TransferGroup.builder()
                .user(user)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .transferDate(transferDate)
                .note(note)
                .build();
        transferGroup = transferGroupRepository.save(transferGroup);

        // Create expense transaction for source account
        Transaction outTransaction = Transaction.builder()
                .user(user)
                .account(fromAccount)
                .transferGroup(transferGroup)
                .title("Transfer to " + toAccount.getAccountName())
                .amount(amount)
                .transactionType("expense")
                .transactionDate(transferDate)
                .note(note)
                .status("confirmed")
                .build();
        transactionRepository.save(outTransaction);

        // Create income transaction for destination account
        Transaction inTransaction = Transaction.builder()
                .user(user)
                .account(toAccount)
                .transferGroup(transferGroup)
                .title("Transfer from " + fromAccount.getAccountName())
                .amount(amount)
                .transactionType("income")
                .transactionDate(transferDate)
                .note(note)
                .status("confirmed")
                .build();
        transactionRepository.save(inTransaction);

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        return TransactionDTO.builder()
                .transactionId(outTransaction.getTransactionId())
                .userId(userId)
                .accountId(fromAccountId)
                .accountName(fromAccount.getAccountName())
                .transferGroupId(transferGroup.getTransferGroupId())
                .title(outTransaction.getTitle())
                .amount(amount)
                .transactionType("transfer")
                .transactionDate(transferDate)
                .note(note)
                .status("confirmed")
                .build();
    }

    public List<TransferGroup> getTransfersByUserId(Integer userId) {
        return transferGroupRepository.findByUserUserId(userId);
    }
}
