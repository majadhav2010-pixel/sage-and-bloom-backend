-- V7: Seed Initial Roles and Botanical Categories
INSERT INTO roles (name) VALUES 
('ROLE_CUSTOMER'),
('ROLE_ADMIN'),
('ROLE_SUPER_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Seed Categories
INSERT INTO categories (name, slug, description) VALUES
('Botanical Herbal', 'botanical-herbal', 'Herbal, earthy, and forest botanical soaps handcrafted with infused oils.'),
('Floral & Rose', 'floral-rose', 'Gentle floral essences with wild rosehip, lavender, and pink mineral salts.'),
('Exfoliating & Purifying', 'exfoliating-purifying', 'Activated charcoal, volcanic ash, and poppy seed exfoliating soaps.'),
('Nourishing & Honey', 'nourishing-honey', 'Warm amber honey, goat milk, and rich shea butter nourishing bars.'),
('Gift Sets & Collections', 'gift-sets', 'Curated handcrafted artisan gift sets and sampler packs.')
ON CONFLICT (slug) DO NOTHING;

-- Seed Initial Super Admin User (Password: Admin@2026! - BCrypt hash)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin@sageandbloom.com', '$2a$10$7Z2vY5n5hO7MhCjJkLq6z.a6T0yU8U8R9H7N6B5V4C3X2Z1W0V9U.', 'Sage', 'Admin', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- Assign SUPER_ADMIN role to seed admin
INSERT INTO user_roles (user_id, role_id)
SELECT 'a0000000-0000-0000-0000-000000000001', id FROM roles WHERE name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;
