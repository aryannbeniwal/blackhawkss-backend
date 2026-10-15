# Going live on blackhawkss.in — a first-timer's walkthrough

This is the long version of `README.md`, written for someone who has never used a cloud VM.
A **VM** (virtual machine) is just a Linux computer that Oracle runs for you in their data centre.
You connect to it from your Mac's Terminal, install Docker on it, and Docker runs our four
programs (database, API, website, nginx web server) from the two GitHub repos.

Total time: about 1–2 hours the first time, most of it waiting.

What you need before starting:
- Your Oracle Cloud account (Always Free).
- Access to the registrar where you bought `blackhawkss.in` (to add DNS records).
- Your Resend API key (from resend.com → API Keys). Keep it somewhere safe; you'll paste it into a file on the VM.
- Your Mac with Terminal.

---

## Part A — Make an SSH key on your Mac (5 min)

SSH is how your Terminal logs into the VM. It uses a key pair instead of a password.

1. Open **Terminal** on your Mac.
2. Run:
   ```bash
   ssh-keygen -t ed25519 -C "blackhawkss-vm" -f ~/.ssh/blackhawkss
   ```
   Press Enter at both passphrase prompts (or set one if you prefer).
3. Show the **public** key and copy the whole line it prints (starts with `ssh-ed25519`):
   ```bash
   cat ~/.ssh/blackhawkss.pub
   ```
   You'll paste this into Oracle in Part B. The file without `.pub` is your private key — never share it.

---

## Part B — Create the VM in Oracle Cloud (15 min)

