# Java-Library-Application

BookCartFX — a JavaFX bookstore with a SQLite backend. An owner manages books and
customers; a customer buys books, earns and redeems loyalty points, and reviews
their own order history.

Project root: `BookCartFX/coe528bookApp2/coe528bookApp2/coe528projectBookApp/`

## Getting started

The JavaFX SDK is a platform-specific download of about 50 MB, so it is not kept
in the repository and has to be fetched once after cloning. Everything else the
build needs is committed.

```bash
cd BookCartFX/coe528bookApp2/coe528bookApp2/coe528projectBookApp
./setup.sh
```

`setup.sh` works out your platform, downloads the matching JavaFX 25.0.1 bundle
and unpacks it into `lib/`. Running it again does nothing. On Windows without a
shell, download the bundle by hand from https://gluonhq.com/products/javafx/ and
unzip it into `lib/` so that `lib/javafx-sdk-25.0.1/lib/` exists.

Then open that same folder in NetBeans — the one containing `nbproject/`, not a
parent — and press **F6**. Or from a terminal:

```bash
ant run
```

Building without the SDK stops with a message telling you to run `setup.sh`,
rather than a hundred `package javafx.x does not exist` errors.

### First run

The database is created and seeded automatically. `bookstore.db` is not
committed; on first start the app creates the schema and imports `customers.txt`
and `books.txt`, then hashes the passwords it just imported. You get three
customers and two books, with no order history:

```
Migrated 3 customers from customers.txt
Migrated 2 books from books.txt
Hashed 3 plain text password(s)
```

Log in as `mary` / `pass`, `ritish` / `hi` or `tristan` / `cooked`. The owner is
`owner` / `admin123`.

That is enough to use the app, but it leaves the analytics with almost nothing to
report. To load the demo dataset instead — 40 customers and ~2,500 orders, which
is what the report further down was generated from:

```bash
sqlite3 bookstore.db < sql/sample-data.sql
```

---

## Sales analytics

Analytics live outside the application. There is no reporting screen and no
charts — the numbers come from a SQL file run against the database from a
terminal.

### Running it

From the project root, the folder holding `bookstore.db`:

```bash
sqlite3 bookstore.db < sql/analytics.sql
```

To save it to a file rather than print it:

```bash
sqlite3 bookstore.db < sql/analytics.sql > analytics-output.txt
```

`sql/analytics.sql` is read-only — every statement in it is a `SELECT`. SQLite
allows concurrent readers, so it is safe to run even while the app is open. The
only caveat is that a purchase committed midway through a run could leave two
sections disagreeing by one order.

### What each section answers

| # | Section | Question |
| --- | --- | --- |
| 1 | Headline figures | How many orders, from how many customers, worth how much? |
| 2 | List price vs paid | How much revenue is the loyalty scheme giving away? |
| 3 | Best selling titles | What sells, and when did it last sell? |
| 4 | Revenue by day | What has the last month of trading looked like? |
| 5 | Customers by spend | Who buys, who has never bought, and what tier are they? |
| 6 | The points economy | How many points are issued, spent, and still owed? |
| 7 | Gold vs Silver | Do Gold customers actually spend more per order? |
| 8 | Stock never sold | What is sitting in the catalogue untouched? |
| 9 | Sold, not in catalogue | What sold before the owner deleted it? |
| 10 | Revenue by category | Which categories carry the business? |
| 11 | Month by month | How does trade move across the year? |
| 12 | Revenue concentration | How much depends on the heaviest buyers? |
| 13 | Retention by cohort | Do sign-ups keep buying, or churn? |

Two conventions are shared with the Java and must stay in step with it:
**100 points = $1** when redeemed (`Customer.redeemPoints`), and **Gold starts at
1000 points** (`GoldCustomer` / `SilverCustomer`).

On revenue: `orders.total` is what the customer actually paid after points were
applied, while `order_items.price` is the list price at the moment of sale. Both
are reported, because the gap between them is the cost of the loyalty scheme.

