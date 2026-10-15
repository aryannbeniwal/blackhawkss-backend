#!/usr/bin/env bash
# One-time TLS bootstrap for blackhawkss.in. Run from ~/apps/blackhawkss-backend/deploy on the VM
# AFTER the DNS A records point at this VM. Safe to re-run.
set -euo pipefail
cd "$(dirname "$0")"
DOMAIN=blackhawkss.in
EMAIL=helloblackhawkss@gmail.com

echo "==> Checking DNS for $DOMAIN"
MY_IP=$(curl -fsS https://api.ipify.org || true)
DNS_IP=$(getent hosts "$DOMAIN" | awk '{print $1}' | head -1 || true)
echo "    this VM: ${MY_IP:-unknown}   DNS says: ${DNS_IP:-not found}"
if [[ -z "$DNS_IP" || "$DNS_IP" != "$MY_IP" ]]; then
  echo "!! DNS does not point here yet. Add the A records and wait, then re-run."; exit 1
fi

if docker compose run --rm --entrypoint "" certbot test -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem 2>/dev/null; then
  echo "==> Certificate already exists. Starting the full stack."
  docker compose up -d --build
  exit 0
fi

echo "==> Starting nginx in HTTP-only mode"
cp nginx/blackhawkss.conf nginx/blackhawkss.conf.full
cp nginx/bootstrap-http.conf nginx/blackhawkss.conf
docker compose up -d nginx

echo "==> Requesting certificate from Let's Encrypt"
docker compose run --rm certbot certonly --webroot -w /var/www/certbot \
  -d "$DOMAIN" -d "www.$DOMAIN" --email "$EMAIL" --agree-tos --no-eff-email

echo "==> Restoring the full nginx config and starting everything"
mv nginx/blackhawkss.conf.full nginx/blackhawkss.conf
docker compose up -d --build
docker compose restart nginx
echo "==> Done. Open https://$DOMAIN"
