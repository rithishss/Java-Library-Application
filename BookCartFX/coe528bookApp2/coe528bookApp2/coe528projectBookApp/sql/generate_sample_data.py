#!/usr/bin/env python3
"""Generates sql/sample-data.sql: a realistic demo dataset for the BookCartFX store.

    python3 sql/generate_sample_data.py

The random seed is fixed, so re-running this produces a byte-identical file. The
generated SQL is what actually gets loaded; this script is only needed if you
want to change the shape of the data.

The data is deliberately not uniform:

  * four heavy repeat buyers, then a long tail of people who bought once
  * a December lift, a milder November one, and a January dip
  * a cohort who joined early, bought for a few months and stopped, so
    retention decays instead of staying flat
  * three titles that sold and were later removed from the catalogue, which is
    what section 9 of analytics.sql reports on

Loyalty maths matches the application: 10 points earned per dollar spent, 100
points redeem for $1, Gold at 1000 points. Every customer's stored balance is
the exact sum of what their orders earned minus what they redeemed.
"""

import random
from datetime import datetime, timedelta

SEED = 20260811
TODAY = datetime(2026, 8, 11, 18, 0, 0)
MONTHS_OF_HISTORY = 18
TARGET_ORDERS = 2500
OUTPUT = "sql/sample-data.sql"

POINTS_PER_DOLLAR = 10          # earned per dollar, matches SceneController
REDEEM_POINTS_PER_DOLLAR = 100  # spent per dollar, matches Customer.redeemPoints

rng = random.Random(SEED)

# --------------------------------------------------------------------------
# Catalogue
# --------------------------------------------------------------------------

CATEGORIES = {
    "Fiction":    (11.99, 27.99),
    "Science":    (24.99, 54.99),
    "History":    (17.99, 41.99),
    "Technology": (34.99, 74.99),
    "Children":   (5.99, 15.99),
    "Poetry":     (9.99, 23.99),
}

TITLE_A = ["The Silent", "A Brief", "Notes on", "The Last", "Shadows of", "Letters from",
           "The Art of", "Beyond the", "The Hidden", "Chronicles of", "Under the",
           "The Weight of", "Fragments of", "The Long", "Return to", "Songs of"]
TITLE_B = ["Garden", "Machine", "Harvest", "Empire", "River", "Winter", "Signal", "Orchard",
           "Frontier", "Lantern", "Compass", "Meridian", "Archive", "Cathedral", "Tide",
           "Almanac", "Foundry", "Threshold", "Wilderness", "Cartographer"]
SURNAMES = ["Okonkwo", "Lindqvist", "Moreau", "Bhattacharya", "Alvarez", "Nakamura", "Whitfield",
            "Costa", "Petrov", "Haddad", "Ferreira", "Osei", "Larsen", "Vasquez", "Bianchi",
            "Novak", "Dlamini", "Kowalski", "Rahman", "Sorensen", "Mbeki", "Fontaine"]
INITIALS = ["A.", "C.", "D.", "E.", "H.", "J.", "K.", "L.", "M.", "N.", "R.", "S.", "T."]


def make_books():
    """60 in the catalogue, plus 3 that sold and were later deleted."""
    books, seen = [], set()
    # The two titles that came from the original books.txt, kept so the
    # catalogue the app shipped with is still there.
    books.append({"id": 1, "title": "Im dropping Eng", "author": "Tristan",
                  "price": 59.99, "category": "Technology", "retired": False})
    books.append({"id": 2, "title": "ABC's", "author": "Gary",
                  "price": 10.99, "category": "Children", "retired": False})
    seen.update(("Im dropping Eng", "ABC's"))

    next_id = 3
    per_category = {c: 0 for c in CATEGORIES}
    per_category["Technology"] = 1
    per_category["Children"] = 1

    while len(books) < 60:
        category = min(per_category, key=lambda c: (per_category[c], c))
        title = f"{rng.choice(TITLE_A)} {rng.choice(TITLE_B)}"
        if title in seen:
            continue
        seen.add(title)
        low, high = CATEGORIES[category]
        books.append({
            "id": next_id,
            "title": title,
            "author": f"{rng.choice(INITIALS)} {rng.choice(SURNAMES)}",
            "price": round(rng.uniform(low, high), 2),
            "category": category,
            "retired": False,
        })
        per_category[category] += 1
        next_id += 1

    for _ in range(3):
        title = f"{rng.choice(TITLE_A)} {rng.choice(TITLE_B)}"
        while title in seen:
            title = f"{rng.choice(TITLE_A)} {rng.choice(TITLE_B)}"
        seen.add(title)
        category = rng.choice(list(CATEGORIES))
        low, high = CATEGORIES[category]
        books.append({
            "id": next_id,
            "title": title,
            "author": f"{rng.choice(INITIALS)} {rng.choice(SURNAMES)}",
            "price": round(rng.uniform(low, high), 2),
            "category": category,
            "retired": True,
        })
        next_id += 1

    # Popularity is skewed: a handful of titles carry most of the volume.
    weights = [rng.paretovariate(1.4) for _ in books]
    rng.shuffle(weights)
    for book, weight in zip(books, weights):
        book["weight"] = weight

    # Three titles are dead stock and never sell at all, so section 8 of
    # analytics.sql has something to report. A real catalogue always has some.
    shelf_warmers = [b for b in books if not b["retired"] and b["id"] > 2]
    for book in rng.sample(shelf_warmers, 3):
        book["weight"] = 0.0

    return books


