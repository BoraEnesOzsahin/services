package com.ayrotek.coldwalletmanagerservice.controller;

import com.ayrotek.coldwalletmanagerservice.dto.SignTransactionRequest;
import com.ayrotek.coldwalletmanagerservice.dto.SignTransactionResponse;
import com.ayrotek.coldwalletmanagerservice.entity.Wallet;
import com.ayrotek.coldwalletmanagerservice.logging.AuditService;
import com.ayrotek.coldwalletmanagerservice.repository.WalletRepository;
import com.ayrotek.coldwalletmanagerservice.service.CheckBalanceService;
import com.ayrotek.coldwalletmanagerservice.service.VaultKeyRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Signs and broadcasts Ethereum Classic transactions.
 *
 * <p><strong>Security constraints</strong>:
 * <ul>
 *   <li>The signed raw transaction hex ({@code signedTxHex}) is NEVER logged.</li>
 *   <li>Key material from Vault is never passed to any logger.</li>
 *   <li>Only the resulting transaction hash and masked address are recorded.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/vault")
public class VaultController {

    private static final Logger log = LoggerFactory.getLogger(VaultController.class);

    private final VaultKeyRetrievalService vaultKeyRetrievalService;
    private final WalletRepository walletRepository;
    private final CheckBalanceService checkBalanceService;
    private final AuditService auditService;

    public VaultController(VaultKeyRetrievalService vaultKeyRetrievalService,
                           WalletRepository walletRepository,
                           CheckBalanceService checkBalanceService,
                           AuditService auditService) {
        this.vaultKeyRetrievalService = vaultKeyRetrievalService;
        this.walletRepository         = walletRepository;
        this.checkBalanceService      = checkBalanceService;
        this.auditService             = auditService;
    }

    @PostMapping("/sign")
    public SignTransactionResponse signTransaction(@RequestBody SignTransactionRequest request) {

        String maskedAddress = maskAddress(request.getAddress());
        MDC.put("event.action",          "transaction-submit");
        MDC.put("wallet.address_masked", maskedAddress);

        try {
            // 1. Resolve wallet
            Wallet wallet = walletRepository.findByAddressIgnoreCase(request.getAddress())
                    .orElseThrow(() -> {
                        log.warn("Transaction signing rejected — wallet not found. addressMasked={}, outcome=failure",
                                maskedAddress);
                        auditService.log("transaction.sign")
                                .actor("system")
                                .resource(maskedAddress)
                                .result("denied")
                                .detail("wallet not found")
                                .emit();
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Wallet not found for address: " + maskedAddress);
                    });

            if (wallet.getHsmAlias() == null || wallet.getHsmAlias().isBlank()) {
                log.error("Transaction signing rejected — wallet has no Vault key. addressMasked={}, outcome=failure",
                        maskedAddress);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Wallet does not have an associated Vault key");
            }

            log.info("Transaction signing started. addressMasked={}, chainId={}", maskedAddress, request.getChainId());

            // 2. Retrieve ECKeyPair — private key is handled securely inside the service
            ECKeyPair ecKeyPair = vaultKeyRetrievalService.getKeyPairFromVault(wallet.getHsmAlias());

            // 3. Resolve amounts
            BigInteger valueInWei = BigInteger.ZERO;
            if (request.getValue() != null) {
                valueInWei = Convert.toWei(request.getValue(), Convert.Unit.ETHER).toBigInteger();
            }

            // 4. Fetch or use provided nonce
            BigInteger nonce = request.getNonce();
            if (nonce == null) {
                nonce = checkBalanceService.getTransactionCount(request.getAddress());
            }

            // 5. Gas price — fetch current and apply 20% buffer; honour caller override
            BigInteger currentGasPrice = checkBalanceService.getGasPrice();
            BigInteger gasPrice = currentGasPrice.multiply(BigInteger.valueOf(120))
                                                 .divide(BigInteger.valueOf(100));
            if (request.getGasPrice() != null && request.getGasPrice().compareTo(gasPrice) > 0) {
                gasPrice = request.getGasPrice();
            }

            // 6. Gas limit — estimate when not provided
            BigInteger gasLimit = request.getGasLimit();
            if (gasLimit == null || gasLimit.compareTo(BigInteger.ZERO) <= 0) {
                gasLimit = checkBalanceService.estimateGas(
                        request.getAddress(), request.getTo(), valueInWei, request.getData());
                gasLimit = gasLimit.multiply(BigInteger.valueOf(120)).divide(BigInteger.valueOf(100));
            }

            // 7. Build, sign, and broadcast — signed hex is NEVER logged
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce, gasPrice, gasLimit, request.getTo(),
                    valueInWei, request.getData() != null ? request.getData() : "");

            byte[] signedMessage = TransactionEncoder.signMessage(
                    rawTransaction, request.getChainId(),
                    org.web3j.crypto.Credentials.create(ecKeyPair));
            String signedTxHex = Numeric.toHexString(signedMessage);

            String txHash = checkBalanceService.sendRawTransaction(signedTxHex);

            // signedTxHex de-referenced immediately; only the txHash (public) is logged
            //noinspection UnusedAssignment
            signedTxHex = null;

            MDC.put("event.outcome", "success");
            log.info("Transaction submitted. addressMasked={}, txHash={}, outcome=success",
                    maskedAddress, txHash);

            auditService.log("transaction.sign")
                    .actor("system")
                    .resource(maskedAddress)
                    .result("success")
                    .detail("txHash=" + txHash + ", chainId=" + request.getChainId())
                    .emit();

            return new SignTransactionResponse(txHash);

        } catch (ResponseStatusException rse) {
            // Already logged and audited above with context; re-throw for the error handler
            throw rse;
        } catch (Exception e) {
            MDC.put("event.outcome", "failure");
            log.error("Transaction submission failed. addressMasked={}, outcome=failure", maskedAddress, e);

            auditService.log("transaction.sign")
                    .actor("system")
                    .resource(maskedAddress)
                    .result("failure")
                    .detail(e.getMessage())
                    .emit();

            throw new RuntimeException("Failed to sign and broadcast transaction", e);
        } finally {
            MDC.remove("event.action");
            MDC.remove("event.outcome");
            MDC.remove("wallet.address_masked");
        }
    }

    private static String maskAddress(String address) {
        if (address == null || address.length() <= 10) return "0x****";
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }
}