### The dataset behind these numbers

> **These are generated figures, not real sales.** The store is a course project;
> the history below was produced so the analytics have something to chew on.

`sql/sample-data.sql` loads a demo dataset: 40 customers, 60 books across six
categories, and roughly 2,500 orders spanning 17 months. It is deliberately not
uniform — a dozen heavy repeat buyers carry most of the volume, a long tail of
people bought once and never returned, December lifts and January sags, an early
cohort churned completely, three titles are dead stock, and three more sold
before being deleted from the catalogue.

```bash
sqlite3 bookstore.db < sql/sample-data.sql
```

**That file replaces the contents of the store.** It deletes every customer, book
and order before loading. The `mary` / `ritish` / `tristan` logins from
`customers.txt` are preserved, as are the two books that shipped with it.

The SQL is generated by `sql/generate_sample_data.py` from a fixed random seed,
so regenerating it produces a byte-identical file. Edit the generator, not the
SQL, if you want a different shape of data.

Every customer's stored points balance is exactly the sum of what their orders
earned minus what they redeemed, so section 6 reconciles rather than being
decorative.

### Key figures

Four numbers worth pulling out, each with the query that produces it so you can
run it yourself and get the same answer. Every one is against the demo dataset
currently in `bookstore.db`.

**Volume — 2,495 orders across 40 customers**

```sql
SELECT (SELECT COUNT(*) FROM orders)    AS orders,
       (SELECT COUNT(*) FROM customers) AS customers;
```

```
┌────────┬───────────┐
│ orders │ customers │
├────────┼───────────┤
│ 2495   │ 40        │
└────────┴───────────┘
```

**Concentration — 12 customers account for 80.3% of all orders**

The Pareto shape of the store. Twelve people out of forty placed 2,003 of the
2,495 orders, so the business depends on a small group far more than the customer
count suggests.

```sql
SELECT COUNT(*) AS heavy_buyers,
       SUM(n)   AS their_orders,
       printf('%.1f%%', 100.0 * SUM(n) / (SELECT COUNT(*) FROM orders)) AS share_of_orders
FROM (SELECT username, COUNT(*) AS n
      FROM orders GROUP BY username HAVING n >= 100);
```

```
┌──────────────┬──────────────┬─────────────────┐
│ heavy_buyers │ their_orders │ share_of_orders │
├──────────────┼──────────────┼─────────────────┤
│ 12           │ 2003         │ 80.3%           │
└──────────────┴──────────────┴─────────────────┘
```

**The long tail — 9 customers ordered exactly once**

```sql
SELECT COUNT(*) AS one_time_buyers
FROM (SELECT username FROM orders GROUP BY username HAVING COUNT(*) = 1);
```

```
┌─────────────────┐
│ one_time_buyers │
├─────────────────┤
│ 9               │
└─────────────────┘
```

**Loyalty — 17 customers have reached Gold**

Gold is 1000 points, matching `GoldCustomer` / `SilverCustomer` in the Java.

```sql
SELECT COUNT(*)                                       AS gold_customers,
       (SELECT COUNT(*) FROM customers) - COUNT(*)    AS silver_customers
FROM customers WHERE points >= 1000;
```

```
┌────────────────┬──────────────────┐
│ gold_customers │ silver_customers │
├────────────────┼──────────────────┤
│ 17             │ 23               │
└────────────────┴──────────────────┘
```

Section 12 of the report below breaks the concentration out into bands, and
section 7 splits revenue by tier.

### Report

Generated 11 August 2026 from the demo dataset. Regenerate with the command above.

