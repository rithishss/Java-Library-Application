-- analytics.sql
-- Sales analytics for the BookCartFX store.
--
-- Read-only. Every statement here is a SELECT; nothing writes, updates or
-- deletes, so it is safe to run against the live database.
--
-- Run it from the project root, the folder that holds bookstore.db:
--
--     sqlite3 bookstore.db < sql/analytics.sql
--
-- To capture it instead of printing it:
--
--     sqlite3 bookstore.db < sql/analytics.sql > analytics-output.txt
--
-- Two conventions carried over from the application, both of which have to stay
-- in step with the Java if you change them there:
--
--   * 100 points are worth $1 when redeemed  (Customer.redeemPoints)
--   * Gold status starts at 1000 points      (GoldCustomer / SilverCustomer)
--
-- A note on revenue. orders.total is what the customer actually paid, after any
-- points were applied. order_items.price is the list price at the moment of
-- sale. The difference between the two is the discount funded by the loyalty
-- scheme, which is why both are reported rather than just one.

.headers on
.mode box

.print
.print ================================================================
.print  1. HEADLINE FIGURES
.print ================================================================

SELECT
    (SELECT COUNT(*) FROM orders)                                   AS orders,
    (SELECT COUNT(DISTINCT username) FROM orders)                   AS buying_customers,
    (SELECT COUNT(*) FROM customers)                                AS all_customers,
    printf('$%.2f', (SELECT COALESCE(SUM(total), 0) FROM orders))   AS revenue,
    printf('$%.2f', (SELECT COALESCE(AVG(total), 0) FROM orders))   AS avg_order,
    printf('$%.2f', (SELECT COALESCE(MAX(total), 0) FROM orders))   AS largest_order;

.print
.print ================================================================
.print  2. LIST PRICE vs WHAT WAS ACTUALLY PAID
.print ================================================================

SELECT
    printf('$%.2f', (SELECT COALESCE(SUM(price), 0) FROM order_items))  AS list_price_sold,
    printf('$%.2f', (SELECT COALESCE(SUM(total), 0) FROM orders))       AS actually_paid,
    printf('$%.2f', (SELECT COALESCE(SUM(price), 0) FROM order_items)
                  - (SELECT COALESCE(SUM(total), 0) FROM orders))       AS funded_by_points,
    printf('%.1f%%', CASE
        WHEN (SELECT COALESCE(SUM(price), 0) FROM order_items) = 0 THEN 0
        ELSE 100.0 * ((SELECT COALESCE(SUM(price), 0) FROM order_items)
                    - (SELECT COALESCE(SUM(total), 0) FROM orders))
                  / (SELECT SUM(price) FROM order_items)
    END)                                                                AS discount_rate;

.print
.print ================================================================
.print  3. BEST SELLING TITLES  (top 15)
.print ================================================================

SELECT
    i.title                             AS title,
    i.author                            AS author,
    COUNT(*)                            AS copies,
    printf('$%.2f', SUM(i.price))       AS list_value,
    MIN(date(o.ordered_at))             AS first_sold,
    MAX(date(o.ordered_at))             AS last_sold
FROM order_items i
JOIN orders o ON o.id = i.order_id
GROUP BY i.title, i.author
ORDER BY copies DESC, SUM(i.price) DESC
LIMIT 15;

.print
.print ================================================================
.print  4. REVENUE BY DAY  (last 30 days of trading)
.print ================================================================
.print  Section 11 has the whole history, month by month.

SELECT
    date(ordered_at)                    AS day,
    COUNT(*)                            AS orders,
    printf('$%.2f', SUM(total))         AS revenue,
    SUM(points_earned)                  AS points_earned,
    SUM(points_redeemed)                AS points_redeemed
FROM orders
WHERE ordered_at >= date((SELECT MAX(ordered_at) FROM orders), '-30 day')
GROUP BY date(ordered_at)
ORDER BY day DESC;

.print
.print ================================================================
.print  5. CUSTOMERS BY SPEND
.print ================================================================
.print  Customers who have never ordered are included, with a zero.

SELECT
    c.username                                      AS customer,
    COUNT(o.id)                                     AS orders,
    printf('$%.2f', COALESCE(SUM(o.total), 0))      AS spent,
    c.points                                        AS points_balance,
    CASE WHEN c.points >= 1000 THEN 'Gold' ELSE 'Silver' END AS tier
FROM customers c
LEFT JOIN orders o ON o.username = c.username
GROUP BY c.username, c.points
ORDER BY COALESCE(SUM(o.total), 0) DESC, c.username;

.print
.print ================================================================
.print  6. THE POINTS ECONOMY
.print ================================================================
.print  Outstanding points are a liability: they are a discount not yet taken.

SELECT
    (SELECT COALESCE(SUM(points_earned), 0) FROM orders)    AS points_issued,
    (SELECT COALESCE(SUM(points_redeemed), 0) FROM orders)  AS points_spent,
    (SELECT COALESCE(SUM(points), 0) FROM customers)        AS points_outstanding,
    printf('$%.2f', (SELECT COALESCE(SUM(points), 0) FROM customers) / 100.0)
                                                            AS outstanding_value;

