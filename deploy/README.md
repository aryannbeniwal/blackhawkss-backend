# Deploying blackhawkss.in on an Oracle Cloud Always-Free VM

Target: one Ampere A1 instance (the free allowance is 2 OCPU / 12 GB RAM, 200 GB storage), Ubuntu 24.04 ARM, running Postgres + API + Next.js + nginx with docker-compose. Nothing here runs automatically; every step is manual and reversible.

## 1. Provision the VM
1. OCI Console → Compute → Create instance → shape **VM.Standard.A1.Flex**, 2 OCPU / 12 GB, image **Canonical Ubuntu 24.04 (aarch64)**, boot volume 50 GB. If you get "Out of host capacity", retry later, try another availability domain, or upgrade the account to Pay-As-You-Go (free tier pricing stays, capacity priority improves).
2. Add your SSH public key. Note the public IP.
3. VCN → Security List → add ingress rules for **TCP 80** and **TCP 443** from `0.0.0.0/0`.
4. On the VM, Ubuntu's iptables also blocks 80/443 by default:
   ```bash
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
   sudo netfilter-persistent save
   ```

## 2. Install Docker
```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER && newgrp docker
```

## 3. Clone both repos
```bash
mkdir -p ~/apps && cd ~/apps
git clone https://github.com/aryannbeniwal/blackhawkss-backend.git
git clone https://github.com/aryannbeniwal/blackhawkss-frontend.git
cp blackhawkss-backend/.env.example blackhawkss-backend/.env
nano blackhawkss-backend/.env   # fill DB password, JWT_SECRET (openssl rand -base64 48), admin seed, SMTP
```

## 4. DNS
At your registrar, create `A` records for `blackhawkss.in` and `www.blackhawkss.in` → the VM public IP. Wait until `dig +short blackhawkss.in` returns it.

## 5. First TLS certificate
nginx cannot start the 443 server block without a certificate, so bootstrap once:
```bash
cd ~/apps/blackhawkss-backend/deploy
# temporarily start only postgres+api+web, then run nginx with the 80 block only:
sed -i 's/^\(.*listen 443.*\)$/#\1/' nginx/blackhawkss.conf   # comment 443 listeners (undo after)
docker compose up -d postgres api web nginx
docker compose run --rm certbot certonly --webroot -w /var/www/certbot \
  -d blackhawkss.in -d www.blackhawkss.in --email helloblackhawkss@gmail.com --agree-tos --no-eff-email
git checkout nginx/blackhawkss.conf      # restore the 443 blocks
docker compose up -d --build             # full stack
```
The `certbot` service renews automatically afterwards.

## 6. Verify
- `curl -I https://blackhawkss.in` → 200
- `curl https://blackhawkss.in/api/v1/public/clients` → JSON envelope
- `docker compose logs -f api` shows "Seeded first admin user"
- Log in at https://blackhawkss.in/admin/login

## 7. Updating
```bash
cd ~/apps/blackhawkss-backend && git pull && cd ../blackhawkss-frontend && git pull
cd ~/apps/blackhawkss-backend/deploy && docker compose up -d --build && docker image prune -f
```
Or enable `.github/workflows/deploy.yml` by adding secrets `VM_HOST`, `VM_USER`, `VM_SSH_KEY` and repository variable `DEPLOY_ENABLED=true`.

## 8. Backups
```bash
docker compose exec postgres pg_dump -U blackhawkss blackhawkss | gzip > ~/backups/bhss-$(date +%F).sql.gz
docker run --rm -v deploy_uploads:/u -v ~/backups:/b alpine tar czf /b/uploads-$(date +%F).tgz -C /u .
```
Add both to a daily cron. Copy the files off the VM (OCI Object Storage has a free 20 GB tier).

## Notes
- **Idle reclamation**: Oracle may reclaim an Always-Free instance idle for 7 days (CPU, network and memory all under 20 %). A low-traffic JVM + Postgres usually stays above that; if not, the `uptime-ping` cron below helps, or switch to Pay-As-You-Go.
  ```bash
  (crontab -l; echo "*/5 * * * * curl -fsS https://blackhawkss.in/ >/dev/null 2>&1") | crontab -
  ```
- Images are built **on the VM** (native arm64), so no cross-compilation is needed.
- Memory budget: Postgres 1 GB, API 1 GB (JVM flags in the Dockerfile), Next.js 512 MB, nginx 128 MB.
