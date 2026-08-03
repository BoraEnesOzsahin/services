#!/bin/sh

# vault-audit-setup.sh
# Enables a Vault file audit device after Vault is initialised and unsealed.
# This script is called from vault-init.sh after login.
#
# The audit log is written to /vault/logs/audit.log which is mounted
# into both the vault container and the filebeat container.
#
# NOTE: Vault audit devices CANNOT be enabled by environment variable.
# They must be enabled via the Vault CLI or API after Vault is unsealed.

AUDIT_LOG_PATH="/vault/logs/audit.log"

echo "Enabling Vault file audit device at $AUDIT_LOG_PATH..."

# Check if the audit device is already enabled
if vault audit list 2>/dev/null | grep -q "file/"; then
  echo "Vault audit device (file/) is already enabled."
else
  vault audit enable file \
    file_path="$AUDIT_LOG_PATH" \
    log_raw=false \
    mode=0640

  echo "Vault audit device enabled. Logs will be written to $AUDIT_LOG_PATH"
fi