.print
.print ================================================================
.print  7. GOLD vs SILVER
.print ================================================================
.print  Careful reading this one. Revenue is net of points, so a customer who
.print  redeems heavily books very little revenue and can make the Gold tier
.print  look worse than Silver. Section 2 is where the redeemed value shows up.

SELECT
    CASE WHEN c.points >= 1000 THEN 'Gold' ELSE 'Silver' END AS tier,
    COUNT(DISTINCT c.username)                      AS customers,
    COUNT(o.id)                                     AS orders,
    printf('$%.2f', COALESCE(SUM(o.total), 0))      AS revenue,
    printf('$%.2f', CASE WHEN COUNT(o.id) = 0 THEN 0
                         ELSE COALESCE(SUM(o.total), 0) / COUNT(o.id) END) AS avg_order
FROM customers c
LEFT JOIN orders o ON o.username = c.username
GROUP BY tier
ORDER BY tier;

.print
.print ================================================================
.print  8. STOCK THAT HAS NEVER SOLD
.print ================================================================

SELECT
    b.title                     AS title,
    b.author                    AS author,
    printf('$%.2f', b.price)    AS price
FROM books b
WHERE NOT EXISTS (SELECT 1 FROM order_items i WHERE i.book_id = b.id)
ORDER BY b.price DESC, b.title;

.print
.print ================================================================
.print  9. SOLD, BUT NO LONGER IN THE CATALOGUE
.print ================================================================
.print  order_items keeps its own copy of the title and price, so these still
.print  report correctly after the owner deleted the book.

SELECT
    i.title                             AS title,
    i.author                            AS author,
    COUNT(*)                            AS copies,
    printf('$%.2f', SUM(i.price))       AS list_value
FROM order_items i
WHERE i.book_id IS NULL
GROUP BY i.title, i.author
ORDER BY copies DESC, i.title;

.print
.print ================================================================
.print  10. REVENUE BY CATEGORY
.print ================================================================
.print  Titles the owner has since deleted group under (retired), because the
.print  category lived on the book row and went with it.

SELECT
    CASE WHEN i.book_id IS NULL THEN '(retired)'
         ELSE COALESCE(b.category, 'Uncategorised') END  AS category,
    COUNT(DISTINCT i.title)                              AS titles,
    COUNT(*)                                             AS copies,
    printf('$%.2f', SUM(i.price))                        AS list_value,
    printf('$%.2f', AVG(i.price))                        AS avg_price
FROM order_items i
LEFT JOIN books b ON b.id = i.book_id
GROUP BY category
ORDER BY SUM(i.price) DESC;

.print
.print ================================================================
.print  11. MONTH BY MONTH
.print ================================================================

SELECT
    strftime('%Y-%m', ordered_at)   AS month,
    COUNT(*)                        AS orders,
    printf('$%.2f', SUM(total))     AS revenue,
    printf('$%.2f', AVG(total))     AS avg_order,
    SUM(points_redeemed)            AS points_redeemed
FROM orders
GROUP BY month
ORDER BY month;

.print
.print ================================================================
.print  12. HOW CONCENTRATED IS THE REVENUE
.print ================================================================
.print  Customers grouped by how many times they have ordered.

SELECT
    CASE
        WHEN n = 1              THEN '1 order'
        WHEN n BETWEEN 2 AND 5  THEN '2 to 5'
        WHEN n BETWEEN 6 AND 25 THEN '6 to 25'
        WHEN n BETWEEN 26 AND 99 THEN '26 to 99'
        ELSE '100 or more'
    END                                                         AS band,
    COUNT(*)                                                    AS customers,
    SUM(n)                                                      AS orders,
    printf('%.1f%%', 100.0 * SUM(n) / (SELECT COUNT(*) FROM orders))
                                                                AS share_of_orders
FROM (SELECT username, COUNT(*) AS n FROM orders GROUP BY username)
GROUP BY band
ORDER BY MIN(n);

.print
.print ================================================================
.print  13. RETENTION BY SIGN-UP MONTH
.print ================================================================
.print  still_buying counts people from that cohort who have ordered in the
.print  last 60 days of trading. A cohort at zero has churned completely.

SELECT
    strftime('%Y-%m', c.joined_at)                      AS cohort,
    COUNT(DISTINCT c.username)                          AS customers,
    COUNT(o.id)                                         AS orders,
    printf('$%.2f', COALESCE(SUM(o.total), 0))          AS revenue,
    COUNT(DISTINCT CASE
        WHEN o.ordered_at >= date((SELECT MAX(ordered_at) FROM orders), '-60 day')
        THEN c.username END)                            AS still_buying
FROM customers c
LEFT JOIN orders o ON o.username = c.username
GROUP BY cohort
ORDER BY cohort;

.print
