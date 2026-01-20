-- ============================================
-- CLEAR EXISTING DATA
-- ============================================
TRUNCATE TABLE purchases RESTART IDENTITY CASCADE;
TRUNCATE TABLE games RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- ============================================
-- INSERT TEST USERS
-- ============================================

INSERT INTO users (user_id, username, email, password, country, registration_date, last_login, active)
VALUES 
    (gen_random_uuid()::text, 'alice', 'alice@example.com', 'password123', 'USA', NOW() - INTERVAL '30 days', NOW() - INTERVAL '1 day', true),
    (gen_random_uuid()::text, 'bob', 'bob@example.com', 'password123', 'Canada', NOW() - INTERVAL '25 days', NOW() - INTERVAL '2 days', true),
    (gen_random_uuid()::text, 'charlie', 'charlie@example.com', 'password123', 'UK', NOW() - INTERVAL '20 days', NOW() - INTERVAL '3 days', true),
    (gen_random_uuid()::text, 'diana', 'diana@example.com', 'password123', 'Australia', NOW() - INTERVAL '15 days', NOW() - INTERVAL '5 days', true),
    (gen_random_uuid()::text, 'eve', 'eve@example.com', 'password123', 'Germany', NOW() - INTERVAL '10 days', NULL, true),
    (gen_random_uuid()::text, 'frank', 'frank@example.com', 'password123', 'France', NOW() - INTERVAL '5 days', NOW(), true);

-- ============================================
-- INSERT TEST GAMES
-- ============================================

INSERT INTO games (game_id, title, publisher, platform, genre, release_year, price, version, available, last_updated, description)
VALUES 
    (gen_random_uuid()::text, 'The Witcher 3: Wild Hunt', 'CD Projekt Red', 'PC', 'RPG', 2015, 39.99, '1.32', true, NOW(), 'Epic fantasy RPG with deep storyline and stunning graphics'),
    (gen_random_uuid()::text, 'Cyberpunk 2077', 'CD Projekt Red', 'PC', 'RPG', 2020, 59.99, '2.1', true, NOW(), 'Open-world action-adventure set in Night City'),
    (gen_random_uuid()::text, 'Elden Ring', 'FromSoftware', 'PC', 'Action', 2022, 49.99, '1.10', true, NOW(), 'Dark fantasy action RPG from the creators of Dark Souls'),
    (gen_random_uuid()::text, 'God of War', 'Santa Monica Studio', 'PlayStation', 'Action', 2018, 49.99, '1.0', true, NOW(), 'Norse mythology action-adventure with Kratos'),
    (gen_random_uuid()::text, 'Halo Infinite', 'Xbox Game Studios', 'Xbox', 'Shooter', 2021, 59.99, '1.0', true, NOW(), 'Sci-fi first-person shooter featuring Master Chief'),
    (gen_random_uuid()::text, 'FIFA 24', 'EA Sports', 'PC', 'Sports', 2023, 69.99, '1.5', true, NOW(), 'Ultimate football simulation game'),
    (gen_random_uuid()::text, 'Call of Duty: Modern Warfare', 'Activision', 'PC', 'Shooter', 2023, 69.99, '1.0', true, NOW(), 'Modern military first-person shooter'),
    (gen_random_uuid()::text, 'Minecraft', 'Mojang Studios', 'PC', 'Sandbox', 2011, 26.95, '1.20', true, NOW(), 'Open-world creative sandbox building game'),
    (gen_random_uuid()::text, 'Grand Theft Auto V', 'Rockstar Games', 'PC', 'Action', 2013, 29.99, '1.0', true, NOW(), 'Open-world action-adventure in Los Santos'),
    (gen_random_uuid()::text, 'Red Dead Redemption 2', 'Rockstar Games', 'PlayStation', 'Action', 2018, 59.99, '1.0', false, NOW(), 'Western action-adventure - Currently unavailable'),
    (gen_random_uuid()::text, 'Starfield', 'Bethesda', 'PC', 'RPG', 2023, 69.99, '1.7', true, NOW(), 'Space exploration RPG'),
    (gen_random_uuid()::text, 'Fortnite', 'Epic Games', 'PC', 'Shooter', 2017, 0.00, '27.0', true, NOW(), 'Battle royale free-to-play shooter'),
    (gen_random_uuid()::text, 'Apex Legends', 'Respawn Entertainment', 'PC', 'Shooter', 2019, 0.00, '19.0', true, NOW(), 'Hero-based battle royale shooter'),
    (gen_random_uuid()::text, 'League of Legends', 'Riot Games', 'PC', 'MOBA', 2009, 0.00, '13.24', true, NOW(), 'Competitive multiplayer online battle arena'),
    (gen_random_uuid()::text, 'Valorant', 'Riot Games', 'PC', 'Shooter', 2020, 0.00, '7.11', true, NOW(), 'Tactical 5v5 character-based shooter');

-- ============================================
-- INSERT TEST PURCHASES
-- ============================================

