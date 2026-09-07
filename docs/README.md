# Documentation set (Issue #6 — owner: Vanessa)

The full written deliverable. Everyone writes their own use-case spec into
`02-use-cases.md`; Goodson owns `07-test-plan.md`; Vanessa owns everything else
and assembles the rest.

| File | Contents | Status |
|---|---|---|
| `01-requirements.md` | Functional + non-functional requirements, assumptions, scope | ☐ to write |
| `02-use-cases.md` | Use-case diagram + specifications (Log In, Process Sale, Print Receipt, Look Up Transaction, Process Refund, Register Customer, View Customer Purchase History, Manage Menu) | ☐ to write |
| `03-domain-model.md` | Domain / conceptual class model | ☐ to write |
| `04-design-class-diagram.md` | Design class diagram (controllers, services, repositories, entities) | ☐ to write |
| `05-system-sequence-diagrams.md` | SSDs for Process Sale and Process Refund | ☐ to write |
| `06-database-design.md` | ERD, data dictionary, 1NF→2NF→3NF walkthrough, DDL | ✅ **draft ready — review against the code** |
| `07-test-plan.md` | Test cases + JUnit list (owner: Goodson) | ☐ to write |
| `08-user-manual.md` | Screenshot-led "how to use the POS" | ☐ after screens are built |
| `09-final-report.md` | Intro, tools used, architecture, **contribution table**, challenges, conclusion | ☐ to write |

Reference for `06`: the live entities are in
`src/main/java/zm/cafe/pos/domain/` — `Staff`, `Category`, `Product`, `Customer`,
`Sale`, `SaleLine`, `Refund`, `RefundLine`.
