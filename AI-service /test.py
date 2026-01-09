from chromadb.utils import embedding_functions
import chromadb

client = chromadb.PersistentClient(path='databaseQQ/VECTOR_STORE')
sentence_transformer_ef = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name="keepitreal/vietnamese-sbert"
)

collection = client.get_or_create_collection(
    name="products",  # Thay bằng tên trùng với biến `collection_name` trong .env
    embedding_function=sentence_transformer_ef
)

# Dữ liệu test
collection.add(
    documents=[
        "Laptop ASUS gaming hiệu suất cao",
        "Điện thoại iPhone 15 Pro Max chính hãng",
        "Tai nghe bluetooth Sony chống ồn"
    ],
    metadatas=[
        {
            "title": "ASUS ROG Zephyrus",
            "description": "Laptop chơi game mạnh mẽ, card RTX 4070, màn hình 240Hz",
            "price": "45.000.000 VND",
            "image_url": "https://example.com/asus.jpg",
            "category": "laptop"
        },
        {
            "title": "iPhone 15 Pro Max",
            "description": "Điện thoại cao cấp với camera chất lượng và chip A17",
            "price": "39.990.000 VND",
            "image_url": "https://example.com/iphone.jpg",
            "category": "phone"
        },
        {
            "title": "Tai nghe Sony WH-1000XM5",
            "description": "Tai nghe chống ồn hàng đầu, thời lượng pin lâu",
            "price": "7.990.000 VND",
            "image_url": "https://example.com/sony.jpg",
            "category": "headphone"
        },
    ],
    ids=["item1", "item2", "item3"]
)
