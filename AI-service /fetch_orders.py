import pandas as pd
from sqlalchemy import create_engine
import warnings
import mysql.connector

# Suppress specific warnings related to MySQL connector
warnings.filterwarnings("ignore", message="Exception ignored in: <function BaseMySQLSocket.__del__")

# 1. Kết nối DB Order Service để lấy dữ liệu đơn hàng với SQLAlchemy
engine_order = create_engine('mysql+mysqlconnector://root:viet12345@localhost:3307/pbl7_order_service')

# Truy vấn lấy thông tin đơn hàng, kết hợp 3 bảng
query_order = """
SELECT 
    o.user_id,
    oi.product_id,
    oi.quantity,
    o.created_at
FROM orders o
JOIN orders_items map ON o.id = map.order_id
JOIN order_items oi ON map.items_id = oi.id
WHERE o.order_status = 'DELIVERED'
"""

order_df = pd.read_sql(query_order, engine_order)
print("✅ Truy vấn đơn hàng thành công")
print(order_df.head())

# 2. Kết nối DB Product Service để lấy thông tin sản phẩm với SQLAlchemy
engine_product = create_engine('mysql+mysqlconnector://root:viet12345@localhost:3307/pbl7_product_service')

# Tạo danh sách product_id từ đơn hàng đã lấy
product_ids = tuple(order_df['product_id'].unique())

# Nếu chỉ có một sản phẩm, sử dụng điều kiện '=' thay vì 'IN'
if len(product_ids) == 1:
    query_products = f"""
    SELECT 
        p.id AS product_id,
        p.title AS product_name,
        p.description,
        p.category_id AS category,
        p.selling_price AS price
    FROM product p
    WHERE p.id = {product_ids[0]}
    """
else:
    query_products = f"""
    SELECT 
        p.id AS product_id,
        p.title AS product_name,
        p.description,
        p.category_id AS category,
        p.selling_price AS price
    FROM product p
    WHERE p.id IN {product_ids}
    """


product_df = pd.read_sql(query_products, engine_product)
print("✅ Truy vấn sản phẩm thành công")
print(product_df.head())

# 3. Kết hợp dữ liệu đơn hàng và thông tin sản phẩm
merged_df = pd.merge(order_df, product_df, on="product_id", how="left")
merged_df = merged_df[[
    'user_id', 'product_id', 'product_name', 'description', 'category',
    'price', 'quantity', 'created_at'
]]


# 4. Lưu kết quả ra CSV để xử lý tiếp với AI
merged_df.to_csv("output_files/orders_data.csv", index=False)
print(merged_df.head())

engine_order = create_engine('mysql+mysqlconnector://root:viet12345@localhost:3307/pbl7_product_service')


# Execute the query to fetch the product details again (using the correct query)
product_df = pd.read_sql(query_products, engine_product)

# Print the fetched product data
print("✅ Truy vấn sản phẩm thành công")
print(product_df.head())

