-- V11: Seed Website Dummy Orders & Order Items from Sage & Bloom Storefront
INSERT INTO orders (
    id, order_number, user_id, customer_email, customer_name, customer_phone,
    shipping_address, shipping_city, shipping_postal_code, status, payment_status,
    subtotal, shipping_fee, total_amount, notes, created_at, updated_at
) VALUES
(
    'c0000000-0000-0000-0000-000000000101',
    '#192541',
    'a0000000-0000-0000-0000-000000000001',
    'esther.howard@example.com',
    'Esther Howard',
    '+1 (415) 555-2671',
    '742 Evergreen Terrace, Apt 4B',
    'Portland, OR',
    '97201',
    'PAID',
    'PAID',
    127.00,
    0.00,
    127.00,
    'Customer requested eco-friendly Kraft packaging with botanical dried lavender sprig.',
    '2026-06-19 10:15:00+00',
    '2026-06-19 10:15:00+00'
),
(
    'c0000000-0000-0000-0000-000000000102',
    '#192540',
    NULL,
    'david.miller@example.com',
    'David Miller',
    '+1 (206) 555-9014',
    'Botanical Atelier Pickup Hub #2',
    'Seattle, WA',
    '98101',
    'PAID',
    'PAID',
    94.50,
    0.00,
    94.50,
    'In-store pickup requested at Hub #2.',
    '2026-06-19 09:30:00+00',
    '2026-06-19 09:30:00+00'
),
(
    'c0000000-0000-0000-0000-000000000103',
    '#192539',
    NULL,
    'brodrigues@gmail.com',
    'James Moore',
    '+1 (415) 555-2671',
    '1490 Mission St, Suite 400',
    'San Francisco, CA',
    '94103',
    'PAID',
    'PAID',
    1927.89,
    0.00,
    1927.89,
    'VIP corporate artisan gift set delivery.',
    '2026-06-19 08:45:00+00',
    '2026-06-19 08:45:00+00'
),
(
    'c0000000-0000-0000-0000-000000000104',
    '#192538',
    NULL,
    'robert.a@example.com',
    'Robert Anderson',
    '+1 (512) 555-3401',
    '88 Meadowlane Boulevard',
    'Austin, TX',
    '78701',
    'PAID',
    'PAID',
    62.00,
    0.00,
    62.00,
    'Leave at front porch inside weatherproof tote.',
    '2026-06-19 07:20:00+00',
    '2026-06-19 07:20:00+00'
),
(
    'c0000000-0000-0000-0000-000000000105',
    '#192537',
    NULL,
    'jessica.m@example.com',
    'Jessica Martinez',
    '+1 (303) 555-7788',
    '450 Sunset Dr',
    'Boulder, CO',
    '80302',
    'REFUNDED',
    'REFUNDED',
    48.00,
    0.00,
    48.00,
    'Customer cancelled prior to shipment dispatch.',
    '2026-06-18 16:20:00+00',
    '2026-06-18 16:20:00+00'
),
(
    'c0000000-0000-0000-0000-000000000106',
    '#192536',
    NULL,
    'w.jackson@example.com',
    'William Jackson',
    '+1 (617) 555-1122',
    '32 Commonwealth Ave',
    'Boston, MA',
    '02116',
    'PAID',
    'PAID',
    185.00,
    0.00,
    185.00,
    'Gift wrap with botanical pressed flower card.',
    '2026-06-18 14:10:00+00',
    '2026-06-18 14:10:00+00'
),
(
    'c0000000-0000-0000-0000-000000000107',
    '#192535',
    NULL,
    'c.harris@example.com',
    'Christopher Harris',
    '+1 (312) 555-4490',
    'Botanical Atelier Pickup Hub #1',
    'Chicago, IL',
    '60601',
    'PAID',
    'PAID',
    310.00,
    0.00,
    310.00,
    'In-store pickup order reserved for Saturday morning.',
    '2026-06-18 12:00:00+00',
    '2026-06-18 12:00:00+00'
),
(
    'c0000000-0000-0000-0000-000000000108',
    '#192534',
    NULL,
    'm.kenter@example.com',
    'Marcus Kenter',
    '+1 (404) 555-8833',
    '102 Peachtree St',
    'Atlanta, GA',
    '30303',
    'PAID',
    'PAID',
    75.00,
    0.00,
    75.00,
    NULL,
    '2026-06-18 10:45:00+00',
    '2026-06-18 10:45:00+00'
),
(
    'c0000000-0000-0000-0000-000000000109',
    '#192533',
    NULL,
    'j.thompson@example.com',
    'Joshua Thompson',
    '+1 (602) 555-6677',
    '55 Desert Palm Way',
    'Phoenix, AZ',
    '85001',
    'CANCELLED',
    'FAILED',
    92.00,
    0.00,
    92.00,
    'Payment gateway authorization declined by issuing bank.',
    '2026-06-18 09:15:00+00',
    '2026-06-18 09:15:00+00'
),
(
    'c0000000-0000-0000-0000-000000000110',
    '#192532',
    NULL,
    'megan.martin@example.com',
    'Megan Martin',
    '+1 (214) 555-3344',
    '800 Main Street',
    'Dallas, TX',
    '75201',
    'PAID',
    'PAID',
    1528.60,
    0.00,
    1528.60,
    'Workshop collector edition bundle shipment.',
    '2026-06-18 08:00:00+00',
    '2026-06-18 08:00:00+00'
),
(
    'c0000000-0000-0000-0000-000000000111',
    '#192531',
    NULL,
    'd.garcia@example.com',
    'Daniel Garcia',
    '+1 (305) 555-9988',
    '120 Ocean Drive',
    'Miami, FL',
    '33139',
    'PAID',
    'PAID',
    158.00,
    0.00,
    158.00,
    'Express priority courier requested.',
    '2026-06-18 07:15:00+00',
    '2026-06-18 07:15:00+00'
)
ON CONFLICT (order_number) DO NOTHING;

