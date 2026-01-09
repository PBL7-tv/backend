import pymysql
import json

conn = pymysql.connect(
    host='localhost',
    user='root',
    password='viet12345',
    db='pbl7_product_service',
    port=3307
)
cursor = conn.cursor()

cursor.execute("SELECT id, title, description, selling_price FROM product")
rows = cursor.fetchall()

products = []
for row in rows:
    product_id = row[0]
    name = row[1]
    description = row[2]
    selling_price = row[3]

    # Fetch associated images for this product
    cursor.execute("SELECT images FROM product_images WHERE product_id = %s", (product_id,))
    image_rows = cursor.fetchall()
    images = [img[0] for img in image_rows]

    products.append({
        "id": product_id,
        "name": name,
        "description": description,
        "selling_price": selling_price,
        "images": images
    })

# Write to JSON
with open("output_files/products.json", "w", encoding="utf-8") as f:
    json.dump(products, f, ensure_ascii=False, indent=2)

print("✅ Exported product data with images to products.json")

