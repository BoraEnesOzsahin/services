package com.ayrotek.coldwalletmanagerservice.service;

import com.ayrotek.coldwalletmanagerservice.entity.Wallet;
import com.ayrotek.coldwalletmanagerservice.logging.AuditService;
import com.ayrotek.coldwalletmanagerservice.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.vault.core.VaultTemplate;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generates new Ethereum wallets, stores the private key in Vault KV v2,
 * and persists the wallet metadata in PostgreSQL.
 *
 * <p><strong>Security constraints</strong>:
 * <ul>
 *   <li>Private keys are NEVER logged — not even partially.</li>
 *   <li>The wallet address is masked in logs (first 6 + last 4 chars).</li>
 *   <li>The Vault key name (hsmAlias) is safe to log; it is a UUID-based path only.</li>
 * </ul>
 */
@Service
public class VaultWalletService {

    private static final Logger log = LoggerFactory.getLogger(VaultWalletService.class);

    private final VaultTemplate vaultTemplate;
    private final WalletRepository walletRepository;
    private final AuditService auditService;

    public VaultWalletService(VaultTemplate vaultTemplate,
                              WalletRepository walletRepository,
                              AuditService auditService) {
        this.vaultTemplate    = vaultTemplate;
        this.walletRepository = walletRepository;
        this.auditService     = auditService;
    }

    /**
     * Generates a new EC key pair, derives its Ethereum address, saves metadata to the
     * database, and stores the private key securely in HashiCorp Vault KV v2.
     *
     * @param name the logical name of the wallet
     * @return the newly persisted {@link Wallet} entity
     */
    @Transactional
    public Wallet generateNewWalletAddress(String name) {
        String keyName = "wallet-" + UUID.randomUUID();

        MDC.put("event.action", "wallet-create");

        try {
            org.web3j.crypto.ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            // ── SECURITY: privateKeyHex is used only to write to Vault; NEVER logged ──
            String privateKeyHex = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
            String publicKeyHex  = Numeric.toHexStringNoPrefix(ecKeyPair.getPublicKey());
            String address       = "0x" + Keys.getAddress(ecKeyPair);
            String maskedAddress = maskAddress(address);

            MDC.put("wallet.address_masked", maskedAddress);

            // Store ONLY the private key in Vault; the value must not touch any log stream
            Map<String, Object> secret = new HashMap<>();
            secret.put("privateKey", privateKeyHex);
            vaultTemplate.opsForVersionedKeyValue("secret").put("wallets/" + keyName, secret);

            // privateKeyHex reference is explicitly nulled after use
            //noinspection UnusedAssignment
            privateKeyHex = null;

            log.info("Vault secret written. event.action=vault-write, keyPath=wallets/{}, outcome=success",
                    keyName);

            LocalDateTime now    = LocalDateTime.now();
            Wallet wallet        = new Wallet(name, address, publicKeyHex, now);
            wallet.setHsmAlias(keyName);
            Wallet saved         = walletRepository.save(wallet);

            MDC.put("event.outcome", "success");
            log.info("Wallet creation completed. walletId={}, addressMasked={}, vaultKey={}, outcome=success",
                    saved.getId(), maskedAddress, keyName);

            auditService.log("wallet.create")
                    .actor("system")
                    .resource(maskedAddress)
                    .result("success")
                    .detail("walletId=" + saved.getId() + ", vaultKey=" + keyName)
                    .emit();

            return saved;

        } catch (Exception e) {
            MDC.put("event.outcome", "failure");
            log.error("Wallet creation failed. vaultKey={}, outcome=failure", keyName, e);

            auditService.log("wallet.create")
                    .actor("system")
                    .resource(keyName)
                    .result("failure")
                    .detail(e.getMessage())
                    .emit();

            throw new RuntimeException("Failed to generate and store wallet in Vault", e);
        } finally {
            MDC.remove("event.action");
            MDC.remove("event.outcome");
            MDC.remove("wallet.address_masked");
        }
    }

    /**
     * Masks a wallet address for safe logging: keeps the first 6 and last 4 characters.
     * Example: {@code 0x1234...89ab}
     */
    static String maskAddress(String address) {
        if (address == null || address.length() <= 10) {
            return "0x****";
        }
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }
}
