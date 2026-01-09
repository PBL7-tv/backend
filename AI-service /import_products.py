import os
from dotenv import load_dotenv
import chromadb
from chromadb.utils import embedding_functions
import json
import pymysql

load_dotenv()

def fetch_products_from_mysql():
    connection = pymysql.connect(
        host='localhost',
        user='root',
        port=3307,
        password='viet12345',
        database='pbl7_product_service',
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )
    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT p.id, p.title, p.description, p.selling_price, p.brand, p.color, p.category_id, GROUP_CONCAT(pi.images) as images
                FROM product p
                LEFT JOIN product_images pi ON p.id = pi.product_id
                GROUP BY p.id
            """
            )
            rows = cursor.fetchall()

        products = []
        for row in rows:
            # Xử lý images thành list
            if row['images']:
                row['images'] = row['images'].split(',')
            else:
                row['images'] = []
            products.append(row)
    finally:
        connection.close()
    return products

# Initialize ChromaDB client
client = chromadb.PersistentClient(path=os.getenv('VECTOR_STORE'))

# XÓA COLLECTION CŨ
client.delete_collection(name=os.getenv('COLLECTION_NAME'))

sentence_transformer_ef = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name="keepitreal/vietnamese-sbert"
)

# Tạo lại collection
collection = client.get_or_create_collection(
    name=os.getenv('COLLECTION_NAME'),
    embedding_function=sentence_transformer_ef
)

products = fetch_products_from_mysql()

# Add products to collection
for product in products:
    # Lấy toàn bộ list ảnh, nếu không có thì để list rỗng
    images = product.get("images", [])
    if not isinstance(images, list):
        images = [images] if images else []
    # Lưu list ảnh dưới dạng JSON string (nếu ChromaDB không hỗ trợ list)
    images_json = json.dumps(images)
    collection.add(
        ids=[str(product["id"])],
        documents=[json.dumps(product)],
        metadatas=[{
            "title": str(product.get("title", "")),
            "description": str(product.get("description", "")),
            "selling_price": str(product.get("selling_price", "")),
            "images": images_json,  # <-- lưu list ảnh dạng json string
            "category_id": str(product.get("category_id", ""))
        }]
    )

print(f"Đã import {len(products)} sản phẩm vào ChromaDB")
print(f"Số lượng sản phẩm trong collection: {collection.count()}")

# results = collection.get(ids=["261"])
# results = collection.get(ids=[str(products[0]['id'])])
# print(results) 