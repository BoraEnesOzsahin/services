package com.ayrotek.coldwalletmanagerservice.controller;

import com.ayrotek.coldwalletmanagerservice.dto.BalanceResponse;
import com.ayrotek.coldwalletmanagerservice.dto.WalletGenerationRequest;
import com.ayrotek.coldwalletmanagerservice.entity.Wallet;
import com.ayrotek.coldwalletmanagerservice.repository.WalletRepository;
import com.ayrotek.coldwalletmanagerservice.logging.AuditService;
import com.ayrotek.coldwalletmanagerservice.service.CheckBalanceService;
import com.ayrotek.coldwalletmanagerservice.service.VaultWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);

    private final VaultWalletService vaultWalletService;
    private final WalletRepository walletRepository;
    private final CheckBalanceService checkBalanceService;
    private final AuditService auditService;

    public WalletController(VaultWalletService vaultWalletService,
                            WalletRepository walletRepository,
                            CheckBalanceService checkBalanceService,
                            AuditService auditService) {
        this.vaultWalletService   = vaultWalletService;
        this.walletRepository     = walletRepository;
        this.checkBalanceService  = checkBalanceService;
        this.auditService         = auditService;
    }

    @PostMapping("/generate")
    public Wallet generateWallet(@RequestBody WalletGenerationRequest request) throws Exception {
        log.info("Wallet generation requested. walletName={}", request.getName());
        // Structured logging and MDC are handled inside VaultWalletService
        return vaultWalletService.generateNewWalletAddress(request.getName());
    }

    @GetMapping("/allWallets")
    public List<Wallet> getAllWallets() {
        log.info("Wallet list requested. event.action=wallet-read, event.outcome=success");
        try {
            List<Wallet> wallets = walletRepository.findAll();
            auditService.log("wallet.list").result("success").emit();
            return wallets;
        } catch (Exception ex) {
            auditService.log("wallet.list").result("failure").emit();
            throw ex;
        }
    }

    @GetMapping("/{address}/balance")
    public BalanceResponse getWalletBalance(@PathVariable String address) {
        // Mask address in logs — never log full addresses in case of sensitive metadata
        String maskedAddress = maskAddress(address);
        MDC.put("event.action", "wallet-read");
        try {
            walletRepository.findByAddressIgnoreCase(address)
                    .orElseThrow(() -> {
                        log.warn("Wallet not found. addressMasked={}, event.action=wallet-read, outcome=failure",
                                maskedAddress);
                        auditService.log("wallet.balance_check").resource(maskedAddress).result("denied").emit();
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Wallet not found for address: " + maskedAddress);
                    });

            BigDecimal balance = checkBalanceService.getBalance(address);
            log.info("Balance retrieved. addressMasked={}, event.action=wallet-read, outcome=success",
                    maskedAddress);
            auditService.log("wallet.balance_check").resource(maskedAddress).result("success").emit();
            return new BalanceResponse(address, balance);
        } finally {
            MDC.remove("event.action");
        }
    }

    private static String maskAddress(String address) {
        if (address == null || address.length() <= 10) return "0x****";
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }
}