```
================================================================
1. HEADLINE FIGURES
================================================================
┌────────┬──────────────────┬───────────────┬───────────┬───────────┬───────────────┐
│ orders │ buying_customers │ all_customers │  revenue  │ avg_order │ largest_order │
├────────┼──────────────────┼───────────────┼───────────┼───────────┼───────────────┤
│ 2495   │ 40               │ 40            │ $65341.93 │ $26.19    │ $74.65        │
└────────┴──────────────────┴───────────────┴───────────┴───────────┴───────────────┘

================================================================
2. LIST PRICE vs WHAT WAS ACTUALLY PAID
================================================================
┌─────────────────┬───────────────┬──────────────────┬───────────────┐
│ list_price_sold │ actually_paid │ funded_by_points │ discount_rate │
├─────────────────┼───────────────┼──────────────────┼───────────────┤
│ $71390.32       │ $65341.93     │ $6048.39         │ 8.5%          │
└─────────────────┴───────────────┴──────────────────┴───────────────┘

================================================================
3. BEST SELLING TITLES (top 15)
================================================================
┌─────────────────────────┬─────────────────┬────────┬────────────┬────────────┬────────────┐
│          title          │     author      │ copies │ list_value │ first_sold │ last_sold  │
├─────────────────────────┼─────────────────┼────────┼────────────┼────────────┼────────────┤
│ The Weight of Orchard   │ N. Bhattacharya │ 237    │ $3860.73   │ 2025-05-03 │ 2026-08-11 │
│ A Brief Empire          │ J. Nakamura     │ 142    │ $3152.40   │ 2025-05-02 │ 2026-08-10 │
│ Letters from Wilderness │ S. Moreau       │ 137    │ $4303.17   │ 2025-04-28 │ 2026-08-09 │
│ The Art of Wilderness   │ A. Osei         │ 136    │ $6083.28   │ 2025-04-06 │ 2026-08-04 │
│ The Long Cartographer   │ S. Fontaine     │ 115    │ $5859.25   │ 2025-04-12 │ 2026-08-10 │
│ The Hidden Archive      │ L. Whitfield    │ 86     │ $994.16    │ 2025-04-22 │ 2026-08-05 │
│ The Last River          │ H. Rahman       │ 77     │ $3415.72   │ 2025-05-02 │ 2026-08-07 │
│ Notes on Empire         │ S. Okonkwo      │ 70     │ $795.90    │ 2025-04-20 │ 2026-08-10 │
│ Im dropping Eng         │ Tristan         │ 62     │ $3719.38   │ 2025-05-26 │ 2026-08-10 │
│ Songs of Cartographer   │ M. Moreau       │ 54     │ $797.04    │ 2025-04-30 │ 2026-07-14 │
│ Beyond the Orchard      │ D. Alvarez      │ 53     │ $1125.72   │ 2025-04-14 │ 2026-08-04 │
│ The Weight of Cathedral │ C. Dlamini      │ 50     │ $710.00    │ 2025-05-03 │ 2026-08-11 │
│ Letters from Frontier   │ K. Vasquez      │ 49     │ $1884.05   │ 2025-05-20 │ 2026-08-03 │
│ Fragments of Almanac    │ J. Osei         │ 47     │ $688.08    │ 2025-05-10 │ 2026-08-10 │
│ Under the Almanac       │ J. Novak        │ 45     │ $656.10    │ 2025-04-25 │ 2026-08-05 │
└─────────────────────────┴─────────────────┴────────┴────────────┴────────────┴────────────┘

================================================================
4. REVENUE BY DAY (last 30 days of trading)
================================================================
Section 11 has the whole history, month by month.
┌────────────┬────────┬─────────┬───────────────┬─────────────────┐
│    day     │ orders │ revenue │ points_earned │ points_redeemed │
├────────────┼────────┼─────────┼───────────────┼─────────────────┤
│ 2026-08-11 │ 4      │ $41.48  │ 413           │ 3312            │
│ 2026-08-10 │ 6      │ $178.94 │ 1786          │ 0               │
│ 2026-08-09 │ 6      │ $152.23 │ 1520          │ 1086            │
│ 2026-08-08 │ 5      │ $137.22 │ 1370          │ 0               │
│ 2026-08-07 │ 11     │ $368.03 │ 3675          │ 2374            │
│ 2026-08-06 │ 6      │ $135.89 │ 1356          │ 0               │
│ 2026-08-05 │ 11     │ $200.30 │ 1997          │ 3979            │
│ 2026-08-04 │ 2      │ $44.73  │ 447           │ 2124            │
│ 2026-08-03 │ 9      │ $244.04 │ 2438          │ 0               │
│ 2026-08-02 │ 8      │ $250.23 │ 2498          │ 2567            │
│ 2026-08-01 │ 4      │ $43.79  │ 437           │ 1486            │
│ 2026-07-31 │ 9      │ $128.59 │ 1283          │ 3093            │
│ 2026-07-30 │ 7      │ $108.47 │ 1082          │ 1538            │
│ 2026-07-29 │ 12     │ $405.21 │ 4047          │ 983             │
│ 2026-07-28 │ 5      │ $120.44 │ 1203          │ 0               │
│ 2026-07-27 │ 4      │ $98.03  │ 979           │ 1448            │
│ 2026-07-26 │ 9      │ $202.70 │ 2023          │ 2083            │
│ 2026-07-25 │ 2      │ $14.86  │ 148           │ 5999            │
│ 2026-07-24 │ 5      │ $153.72 │ 1535          │ 4063            │
│ 2026-07-23 │ 4      │ $133.32 │ 1332          │ 0               │
│ 2026-07-22 │ 6      │ $118.19 │ 1179          │ 1676            │
│ 2026-07-21 │ 3      │ $62.23  │ 621           │ 3950            │
│ 2026-07-20 │ 3      │ $48.17  │ 480           │ 3505            │
│ 2026-07-19 │ 6      │ $104.50 │ 1041          │ 5020            │
│ 2026-07-18 │ 1      │ $9.98   │ 99            │ 1222            │
│ 2026-07-17 │ 5      │ $175.22 │ 1750          │ 0               │
│ 2026-07-16 │ 7      │ $213.74 │ 2135          │ 1354            │
│ 2026-07-15 │ 6      │ $87.21  │ 868           │ 1281            │
│ 2026-07-14 │ 4      │ $67.37  │ 672           │ 3683            │
│ 2026-07-13 │ 2      │ $35.06  │ 349           │ 0               │
│ 2026-07-12 │ 5      │ $149.93 │ 1498          │ 1137            │
└────────────┴────────┴─────────┴───────────────┴─────────────────┘

================================================================
5. CUSTOMERS BY SPEND
================================================================
Customers who have never ordered are included, with a zero.
┌──────────┬────────┬──────────┬────────────────┬────────┐
│ customer │ orders │  spent   │ points_balance │  tier  │
├──────────┼────────┼──────────┼────────────────┼────────┤
│ tristan  │ 259    │ $6835.47 │ 4148           │ Gold   │
│ ritish   │ 243    │ $6437.70 │ 1622           │ Gold   │
│ mary     │ 228    │ $5850.83 │ 101            │ Silver │
│ amara    │ 203    │ $5286.34 │ 2103           │ Gold   │
│ tomas    │ 155    │ $4055.07 │ 882            │ Silver │
│ priya    │ 156    │ $3862.08 │ 1209           │ Gold   │
│ ingrid   │ 136    │ $3842.76 │ 1209           │ Gold   │
│ yusuf    │ 139    │ $3720.19 │ 2642           │ Gold   │
│ marco    │ 119    │ $3255.89 │ 1123           │ Gold   │
│ kofi     │ 118    │ $3170.18 │ 1402           │ Gold   │
│ declan   │ 121    │ $3139.16 │ 2094           │ Gold   │
│ leena    │ 126    │ $3029.19 │ 952            │ Silver │
│ thandi   │ 56     │ $1501.05 │ 674            │ Silver │
│ rania    │ 46     │ $1306.28 │ 2866           │ Gold   │
│ victor   │ 46     │ $1191.24 │ 3335           │ Gold   │
│ oscar    │ 48     │ $1178.68 │ 847            │ Silver │
│ zoya     │ 44     │ $1136.48 │ 1986           │ Gold   │
│ elias    │ 38     │ $1046.03 │ 2867           │ Gold   │
│ nadia    │ 36     │ $1019.12 │ 1124           │ Gold   │
│ farida   │ 36     │ $965.37  │ 3185           │ Gold   │
│ wen      │ 41     │ $962.06  │ 62             │ Silver │
│ sven     │ 42     │ $946.45  │ 1043           │ Gold   │
│ mikael   │ 24     │ $575.05  │ 981            │ Silver │
│ hana     │ 8      │ $184.26  │ 598            │ Silver │
│ vera     │ 4      │ $124.09  │ 1238           │ Gold   │
│ nour     │ 3      │ $91.08   │ 909            │ Silver │
│ gideon   │ 2      │ $89.87   │ 898            │ Silver │
│ quinn    │ 3      │ $76.94   │ 767            │ Silver │
│ tilda    │ 2      │ $71.54   │ 714            │ Silver │
│ ugo      │ 2      │ $68.47   │ 684            │ Silver │
│ samir    │ 1      │ $63.19   │ 631            │ Silver │
│ rosa     │ 1      │ $52.98   │ 529            │ Silver │
│ jasmin   │ 1      │ $50.95   │ 509            │ Silver │
│ lucia    │ 1      │ $44.36   │ 443            │ Silver │
│ kwame    │ 2      │ $37.69   │ 376            │ Silver │
│ yara     │ 1      │ $25.42   │ 254            │ Silver │
│ ivan     │ 1      │ $14.82   │ 148            │ Silver │
│ xiomara  │ 1      │ $14.76   │ 147            │ Silver │
│ willem   │ 1      │ $11.37   │ 113            │ Silver │
│ pablo    │ 1      │ $7.47    │ 74             │ Silver │
└──────────┴────────┴──────────┴────────────────┴────────┘

================================================================
6. THE POINTS ECONOMY
================================================================
Outstanding points are a liability: they are a discount not yet taken.
┌───────────────┬──────────────┬────────────────────┬───────────────────┐
│ points_issued │ points_spent │ points_outstanding │ outstanding_value │
├───────────────┼──────────────┼────────────────────┼───────────────────┤
│ 652328        │ 604839       │ 47489              │ $474.89           │
└───────────────┴──────────────┴────────────────────┴───────────────────┘

================================================================
7. GOLD vs SILVER
================================================================
Careful reading this one. Revenue is net of points, so a customer who
redeems heavily books very little revenue and can make the Gold tier
look worse than Silver. Section 2 is where the redeemed value shows up.
┌────────┬───────────┬────────┬───────────┬───────────┐
│  tier  │ customers │ orders │  revenue  │ avg_order │
├────────┼───────────┼────────┼───────────┼───────────┤
│ Gold   │ 17        │ 1786   │ $47284.83 │ $26.48    │
│ Silver │ 23        │ 709    │ $18057.10 │ $25.47    │
└────────┴───────────┴────────┴───────────┴───────────┘

================================================================
8. STOCK THAT HAS NEVER SOLD
================================================================
┌──────────────────────┬──────────────┬────────┐
│        title         │    author    │ price  │
├──────────────────────┼──────────────┼────────┤
│ Songs of Lantern     │ D. Whitfield │ $39.21 │
│ The Weight of Empire │ C. Petrov    │ $32.66 │
│ The Hidden Harvest   │ L. Osei      │ $20.68 │
└──────────────────────┴──────────────┴────────┘

================================================================
9. SOLD, BUT NO LONGER IN THE CATALOGUE
================================================================
order_items keeps its own copy of the title and price, so these still
report correctly after the owner deleted the book.
┌────────────────────┬──────────────┬────────┬────────────┐
│       title        │    author    │ copies │ list_value │
├────────────────────┼──────────────┼────────┼────────────┤
│ The Hidden Archive │ L. Whitfield │ 86     │ $994.16    │
│ Notes on Threshold │ H. Moreau    │ 22     │ $392.26    │
│ Songs of Tide      │ L. Vasquez   │ 18     │ $230.58    │
└────────────────────┴──────────────┴────────┴────────────┘

================================================================
10. REVENUE BY CATEGORY
================================================================
Titles the owner has since deleted group under (retired), because the
category lived on the book row and went with it.
┌────────────┬────────┬────────┬────────────┬───────────┐
│  category  │ titles │ copies │ list_value │ avg_price │
├────────────┼────────┼────────┼────────────┼───────────┤
│ Technology │ 10     │ 482    │ $25452.70  │ $52.81    │
│ Science    │ 10     │ 476    │ $18554.98  │ $38.98    │
│ Fiction    │ 10     │ 413    │ $8072.04   │ $19.54    │
│ Poetry     │ 10     │ 493    │ $7926.13   │ $16.08    │
│ History    │ 7      │ 209    │ $6375.13   │ $30.50    │
│ Children   │ 10     │ 296    │ $3392.34   │ $11.46    │
│ (retired)  │ 3      │ 126    │ $1617.00   │ $12.83    │
└────────────┴────────┴────────┴────────────┴───────────┘

================================================================
11. MONTH BY MONTH
================================================================
┌─────────┬────────┬──────────┬───────────┬─────────────────┐
│  month  │ orders │ revenue  │ avg_order │ points_redeemed │
├─────────┼────────┼──────────┼───────────┼─────────────────┤
│ 2025-04 │ 35     │ $1058.18 │ $30.23    │ 6227            │
│ 2025-05 │ 60     │ $1535.48 │ $25.59    │ 20013           │
│ 2025-06 │ 34     │ $828.36  │ $24.36    │ 6944            │
│ 2025-07 │ 47     │ $1383.48 │ $29.44    │ 1486            │
│ 2025-08 │ 175    │ $4643.77 │ $26.54    │ 36995           │
│ 2025-09 │ 235    │ $5909.69 │ $25.15    │ 59780           │
│ 2025-10 │ 167    │ $4272.21 │ $25.58    │ 38055           │
│ 2025-11 │ 222    │ $5840.33 │ $26.31    │ 40430           │
│ 2025-12 │ 273    │ $7167.68 │ $26.26    │ 70580           │
│ 2026-01 │ 99     │ $2863.12 │ $28.92    │ 17635           │
│ 2026-02 │ 117    │ $2951.50 │ $25.23    │ 35855           │
│ 2026-03 │ 149    │ $3808.64 │ $25.56    │ 34614           │
│ 2026-04 │ 193    │ $5050.36 │ $26.17    │ 48549           │
│ 2026-05 │ 235    │ $6812.42 │ $28.99    │ 52251           │
│ 2026-06 │ 199    │ $4815.43 │ $24.20    │ 62358           │
│ 2026-07 │ 183    │ $4604.40 │ $25.16    │ 56139           │
│ 2026-08 │ 72     │ $1796.88 │ $24.96    │ 16928           │
└─────────┴────────┴──────────┴───────────┴─────────────────┘

================================================================
12. HOW CONCENTRATED IS THE REVENUE
================================================================
Customers grouped by how many times they have ordered.
┌─────────────┬───────────┬────────┬─────────────────┐
│    band     │ customers │ orders │ share_of_orders │
├─────────────┼───────────┼────────┼─────────────────┤
│ 1 order     │ 9         │ 9      │ 0.4%            │
│ 2 to 5      │ 7         │ 18     │ 0.7%            │
│ 6 to 25     │ 2         │ 32     │ 1.3%            │
│ 26 to 99    │ 10        │ 433    │ 17.4%           │
│ 100 or more │ 12        │ 2003   │ 80.3%           │
└─────────────┴───────────┴────────┴─────────────────┘

================================================================
13. RETENTION BY SIGN-UP MONTH
================================================================
still_buying counts people from that cohort who have ordered in the
last 60 days of trading. A cohort at zero has churned completely.
┌─────────┬───────────┬────────┬───────────┬──────────────┐
│ cohort  │ customers │ orders │  revenue  │ still_buying │
├─────────┼───────────┼────────┼───────────┼──────────────┤
│ 2025-04 │ 6         │ 188    │ $5151.25  │ 0            │
│ 2025-07 │ 7         │ 503    │ $13247.56 │ 2            │
│ 2025-08 │ 15        │ 1230   │ $31775.97 │ 4            │
│ 2025-09 │ 3         │ 128    │ $3354.33  │ 1            │
│ 2025-11 │ 1         │ 118    │ $3170.18  │ 1            │
│ 2025-12 │ 1         │ 1      │ $52.98    │ 0            │
│ 2026-01 │ 2         │ 2      │ $114.14   │ 0            │
│ 2026-03 │ 1         │ 1      │ $11.37    │ 0            │
│ 2026-04 │ 3         │ 316    │ $8279.89  │ 3            │
│ 2026-06 │ 1         │ 8      │ $184.26   │ 1            │
└─────────┴───────────┴────────┴───────────┴──────────────┘

```

