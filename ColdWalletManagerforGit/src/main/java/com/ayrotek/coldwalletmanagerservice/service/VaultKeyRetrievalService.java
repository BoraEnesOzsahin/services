package com.ayrotek.coldwalletmanagerservice.service;

import com.ayrotek.coldwalletmanagerservice.logging.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Versioned;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import java.util.Map;

/**
 * Retrieves private key material from HashiCorp Vault KV v2 and returns
 * an {@link ECKeyPair} for transaction signing.
 *
 * <p><strong>Security constraints</strong>:
 * <ul>
 *   <li>The raw private key hex value is NEVER logged, traced, or passed outside this method.</li>
 *   <li>Only the Vault key path (hsmAlias) is recorded — it is a non-secret identifier.</li>
 * </ul>
 */
@Service
public class VaultKeyRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(VaultKeyRetrievalService.class);

    private final VaultTemplate vaultTemplate;
    private final AuditService auditService;

    public VaultKeyRetrievalService(VaultTemplate vaultTemplate, AuditService auditService) {
        this.vaultTemplate = vaultTemplate;
        this.auditService  = auditService;
    }

    /**
     * Retrieves the {@link ECKeyPair} from Vault using the stored Vault key name.
     *
     * @param hsmAlias the path suffix of the key in Vault (e.g. {@code wallet-<uuid>})
     * @return the loaded ECKeyPair ready for signing
     */
    public ECKeyPair getKeyPairFromVault(String hsmAlias) {
        if (hsmAlias == null || hsmAlias.isBlank()) {
            throw new IllegalArgumentException("HSM Alias cannot be null or empty");
        }

        log.info("Retrieving key material from Vault. event.action=vault-read, keyPath=wallets/{}", hsmAlias);

        Versioned<Map<String, Object>> response =
                vaultTemplate.opsForVersionedKeyValue("secret").get("wallets/" + hsmAlias);

        if (response == null || !response.hasData() || response.getData() == null) {
            log.error("Vault key not found. event.action=vault-read, keyPath=wallets/{}, outcome=failure", hsmAlias);
            auditService.log("vault.key_read")
                    .actor("system")
                    .resource("wallets/" + hsmAlias)
                    .result("failure")
                    .detail("key not found in vault")
                    .emit();
            throw new RuntimeException("Private key not found in Vault for alias: " + hsmAlias);
        }

        Map<String, Object> data = response.getData();

        if (!data.containsKey("privateKey")) {
            log.error("Vault secret has unexpected format. event.action=vault-read, keyPath=wallets/{}, outcome=failure",
                    hsmAlias);
            auditService.log("vault.key_read")
                    .actor("system")
                    .resource("wallets/" + hsmAlias)
                    .result("failure")
                    .detail("unexpected secret format in vault")
                    .emit();
            throw new RuntimeException("Invalid data format in Vault for alias: " + hsmAlias);
        }

        // ── SECURITY: value extracted into a local variable and never logged ──
        String privateKeyHex = (String) data.get("privateKey");
        Credentials credentials = Credentials.create(privateKeyHex);
        //noinspection UnusedAssignment
        privateKeyHex = null;                   // de-reference immediately after use

        log.info("Key material retrieved successfully. event.action=vault-read, keyPath=wallets/{}, outcome=success",
                hsmAlias);

        auditService.log("vault.key_read")
                .actor("system")
                .resource("wallets/" + hsmAlias)
                .result("success")
                .emit();

        return credentials.getEcKeyPair();
    }
}
