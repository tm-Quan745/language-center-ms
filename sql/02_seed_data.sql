USE quan_ly_san_pham;

INSERT INTO category(name) VALUES
('Beverage'),
('Snack'),
('Electronics');

INSERT INTO product(name, price, quantity, category_id) VALUES
('Coca Cola', 10.00, 50, 1),
('Pepsi', 9.50, 40, 1),
('Potato Chips', 15.00, 20, 2),
('Headphone', 199.00, 10, 3);

UPDATE category c
SET product_count = (SELECT COUNT(*) FROM product p WHERE p.category_id = c.id);