-- Seed Order Items associated with each order
INSERT INTO order_items (order_id, product_id, product_title, quantity, unit_price, total_price)
VALUES
-- Order #192541 items
('c0000000-0000-0000-0000-000000000101', 'b0000000-0000-0000-0000-000000000001', 'Woodland Botanical Bar', 3, 14.00, 42.00),
('c0000000-0000-0000-0000-000000000101', 'b0000000-0000-0000-0000-000000000004', 'Pink Himalayan Rose Bar', 2, 15.50, 31.00),
('c0000000-0000-0000-0000-000000000101', 'b0000000-0000-0000-0000-000000000003', 'Wild Herbal & Slate Bar', 3, 18.00, 54.00),

-- Order #192540 items
('c0000000-0000-0000-0000-000000000102', 'b0000000-0000-0000-0000-000000000006', 'Amber Honey & Saffron Bar', 3, 16.50, 49.50),
('c0000000-0000-0000-0000-000000000102', 'b0000000-0000-0000-0000-000000000008', 'Botanical Garden Spectrum', 3, 15.00, 45.00),

-- Order #192539 items
('c0000000-0000-0000-0000-000000000103', 'b0000000-0000-0000-0000-000000000010', 'Artisan Master Botanical Gift Set (12 pcs)', 1, 1590.00, 1590.00),
('c0000000-0000-0000-0000-000000000103', 'b0000000-0000-0000-0000-000000000013', 'Botanical Essential Distiller & Diffuser', 1, 337.89, 337.89),

-- Order #192538 items
('c0000000-0000-0000-0000-000000000104', 'b0000000-0000-0000-0000-000000000003', 'Wild Herbal & Slate Bar', 2, 15.00, 30.00),
('c0000000-0000-0000-0000-000000000104', 'b0000000-0000-0000-0000-000000000008', 'Botanical Garden Spectrum', 2, 16.00, 32.00),

-- Order #192537 items
('c0000000-0000-0000-0000-000000000105', 'b0000000-0000-0000-0000-000000000001', 'Woodland Botanical Bar', 3, 16.00, 48.00),

-- Order #192536 items
('c0000000-0000-0000-0000-000000000106', 'b0000000-0000-0000-0000-000000000006', 'Amber Honey & Saffron Bar', 5, 16.50, 82.50),
('c0000000-0000-0000-0000-000000000106', 'b0000000-0000-0000-0000-000000000004', 'Pink Himalayan Rose Bar', 5, 20.50, 102.50),

-- Order #192535 items
('c0000000-0000-0000-0000-000000000107', 'b0000000-0000-0000-0000-000000000008', 'Botanical Garden Spectrum', 10, 16.00, 160.00),
('c0000000-0000-0000-0000-000000000107', 'b0000000-0000-0000-0000-000000000003', 'Wild Herbal & Slate Bar', 10, 15.00, 150.00),

-- Order #192534 items
('c0000000-0000-0000-0000-000000000108', 'b0000000-0000-0000-0000-000000000001', 'Woodland Botanical Bar', 5, 15.00, 75.00),

-- Order #192533 items
('c0000000-0000-0000-0000-000000000109', 'b0000000-0000-0000-0000-000000000004', 'Pink Himalayan Rose Bar', 4, 23.00, 92.00),

-- Order #192532 items
('c0000000-0000-0000-0000-000000000110', 'b0000000-0000-0000-0000-000000000013', 'Artisan Soap Workshop Collector Edition', 1, 1200.00, 1200.00),
('c0000000-0000-0000-0000-000000000110', 'b0000000-0000-0000-0000-000000000006', 'Amber Honey & Saffron Bar', 12, 16.50, 198.00),
('c0000000-0000-0000-0000-000000000110', 'b0000000-0000-0000-0000-000000000003', 'Wild Herbal & Slate Bar', 8, 16.32, 130.60),

-- Order #192531 items
('c0000000-0000-0000-0000-000000000111', 'b0000000-0000-0000-0000-000000000001', 'Woodland Botanical Bar', 5, 14.00, 70.00),
('c0000000-0000-0000-0000-000000000111', 'b0000000-0000-0000-0000-000000000004', 'Pink Himalayan Rose Bar', 5, 17.60, 88.00)
ON CONFLICT DO NOTHING;
