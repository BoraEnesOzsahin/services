#!/bin/sh

# Wait for Vault to become reachable
echo "Waiting for Vault to start at $VAULT_ADDR..."
while true; do
  vault status > /dev/null 2>&1
  RES=$?
  # vault status: 0=unsealed+initialized, 2=sealed/uninitialized, 1=connection error
  if [ $RES -eq 0 ] || [ $RES -eq 2 ]; then
    break
  fi
  sleep 2
done
echo "Vault is reachable."

# Check if Vault is initialized
if vault status -format=json | grep -q '"initialized": false'; then
  echo "Vault is not initialized. Initializing..."

  # Initialize Vault and capture the output
  vault operator init -key-shares=1 -key-threshold=1 > /vault/file/init.out

  # Extract keys — values are written to files, NEVER echoed to stdout/logs
  UNSEAL_KEY=$(grep 'Unseal Key 1:' /vault/file/init.out | awk '{print $NF}')
  ROOT_TOKEN=$(grep 'Initial Root Token:' /vault/file/init.out | awk '{print $NF}')

  # Persist keys for future unseal operations (file is on a mounted volume)
  printf '%s' "$UNSEAL_KEY" > /vault/file/unseal.key
  printf '%s' "$ROOT_TOKEN" > /vault/file/root.token

  # Unseal Vault
  vault operator unseal "$UNSEAL_KEY"

  # Login with root token to setup Vault
  vault login "$ROOT_TOKEN" > /dev/null 2>&1
  export VAULT_TOKEN="$ROOT_TOKEN"

  # 1. Enable KV v2 at 'secret' path
  vault secrets enable -path=secret kv-v2

  # 2. Enable AppRole authentication
  vault auth enable approle

  # 3. Create a policy scoped to wallet secrets only
  cat <<EOF > /tmp/wallet-service-policy.hcl
path "secret/data/wallets/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
path "secret/metadata/wallets/*" {
  capabilities = ["list", "read", "delete"]
}
EOF
  vault policy write wallet-service-policy /tmp/wallet-service-policy.hcl

  # 4. Create AppRole with the policy
  vault write auth/approle/role/wallet-service \
    secret_id_ttl=0 \
    token_num_uses=0 \
    token_ttl=1h \
    token_max_ttl=24h \
    secret_id_num_uses=0 \
    policies=wallet-service-policy

  # 5. Persist the RoleID and SecretID for the app container
  ROLE_ID=$(vault read -field=role_id auth/approle/role/wallet-service/role-id)
  SECRET_ID=$(vault write -f -field=secret_id auth/approle/role/wallet-service/secret-id)

  # Write to files — NEVER log these values
  printf '%s' "$ROLE_ID"   > /vault/file/role.id
  printf '%s' "$SECRET_ID" > /vault/file/secret.id

  # 6. Enable Vault audit device so logs are shipped to Filebeat
  sh /vault-audit-setup.sh

  echo "Vault initialization complete."
else
  echo "Vault is already initialized."

  # Unseal if sealed
  if vault status -format=json | grep -q '"sealed": true'; then
    echo "Vault is sealed. Unsealing..."
    UNSEAL_KEY=$(cat /vault/file/unseal.key)
    vault operator unseal "$UNSEAL_KEY"
    echo "Vault unsealed."
  else
    echo "Vault is already unsealed."
  fi

  # Re-login and ensure audit device is still enabled (idempotent)
  if [ -f /vault/file/root.token ]; then
    ROOT_TOKEN=$(cat /vault/file/root.token)
    vault login "$ROOT_TOKEN" > /dev/null 2>&1
    export VAULT_TOKEN="$ROOT_TOKEN"
    sh /vault-audit-setup.sh
  fi
fi

exit 0
