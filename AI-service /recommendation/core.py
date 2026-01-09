import os
from typing import List, Dict, Any
from dotenv import load_dotenv
import pymysql
from embedding_model.core import EmbeddingModel
from rag.core_test import RAG
import time
import logging
import json

load_dotenv()

MYSQL_ORDER_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'port': 3307,
    'password': 'viet12345',
    'database': 'pbl7_order_service',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

MYSQL_PRODUCT_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'port': 3307,
    'password': 'viet12345',
    'database': 'pbl7_product_service',
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

def fetch_orders_from_mysql(user_id: str):
    connection = pymysql.connect(**MYSQL_ORDER_CONFIG)
    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                SELECT oi.product_id, oi.quantity, o.created_at
                FROM orders o
                JOIN orders_items map ON o.id = map.order_id
                JOIN order_items oi ON map.items_id = oi.id
                WHERE o.order_status = 'DELIVERED' AND o.user_id = %s
            """, (user_id,))
            orders = cursor.fetchall()
    finally:
        connection.close()
    return orders

def fetch_products_by_ids(product_ids: List[Any]):
    if not product_ids:
        return []
    connection = pymysql.connect(**MYSQL_PRODUCT_CONFIG)
    try:
        with connection.cursor() as cursor:
            format_strings = ','.join(['%s'] * len(product_ids))
            cursor.execute(f"""
                SELECT id, title, description, category_id, selling_price
                FROM product
                WHERE id IN ({format_strings})
            """, tuple(product_ids))
            products = cursor.fetchall()
    finally:
        connection.close()
    return products

class Recommendation:
    def __init__(self):
        # Initialize embedding model and RAG
        self.embedding_model = EmbeddingModel()
        self.rag = RAG(
            collection_name=os.getenv('COLLECTION_NAME'),
            db_path=os.getenv('VECTOR_STORE')
        )
        from pymongo import MongoClient

# class Recommendation:
#     def __init__(self):
#         # ... giữ nguyên phần MySQL
#         self.embedding_model = EmbeddingModel()
#         self.rag = RAG(
#             collection_name=os.getenv('COLLECTION_NAME'),
#             db_path=os.getenv('VECTOR_STORE')
#         )
#         # Kết nối MongoDB cho chat history
#         self.mongo_client = MongoClient(os.getenv("MONGO_URI"))
#         self.db = self.mongo_client[os.getenv("MONGO_DB")]
#         self.chat_history_collection = self.db[os.getenv('DB_CHAT_HISTORY_COLLECTION')]

#     def get_user_interests(self, user_id: str) -> List[str]:
#         # 1. Lấy interests từ đơn hàng MySQL
#         orders = fetch_orders_from_mysql(user_id)
#         product_ids = [order['product_id'] for order in orders]
#         products = fetch_products_by_ids(product_ids)
#         interests = []
#         for product in products:
#             interests.append(f"interested in {product['category_id']}")
#             interests.append(product['title'])

#         # 2. Lấy interests từ chat history MongoDB
#         chat_history = list(self.chat_history_collection.find({"SessionId": user_id}))
#         for chat in chat_history:
#             if chat['History']['type'] == 'human':
#                 content = chat['History']['data']['content']
#                 # Lấy các câu chat có nhắc đến sản phẩm
#                 if any(keyword in content.lower() for keyword in ['laptop', 'điện thoại', 'tai nghe', 'gaming']):
#                     interests.append(content)

#         return interests

    def get_user_interests(self, user_id: str) -> List[str]:
        # Lấy đơn hàng từ MySQL
        orders = fetch_orders_from_mysql(user_id)
        product_ids = [order['product_id'] for order in orders]
        products = fetch_products_by_ids(product_ids)
        interests = []
        for product in products:
            interests.append(f"interested in {product['category_id']}")
            interests.append(product['title'])
        return interests

    def get_recommendations(self, user_id: str, limit: int = 5) -> List[Dict[str, Any]]:
        interests = self.get_user_interests(user_id)
        if not interests:
            # Nếu user mới, trả về random sản phẩm 
            print("DEBUG - No interests, returning random products")
            random_products = self.rag.get_random_products(limit=limit)
            for prod in random_products:
                print("DEBUG - random product object:", prod)
                print(f"RANDOM PRODUCT: id={prod.get('id', 'unknown')}, title={prod.get('title', '')}")
            return [{"id": prod.get('id', 'unknown'), "title": prod.get('title', ''), "source": "random"} for prod in random_products]
        combined_query = " ".join(interests)
        query_embedding = self.embedding_model.get_embedding(combined_query)
        recommendations = self.rag.hybrid_search(
            query=combined_query,
            query_embedding=query_embedding,
            limit=limit
        )
        formatted_recommendations = []
        for rec in recommendations:
            meta = rec.get('metadatas') or rec
            product_id = rec.get('_id') or meta.get('id', '')  # Lấy id từ rec hoặc từ metadata
            if rec.get('source') == "random":
                print(f"RANDOM PRODUCT: id={product_id}, title={meta.get('title', '')}")
                formatted_recommendations.append({
                    "id": product_id,
                    "title": meta.get('title', ''),
                    "selling_price": meta.get('selling_price', ''),
                    "images": meta.get('images', ''),
                    "source": "random"
                })
            else:
                print("RECOMMENDATION PRODUCT:", meta)
                formatted_recommendations.append({
                    "id": product_id,
                    "title": meta.get('title', ''),
                    "description": meta.get('description', ''),
                    "selling_price": meta.get('selling_price', ''),
                    "images": meta.get('images', ''),
                    "category_id": meta.get('category_id', ''),
                    "relevance_score": 1 - rec.get('distance', 0),
                    "source": "recommendation"
                })
        # Nếu chưa đủ, bổ sung random
        if len(formatted_recommendations) < limit:
            exclude_ids = [rec.get('_id') for rec in recommendations]
            random_products = self.rag.get_random_products(exclude_ids=exclude_ids, limit=limit-len(formatted_recommendations))
            for prod in random_products:
                print(f"RANDOM PRODUCT: id={prod.get('id', 'unknown')}, title={prod.get('title', '')}")
            formatted_recommendations.extend([{
                "id": prod.get('id', 'unknown'),
                "title": prod.get('title', ''),
                "selling_price": prod.get('selling_price', ''),
                "images": prod.get('images', ''),
                "source": "random"
            } for prod in random_products])
        return formatted_recommendations

    def get_personalized_recommendations(self, user_id: str, current_query: str, limit: int = 5) -> List[Dict[str, Any]]:
        interests = self.get_user_interests(user_id)
        print("DEBUG - (personalized)  CALLED")
        if not interests:
            print("DEBUG - No interests (personalized), returning random products")
            # Chỉ trả về id cho random
            random_products = self.rag.get_random_products(limit=limit)
            for prod in random_products:
                print("DEBUG - random product object:", prod)
                print(f"RANDOM PRODUCT ID: {prod.get('id', 'unknown')}")
            return [{"id": prod.get('id', 'unknown'), "source": "random"} for prod in random_products]
        combined_query = f"{current_query} {' '.join(interests)}"
        query_embedding = self.embedding_model.get_embedding(combined_query)
        recommendations = self.rag.hybrid_search(
            query=combined_query,
            query_embedding=query_embedding,
            limit=limit
        )
        formatted_recommendations = []
        for rec in recommendations:
            meta = rec.get('metadatas') or rec
            if rec.get('source') == "random":
                # Chỉ in id cho random
                print("DEBUG - random product object:", rec)
                print(f"RANDOM PRODUCT ID: {rec.get('_id', 'unknown')}")
                formatted_recommendations.append({
                    "id": rec.get('_id', 'unknown'),
                    "title": meta.get('title', ''),
                    "selling_price": meta.get('selling_price', ''),
                    "images": meta.get('images', ''),
                    "source": "random"
                })
            else:
                # In chi tiết cho recommendation
                print("RECOMMENDATION PRODUCT:", meta)
                formatted_recommendations.append({
                    "title": meta.get('title', ''),
                    "description": meta.get('description', ''),
                    "selling_price": meta.get('selling_price', ''),
                    "images": meta.get('images', ''),
                    "category_id": meta.get('category_id', ''),
                    "relevance_score": 1 - rec.get('distance', 0),
                    "source": "recommendation"
                })
        # Nếu chưa đủ, bổ sung random
        if len(formatted_recommendations) < limit:
            exclude_ids = [rec.get('_id') for rec in recommendations]
            random_products = self.rag.get_random_products(exclude_ids=exclude_ids, limit=limit-len(formatted_recommendations))
            for prod in random_products:
                print(f"RANDOM PRODUCT: id={prod.get('id', 'unknown')}, title={prod.get('title', '')}")
            formatted_recommendations.extend([{
                "id": prod.get('id', 'unknown'),
                "title": prod.get('title', ''),
                "selling_price": prod.get('selling_price', ''),
                "images": prod.get('images', ''),
                "source": "random"
            } for prod in random_products])
        return formatted_recommendations

    def get_recommendation_with_log(self, user_id: str, query: str, expected_answer: str, response_time: float) -> Dict[str, Any]:
        interests = self.get_user_interests(user_id)
        if not interests:
            print("DEBUG - No interests, returning random products")
            random_products = self.rag.get_random_products(limit=1)
            response = random_products[0].get('title', 'No recommendation found')
        else:
            combined_query = " ".join(interests)
            query_embedding = self.embedding_model.get_embedding(combined_query)
            recommendations = self.rag.hybrid_search(
                query=combined_query,
                query_embedding=query_embedding,
                limit=1
            )
            response = recommendations[0].get('metadatas', {}).get('title', 'No recommendation found')

        # Khi test thủ công, bạn có thể thêm expected_answer vào log
        logging.info({
            'user_id': user_id,
            'query': query,
            'query_type': 'recommendation',
            'response': response,
            'expected_answer': expected_answer,
            'is_correct': response == expected_answer,
            'response_time': response_time
        })

        return {
            "id": recommendations[0].get('_id', 'unknown'),
            "title": response,
            "selling_price": recommendations[0].get('selling_price', ''),
            "images": recommendations[0].get('images', ''),
            "source": "recommendation"
        } 