-- Alice purchases
INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'alice'),
    (SELECT game_id FROM games WHERE title = 'The Witcher 3: Wild Hunt'),
    (SELECT price FROM games WHERE title = 'The Witcher 3: Wild Hunt'),
    NOW() - INTERVAL '20 days',
    'Credit Card',
    'US';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'alice'),
    (SELECT game_id FROM games WHERE title = 'Cyberpunk 2077'),
    (SELECT price FROM games WHERE title = 'Cyberpunk 2077'),
    NOW() - INTERVAL '15 days',
    'PayPal',
    'US';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'alice'),
    (SELECT game_id FROM games WHERE title = 'Minecraft'),
    (SELECT price FROM games WHERE title = 'Minecraft'),
    NOW() - INTERVAL '10 days',
    'Credit Card',
    'US';

-- Bob purchases
INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'bob'),
    (SELECT game_id FROM games WHERE title = 'Elden Ring'),
    (SELECT price FROM games WHERE title = 'Elden Ring'),
    NOW() - INTERVAL '18 days',
    'Debit Card',
    'CA';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'bob'),
    (SELECT game_id FROM games WHERE title = 'FIFA 24'),
    (SELECT price FROM games WHERE title = 'FIFA 24'),
    NOW() - INTERVAL '12 days',
    'Credit Card',
    'CA';

-- Charlie purchases
INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'charlie'),
    (SELECT game_id FROM games WHERE title = 'Call of Duty: Modern Warfare'),
    (SELECT price FROM games WHERE title = 'Call of Duty: Modern Warfare'),
    NOW() - INTERVAL '16 days',
    'PayPal',
    'UK';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'charlie'),
    (SELECT game_id FROM games WHERE title = 'Grand Theft Auto V'),
    (SELECT price FROM games WHERE title = 'Grand Theft Auto V'),
    NOW() - INTERVAL '8 days',
    'Credit Card',
    'UK';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'charlie'),
    (SELECT game_id FROM games WHERE title = 'Minecraft'),
    (SELECT price FROM games WHERE title = 'Minecraft'),
    NOW() - INTERVAL '5 days',
    'PayPal',
    'UK';

-- Diana purchases
INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'diana'),
    (SELECT game_id FROM games WHERE title = 'Starfield'),
    (SELECT price FROM games WHERE title = 'Starfield'),
    NOW() - INTERVAL '10 days',
    'Credit Card',
    'AU';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'diana'),
    (SELECT game_id FROM games WHERE title = 'The Witcher 3: Wild Hunt'),
    (SELECT price FROM games WHERE title = 'The Witcher 3: Wild Hunt'),
    NOW() - INTERVAL '7 days',
    'PayPal',
    'AU';

-- Eve purchases
INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'eve'),
    (SELECT game_id FROM games WHERE title = 'Valorant'),
    (SELECT price FROM games WHERE title = 'Valorant'),
    NOW() - INTERVAL '5 days',
    'Credit Card',
    'DE';

-- Frank purchases (recent purchases)
INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'frank'),
    (SELECT game_id FROM games WHERE title = 'Cyberpunk 2077'),
    (SELECT price FROM games WHERE title = 'Cyberpunk 2077'),
    NOW() - INTERVAL '3 days',
    'Credit Card',
    'FR';

INSERT INTO purchases (purchase_id, user_id, game_id, price, purchase_date, payment_method, region)
SELECT 
    gen_random_uuid()::text,
    (SELECT user_id FROM users WHERE username = 'frank'),
    (SELECT game_id FROM games WHERE title = 'Elden Ring'),
    (SELECT price FROM games WHERE title = 'Elden Ring'),
    NOW() - INTERVAL '1 day',
    'PayPal',
    'FR';

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Show all users
SELECT user_id, username, email, country, registration_date, active FROM users ORDER BY registration_date DESC;

-- Show all games
SELECT game_id, title, publisher, platform, genre, price, available FROM games ORDER BY title;

-- Show all purchases with details
SELECT 
    p.purchase_id,
    u.username,
    g.title as game_title,
    p.price,
    p.purchase_date,
    p.payment_method,
    p.region
FROM purchases p
JOIN users u ON p.user_id = u. user_id
JOIN games g ON p.game_id = g. game_id
ORDER BY p. purchase_date DESC;

-- Summary statistics
SELECT 
    'Users' as entity, 
    COUNT(*) as total 
FROM users
UNION ALL
SELECT 
    'Games' as entity, 
    COUNT(*) as total 
FROM games
UNION ALL
SELECT 
    'Purchases' as entity, 
    COUNT(*) as total 
FROM purchases;

-- User purchase summary
SELECT 
    u.username,
    COUNT(p.purchase_id) as total_purchases,
    SUM(p.price) as total_spent
FROM users u
LEFT JOIN purchases p ON u.user_id = p.user_id
GROUP BY u.username
ORDER BY total_spent DESC;

-- Most purchased games
SELECT 
    g. title,
    COUNT(p. purchase_id) as purchase_count,
    g.price
FROM games g
LEFT JOIN purchases p ON g.game_id = p.game_id
GROUP BY g.title, g.price
ORDER BY purchase_count DESC;