1. Sign in at https://cloud.oracle.com. Top-left menu ☰ → **Compute** → **Instances**.
2. Click **Create instance**.
3. **Name:** `blackhawkss`.
4. **Placement:** leave default (if the next step fails with "Out of host capacity", come back and try a different *Availability domain* here).
5. **Image and shape** → click **Edit**:
   - **Image:** click *Change image* → choose **Canonical Ubuntu 24.04** (plain, not Minimal) and expand its ▸ row to pick the **aarch64** build. If only *Minimal aarch64* is offered for ARM, that is fine too — just run `sudo apt update && sudo apt install -y iptables-persistent curl git` first thing in Part C. Avoid 26.04 (too new for Docker's installer).
   - **Shape:** click *Change shape* → **Ampere** → **VM.Standard.A1.Flex** → set **2 OCPUs** and **12 GB** memory. This is the free allowance.
6. **Networking:** leave the defaults (it creates a VCN for you). Make sure **Assign a public IPv4 address** is selected.
7. **Add SSH keys:** choose **Paste public keys** and paste the line you copied in Part A.
8. **Boot volume:** 50 GB is fine.
9. Click **Create**. Wait until the status turns **Running** (1–2 min).
10. On the instance page, copy the **Public IP address**. Write it down — call it `VM_IP` below.

> **"Out of host capacity"**: the free ARM shape is popular and Hyderabad has a single availability domain. Retry **Create** every few minutes (nights and early mornings IST are better), or upgrade the account to **Pay As You Go** (Billing → Upgrade and Manage Payment). Upgrading keeps Always Free resources free but gives capacity priority; set a Budget alert at ₹100 for peace of mind.

### Open the web ports in Oracle's firewall

Oracle blocks all incoming traffic except SSH until you allow it.

1. On the instance page, under **Primary VNIC**, click the **Subnet** link.
2. Click the **Security List** (usually "Default Security List for …").
3. Click **Add Ingress Rules** and add two rules:
   - Source CIDR `0.0.0.0/0`, IP protocol **TCP**, Destination port range **80**
   - Source CIDR `0.0.0.0/0`, IP protocol **TCP**, Destination port range **443**
4. Save.

---

## Part C — Connect to the VM and prepare it (15 min)

From your Mac Terminal (replace `VM_IP`):
```bash
ssh -i ~/.ssh/blackhawkss ubuntu@VM_IP
```
Type `yes` when asked about the fingerprint. You're now typing commands **on the VM** (prompt shows `ubuntu@blackhawkss`).

1. (Minimal image only) `sudo apt update && sudo apt install -y iptables-persistent curl git`
2. Ubuntu's own firewall also blocks 80/443. Open them:
   ```bash
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
   sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
   sudo netfilter-persistent save
   ```
3. Update the system and install Docker:
   ```bash
   sudo apt update && sudo apt -y upgrade
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker $USER
   ```
4. Log out and back in so Docker works without `sudo`:
   ```bash
   exit
   ssh -i ~/.ssh/blackhawkss ubuntu@VM_IP
   docker --version        # should print a version
   ```

---

## Part D — Point the domain at the VM (5 min + waiting)

At the website where you bought `blackhawkss.in` (its **DNS** / **DNS management** page):

1. Add an **A record**: Host/Name `@` (or blank), Value `VM_IP`, TTL default.
2. Add another **A record**: Host/Name `www`, Value `VM_IP`.
3. Delete any existing A or "parking" records for `@` and `www` if the registrar created some.
4. Keep the three **Resend** records (TXT/CNAME/MX) you added for email — they live alongside these.

Check from your Mac after 5–30 minutes:
```bash
dig +short blackhawkss.in
```
It must print `VM_IP`. Don't continue to Part F until it does.

---

## Part E — Get the code onto the VM and fill in the secrets (15 min)

On the VM:
```bash
mkdir -p ~/apps && cd ~/apps
git clone https://github.com/aryannbeniwal/blackhawkss-backend.git
git clone https://github.com/aryannbeniwal/blackhawkss-frontend.git
cd blackhawkss-backend
cp .env.example .env
```

Generate two secrets and keep the output on screen:
```bash
echo "DB password:  $(openssl rand -base64 24 | tr -d '/+=' | cut -c1-24)"
echo "JWT secret:   $(openssl rand -base64 48)"
```

Now edit the file:
```bash
nano .env
```
`nano` basics: arrow keys to move, type to edit, **Ctrl+O** then Enter to save, **Ctrl+X** to exit.

Fill these lines (leave everything else as it is):

| Line | Put |
|---|---|
| `DB_PASS=` and `POSTGRES_PASSWORD=` | the **same** DB password you generated |
| `JWT_SECRET=` | the JWT secret you generated |
| `ADMIN_SEED_EMAIL=` | the email you'll log in to `/admin` with (e.g. helloblackhawkss@gmail.com) |
| `ADMIN_SEED_PASSWORD=` | a strong password for that login (12+ characters) |
| `SMTP_PASS=` | your **Resend API key** (`SMTP_USER` stays `resend`) |

Save and exit. Never commit or share this file.

---

## Part F — First start with HTTPS (10 min)

Still on the VM:
```bash
cd ~/apps/blackhawkss-backend/deploy
./bootstrap-tls.sh
```
The script checks DNS, requests the free Let's Encrypt certificate, then builds and starts all four containers. The first build takes **5–10 minutes** (it compiles the Java API and the Next.js site on the VM). If it stops with "DNS does not point here yet", wait and re-run.

When it prints `Done`, open **https://blackhawkss.in** in your browser.

Check:
```bash
docker compose ps                 # all four services "running"
docker compose logs api | grep -i "seeded"   # "Seeded first admin user …"
```

---

## Part G — Verify everything works (10 min)

1. Open https://blackhawkss.in and browse a few pages.
2. Submit a test quote from `/quote`. You should get a reference like `BH-2026-0001`.
3. Check helloblackhawkss@gmail.com for the owner email (also check spam the first time).
4. Open https://blackhawkss.in/admin/login and sign in with `ADMIN_SEED_EMAIL` / `ADMIN_SEED_PASSWORD`. The test enquiry should be listed.
5. In Admin → Gallery, upload one photo and confirm it appears on `/gallery`.

If an email didn't arrive: `docker compose logs api | grep -i mail` shows the SMTP error; usually the Resend domain is not verified yet or the key is wrong.

---

## Part H — Keep it healthy (10 min, once)

On the VM, set up daily backups and an uptime ping (Oracle can reclaim a free VM that looks idle for 7 days):
```bash
mkdir -p ~/backups
cat > ~/backup.sh <<'EOF'
#!/usr/bin/env bash
cd ~/apps/blackhawkss-backend/deploy
docker compose exec -T postgres pg_dump -U blackhawkss blackhawkss | gzip > ~/backups/bhss-$(date +%F).sql.gz
docker run --rm -v deploy_uploads:/u -v ~/backups:/b alpine tar czf /b/uploads-$(date +%F).tgz -C /u .
find ~/backups -type f -mtime +30 -delete
EOF
chmod +x ~/backup.sh
(crontab -l 2>/dev/null; echo "30 2 * * * ~/backup.sh >/dev/null 2>&1"; echo "*/5 * * * * curl -fsS https://blackhawkss.in/ >/dev/null 2>&1") | crontab -
```
Every month or so, copy `~/backups` off the VM (`scp -i ~/.ssh/blackhawkss ubuntu@VM_IP:~/backups/* ~/Desktop/bhss-backups/` from your Mac).

---

## Updating the site later

After new commits are pushed to GitHub:
```bash
ssh -i ~/.ssh/blackhawkss ubuntu@VM_IP
cd ~/apps/blackhawkss-backend && git pull
cd ~/apps/blackhawkss-frontend && git pull
cd ~/apps/blackhawkss-backend/deploy && docker compose up -d --build && docker image prune -f
```
About 30 seconds of downtime while the containers restart.

---

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `ssh: Permission denied (publickey)` | Wrong key path or user. Use `-i ~/.ssh/blackhawkss` and user `ubuntu`. |
| Browser times out on http://VM_IP | Ports not open: redo the Security List rules (Part B) **and** the iptables commands (Part C). |
| `bootstrap-tls.sh` says DNS does not point here | A records not added or not propagated yet. Check `dig +short blackhawkss.in`. |
| Certbot "too many failed authorizations" | Wait 1 hour, confirm port 80 reaches the VM, re-run. |
| Build killed / "exit code 137" | Out of memory during the first build. Run `docker compose build api` then `docker compose build web` one at a time, then `docker compose up -d`. |
| Site up but emails not sending | Resend domain not verified or wrong `SMTP_PASS`. `docker compose logs api \| grep -i mail`. |
| Admin login fails | `ADMIN_SEED_*` only applies on the very first start. To reset, stop the stack, `docker volume rm deploy_pgdata` (this deletes all data), fix `.env`, start again. |
| Out of host capacity when creating the VM | Retry later / other availability domain / upgrade to Pay As You Go (still free). |
