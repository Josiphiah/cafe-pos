# Team workflow — Café POS

Five people, five slices, everything pushed **straight to `main`**. This file is how each
of you ends up showing on the repo's **Contributors** page with real commits.

---

## 1. One-time setup (everyone, ~5 min)

1. **Accept the invite** — email from GitHub, or https://github.com/notifications → accept
   the invitation to `Josiphiah/cafe-pos`.
2. **Clone it** — VS Code → `Ctrl+Shift+P` → *Git: Clone* →
   `https://github.com/Josiphiah/cafe-pos` → pick a folder → *Open*.
3. **Set your commit identity so GitHub counts your commits.** Open a terminal in the
   project (`` Ctrl+` ``) and run, with **your** details:

   ```
   git config user.name  "Your Full Name"
   git config user.email "the-email-on-your-github-account"
   ```

   Don't know which email? GitHub → *Settings* → *Emails*. If you tick
   *"Keep my email addresses private"*, use the address shown there that looks like
   `12345678+yourusername@users.noreply.github.com`. **If the email doesn't match your
   GitHub account, your commits will NOT show you as a contributor.**
4. **Check it built:** `./mvnw spring-boot:run` → open http://localhost:8080 →
   log in as `manager` / `manager123`. Stop it with `Ctrl+C`.

---

## 2. Every work session

```
git pull                       # always start here
# ... do your changes ...
git add -A
git commit -m "auth: add staff list screen (#1)"   # put your issue number in ()
git push
```

- Put your **issue number** in every commit message like `(#3)` — GitHub then links the
  commit to your ticket automatically.
- Only touch the files your issue says you own. If you need something from another slice,
  comment on that issue instead of editing it.
- If VS Code ever pops up a **"Java upgrade / modernize"** suggestion, **dismiss it** — it
  moves your work onto a stray branch.

---

## 3. Your reserved task

The shared skeleton is already on `main` (entities, repositories, layout, seeded menu).
Josiphiah is building the bulk of each slice; the piece below is **yours to implement and
push yourself** so your contribution is real. Full detail is in your GitHub issue.

| You | Issue | Your piece to build & push |
|---|---|---|
| **Salifyanji** | [#1](https://github.com/Josiphiah/cafe-pos/issues/1) | The manager-only **Staff screen**: `GET/POST /staff` list + add-staff form (`auth/StaffAdminController.java`, `templates/staff/list.html`) and one test that a CASHIER gets 403. |
| **Goodson** | [#2](https://github.com/Josiphiah/cafe-pos/issues/2) | The **catalog test suite** (`src/test/java/zm/cafe/pos/catalog/CatalogServiceTest.java`, ≥3 cases: price-must-be-positive, retire hides item, edit updates price) **plus** `src/test/java/README.md` — how we name and run tests. (QA owns testing.) |
| **Kenneth** | [#4](https://github.com/Josiphiah/cafe-pos/issues/4) | The **refunds audit page**: `GET /history/refunds` listing every refund (amount, reason, who, when) → `history/refunds-list.html`, **plus** the date-range filter on `GET /history`. One test for the date filter. |
| **Vanessa** | [#5](https://github.com/Josiphiah/cafe-pos/issues/5) + [#6](https://github.com/Josiphiah/cafe-pos/issues/6) | **All of `docs/`** (requirements, use-case specs, domain model, design class diagram, SSDs, ERD + 1NF→2NF→3NF walkthrough, test plan, user manual, final report). Plus the customer **profile page** `customers/detail.html` (the past-purchases table). |
| **Josiphiah** | [#3](https://github.com/Josiphiah/cafe-pos/issues/3) | The **receipt**: `sales/receipt.html` (café header, lines, Subtotal / VAT 16% / Total, customer name, Print button) + repo admin (close issues, fill the contribution table in the report). |

---

## 4. Confirm you're on the board

A few minutes after your first `push` to `main`, open
**https://github.com/Josiphiah/cafe-pos/graphs/contributors** — your avatar and commit
count should be there. Also check your issue shows your commits linked at the bottom.

If your commits show as a greyed-out name with no avatar, your `git config user.email`
was wrong — fix it (step 1.3) and your **next** commit will link correctly.

---

## 5. Contribution record

Everyone adds their own row to the table in `docs/09-final-report.md`:
name · role · issue number · what you built · number of commits.