# --------------------------------------------------------------------------
# Customers
# --------------------------------------------------------------------------

FIRST_NAMES = ["amara", "declan", "priya", "tomas", "yusuf", "ingrid", "kofi", "leena",
               "marco", "nadia", "oscar", "rania", "sven", "thandi", "victor", "wen",
               "zoya", "elias", "farida", "gideon", "hana", "ivan", "jasmin", "kwame",
               "lucia", "mikael", "nour", "pablo", "quinn", "rosa", "samir", "tilda",
               "ugo", "vera", "willem", "xiomara", "yara"]

# username -> password, carried over from customers.txt so these logins still work
ORIGINALS = {"mary": "pass", "ritish": "hi", "tristan": "cooked"}

SEGMENTS = [
    # (label, count, orders_low, orders_high, active_months_low, active_months_high, joins_early)
    ("heavy",   4, 200, 260, 14, 18, True),
    ("regular", 8, 110, 155, 9, 17, False),
    ("lapsed", 10,  30,  60,  2,  5, True),
    # The tail is drawn from a power law rather than a flat range, so it runs
    # continuously from one-time buyers up to the twenties instead of leaving a
    # hole in the middle of the distribution.
    ("light",  18,   1,  25,  0,  4, False),
]

TAIL_SEGMENT = "light"
TAIL_CAP = 25


def month_factor(when):
    """Seasonality multiplier. December lifts hard, January sags."""
    return {12: 1.70, 11: 1.25, 10: 1.05, 1: 0.72, 2: 0.84, 7: 0.92}.get(when.month, 1.0)


def make_customers():
    names = list(ORIGINALS) + FIRST_NAMES
    customers, index = [], 0
    history_start = TODAY - timedelta(days=MONTHS_OF_HISTORY * 30)

    for label, count, lo, hi, act_lo, act_hi, joins_early in SEGMENTS:
        for _ in range(count):
            username = names[index]
            index += 1
            if joins_early:
                # First third of the window.
                offset = rng.uniform(0, MONTHS_OF_HISTORY * 30 * 0.35)
            else:
                # Weighted towards recent, so the store looks like it is growing.
                offset = (1 - rng.betavariate(1.6, 2.4)) * MONTHS_OF_HISTORY * 30
                offset = MONTHS_OF_HISTORY * 30 - offset
            joined = history_start + timedelta(days=offset,
                                               hours=rng.randint(8, 21),
                                               minutes=rng.randint(0, 59))
            if joined > TODAY - timedelta(days=1):
                joined = TODAY - timedelta(days=rng.randint(1, 20))

            active_days = rng.uniform(act_lo, act_hi) * 30
            ends = min(joined + timedelta(days=active_days), TODAY)

            if label == TAIL_SEGMENT:
                wanted = min(TAIL_CAP, max(lo, int(rng.paretovariate(1.15))))
            else:
                wanted = rng.randint(lo, hi)

            customers.append({
                "username": username,
                "password": ORIGINALS.get(username, "pw" + str(1000 + index)),
                "segment": label,
                "joined": joined,
                "ends": ends,
                "orders_wanted": wanted,
                "points": 0,
            })
    return customers


# --------------------------------------------------------------------------
# Orders
# --------------------------------------------------------------------------

def pick_date(customer):
    """A date in the customer's active window, biased by seasonality."""
    span = (customer["ends"] - customer["joined"]).total_seconds()
    if span <= 0:
        return customer["joined"]
    for _ in range(12):
        when = customer["joined"] + timedelta(seconds=rng.uniform(0, span))
        if rng.random() < month_factor(when) / 1.70:
            return when
    return customer["joined"] + timedelta(seconds=rng.uniform(0, span))