### Reading the report

**The business runs on a dozen people.** Section 12 is the sharpest number here:
12 customers out of 40 account for **80.3%** of all orders, while nine customers
have ordered exactly once between them. Losing one heavy buyer costs more than
losing every one-time buyer combined.

**December is the peak, January the trough.** Section 11 shows 273 orders in
December 2025 against 222 in November and **99 in January** — trade more than
halves in the new year. Any stock planning has to work around that.

**The loyalty scheme costs 8.5% of list price.** Section 2: $71,390.32 of books
went out at list, $65,341.93 was actually collected, so **$6,048.39** was funded
by points. Section 6 shows another **$474.89** still owed as unspent balances —
a discount already promised but not yet taken.

**Retention decays, visibly.** Section 13: the April 2025 cohort has six
customers, 188 orders and **nobody still buying**. The August 2025 cohort, the
largest at fifteen, still has four active. Cohorts stop contributing rather than
tapering, which is what the generated data was built to show.

**Technology carries the revenue, Children carries the volume.** Section 10:
Technology sold fewer copies than Children but at roughly four times the average
price, so it leads on value.

**Section 7 needs the caveat it carries.** Gold averages $26.48 an order against
Silver's $25.47 — close, because at this volume heavy redeemers are averaged in
with heavy spenders. On a smaller dataset a single fully-redeemed order can drag
the Gold average below Silver and make loyal customers look unprofitable, which
is why the section prints a warning pointing at section 2.

**Three titles are dead stock** (section 8) and **three more sold before being
deleted** (section 9). The second group still reports its title, author and price
correctly because `order_items` keeps its own copy at the time of sale rather
than reading through to the book row.

---

## Database

`bookstore.db` is created in the project root on first run and seeded from
`customers.txt` and `books.txt`. It is not committed — it regenerates from those
files on any fresh clone, or from `sql/sample-data.sql` for the demo dataset.

| Table | Holds |
| --- | --- |
| `customers` | username, password, points, join date |
| `books` | catalogue: title, author, price, category |
| `orders` | one row per completed purchase, with what was paid and the points moved |
| `order_items` | the books on an order, with title and price copied in at sale time |

`customers.joined_at` and `books.category` exist only for the analytics — no
screen reads or writes them. Books added through the app have no category, and
group under `Uncategorised` in section 10.

Passwords are stored as salted PBKDF2 hashes, in the form
`pbkdf2$100000$<salt>$<hash>`. The app hashes any plain text it finds on start
up, so `sql/sample-data.sql` and `customers.txt` can both keep readable
passwords — they are converted the next time the app runs. The owner's customer
table shows dots rather than the stored value.