def make_orders(customers, books):
    titles = [b for b in books]
    weights = [b["weight"] for b in books]
    scale = TARGET_ORDERS / sum(c["orders_wanted"] for c in customers)

    orders = []
    for customer in customers:
        wanted = max(1, round(customer["orders_wanted"] * scale))
        for _ in range(wanted):
            book = rng.choices(titles, weights=weights, k=1)[0]
            price = book["price"]
            balance = customer["points"]

            redeemed = 0
            if balance >= 800 and rng.random() < 0.20:
                redeemed = min(balance, int(round(price * REDEEM_POINTS_PER_DOLLAR)))
            total = round(price - redeemed / float(REDEEM_POINTS_PER_DOLLAR), 2)
            earned = int(total * POINTS_PER_DOLLAR)
            customer["points"] = balance - redeemed + earned

            orders.append({
                "username": customer["username"],
                "when": pick_date(customer),
                "total": total,
                "earned": earned,
                "redeemed": redeemed,
                "book": book,
            })

    orders.sort(key=lambda o: o["when"])
    for position, order in enumerate(orders, start=1):
        order["id"] = position
    return orders


# --------------------------------------------------------------------------
# Emit
# --------------------------------------------------------------------------

def quote(value):
    return "'" + str(value).replace("'", "''") + "'"


def batched(rows, statement, size=200):
    out = []
    for start in range(0, len(rows), size):
        chunk = rows[start:start + size]
        out.append(statement + "\n" + ",\n".join(chunk) + ";")
    return "\n".join(out)


def main():
    books = make_books()
    customers = make_customers()
    orders = make_orders(customers, books)

    lines = [
        "-- sample-data.sql",
        "-- GENERATED FILE. Produced by sql/generate_sample_data.py with seed %d." % SEED,
        "-- Edit the generator, not this file.",
        "--",
        "-- WARNING: this REPLACES the contents of the store. It deletes every",
        "-- customer, book and order first, then loads the demo dataset. Back up",
        "-- bookstore.db before running it if there is anything in there you want.",
        "--",
        "--     sqlite3 bookstore.db < sql/sample-data.sql",
        "--",
        "-- %d customers, %d books in the catalogue (3 more sold then retired)," % (
            len(customers), len([b for b in books if not b['retired']])),
        "-- %d orders spanning %d months." % (len(orders), MONTHS_OF_HISTORY),
        "-- The mary / ritish / tristan logins from customers.txt are preserved.",
        "",
        "PRAGMA foreign_keys = ON;",
        "BEGIN TRANSACTION;",
        "",
        "DELETE FROM order_items;",
        "DELETE FROM orders;",
        "DELETE FROM books;",
        "DELETE FROM customers;",
        "",
    ]

    rows = ["(%s, %s, %d, %s)" % (quote(c["username"]), quote(c["password"]), c["points"],
                                  quote(c["joined"].strftime("%Y-%m-%d %H:%M:%S")))
            for c in customers]
    lines.append(batched(rows, "INSERT INTO customers (username, password, points, joined_at) VALUES"))
    lines.append("")

    rows = ["(%d, %s, %s, %.2f, %s)" % (b["id"], quote(b["title"]), quote(b["author"]),
                                        b["price"], quote(b["category"]))
            for b in books if not b["retired"]]
    lines.append(batched(rows, "INSERT INTO books (id, title, author, price, category) VALUES"))
    lines.append("")

    rows = ["(%d, %s, %s, %.2f, %d, %d)" % (
        o["id"], quote(o["username"]), quote(o["when"].strftime("%Y-%m-%d %H:%M:%S")),
        o["total"], o["earned"], o["redeemed"]) for o in orders]
    lines.append(batched(rows,
                         "INSERT INTO orders (id, username, ordered_at, total, points_earned, points_redeemed) VALUES"))
    lines.append("")

    rows = ["(%d, %s, %s, %s, %.2f)" % (
        o["id"],
        "NULL" if o["book"]["retired"] else str(o["book"]["id"]),
        quote(o["book"]["title"]), quote(o["book"]["author"]), o["book"]["price"])
        for o in orders]
    lines.append(batched(rows,
                         "INSERT INTO order_items (order_id, book_id, title, author, price) VALUES"))
    lines.append("")
    lines.append("COMMIT;")
    lines.append("")

    with open(OUTPUT, "w") as handle:
        handle.write("\n".join(lines))

    retired_sales = len([o for o in orders if o["book"]["retired"]])
    print("wrote %s" % OUTPUT)
    print("  customers %d, books %d (+3 retired), orders %d" % (
        len(customers), len([b for b in books if not b['retired']]), len(orders)))
    print("  orders against retired titles: %d" % retired_sales)
    print("  gold customers: %d" % len([c for c in customers if c["points"] >= 1000]))
    print("  date range: %s .. %s" % (orders[0]["when"].date(), orders[-1]["when"].date()))


if __name__ == "__main__":
    main()
