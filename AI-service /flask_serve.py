import pymongo
from flask import Flask, request, jsonify
from flask_cors import CORS
from rag.core_test import RAG
from semantic_router import SemanticRouter, Route
from semantic_router.samples import productSample, chitchatSample
from reflection.core import Reflection
from embedding_model.core import EmbeddingModel
from recommendation.core import Recommendation
import os
from dotenv import load_dotenv
from chromadb.utils import embedding_functions
import chromadb
import uuid
import google.generativeai as genai
from typing import List, Dict, Any


class GeminiClient:
    def __init__(self):
        self.model = genai.GenerativeModel("gemini-1.5-flash")

    def chat(self, prompt: str):
        try:
            print(f"[Gemini] Prompt sent to Gemini:\n{prompt}\n")
            if isinstance(prompt, dict) and "parts" in prompt:
                # Chuyển thành string nếu lỡ truyền nhầm format
                prompt_text = "\n".join([p["text"] for p in prompt["parts"] if p["type"] == "text"])
            else:
                prompt_text = prompt

            response = self.model.generate_content(prompt_text)
            print(f"[Gemini] Response: {response.text}")

            return {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": response.text
                        }
                    }
                ]
            }
        except Exception as e:
            return {
                "choices": [
                    {
                        "message": {
                            "role": "assistant",
                            "content": f"Gemini Error in GeminiClient: {str(e)}"
                        }
                    }
                ]
            }

load_dotenv()
genai.configure(api_key=os.getenv("GENMINI_API_KEY"))

# Load environment variables
db_chat_history_collection = os.getenv('DB_CHAT_HISTORY_COLLECTION')
collection_name = os.getenv('COLLECTION_NAME')
semanticCacheCollection = os.getenv('semanticCacheCollection')
db_path = 'databaseQQ/VECTOR_STORE'
mongo_uri = os.getenv("MONGO_URI")
mongo_db = os.getenv("MONGO_DB")

# Initialize Flask
app = Flask(__name__)
CORS(app, origins=["http://localhost:3000"])

# Initialize embedding model and Gemini client
embedding_model = EmbeddingModel()
llm = GeminiClient()

# client = chromadb.PersistentClient(path=db_path)
client = chromadb.PersistentClient(path=os.getenv("VECTOR_STORE"))
sentence_transformer_ef = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name="keepitreal/vietnamese-sbert")
chroma_collection = client.get_or_create_collection(name=collection_name, embedding_function=sentence_transformer_ef)

rag = RAG(
    collection_name=collection_name,
    db_path=db_path
)

# Setup Semantic Router
PRODUCT_ROUTE_NAME = 'products'
CHITCHAT_ROUTE_NAME = 'chitchat'
productRoute = Route(name=PRODUCT_ROUTE_NAME, samples=productSample)
chitchatRoute = Route(name=CHITCHAT_ROUTE_NAME, samples=chitchatSample)
semanticRouter = SemanticRouter(routes=[productRoute, chitchatRoute])

# Setup Reflection
reflection = Reflection(
    llm=llm,
    db_path=db_path,
    dbChatHistoryCollection=db_chat_history_collection,
    semanticCacheCollection=semanticCacheCollection,
    mongo_uri=mongo_uri,
    mongo_db=mongo_db
)

# Initialize Recommendation system
recommendation_system = Recommendation(
    # mongo_uri=mongo_uri,
    # mongo_db=mongo_db
)

@app.route('/api/v1/chat', methods=['POST'])
def chewy_chewy():
    data = request.get_json()
    print(f"[DEBUG] Request received: {data}")

    query = data.get('query', '')
    session_id = data.get('session_id', str(uuid.uuid4()))
    user_id = data.get('user_id', '')

    guided_route = semanticRouter.guide(query)[1]
    print(f"[DEBUG] Semantic route: {guided_route}")

    if guided_route == PRODUCT_ROUTE_NAME:
        query_embedding = embedding_model.get_embedding(query)
        source_information = rag.enhance_prompt(query, query_embedding).replace('<br>', '\n')
        print(f"[DEBUG] Retrieved source info from RAG: \n{source_information}")

        combined_information = f"Câu hỏi: {query}\nTrả lời khách hàng sử dụng thông tin sản phẩm sau:\n###Sản Phẩm###\n{source_information}"

        llm_response = reflection.chat(
            session_id=session_id,
            enhanced_message=combined_information,
            original_message=query,
            cache_response=True,
            query_embedding=query_embedding,
            user_id=user_id
        )
    else:
        llm_response = reflection.chat(
            session_id=session_id,
            enhanced_message=query,
            original_message=query,
            cache_response=False,
            user_id=user_id
        )

    try:
        assistant_text = llm_response["choices"][0]["message"]["content"]

        response_data = jsonify({
            "choices": [
                {
                    "message": {
                        "role": "assistant",
                        "content": {
                            "parts": [
                                {"type": "text", "text": assistant_text}
                            ]
                        }
                    }
                }
            ]
        })
    except Exception as e:
        print(f"[ERROR] Formatting response failed: {e}")
        response_data = jsonify({"error": f"Failed to process LLM response: {str(e)}"}), 500

    return response_data

@app.route('/api/v1/recommendations', methods=['POST'])
def get_recommendations():
    print("DEBUG - Flask route /api/v1/recommendations CALLED")
    data = request.get_json()
    print(f"[DEBUG] Recommendation request received: {data}")

    user_id = data.get('user_id', '')
    current_query = data.get('query', '')
    limit = data.get('limit', 5)

    try:
        if current_query:
            # Get personalized recommendations based on current query
            recommendations = recommendation_system.get_personalized_recommendations(
                user_id=user_id,
                current_query=current_query,
                limit=limit
            )
        else:
            # Get general recommendations based on user history
            recommendations = recommendation_system.get_recommendations(
                user_id=user_id,
                limit=limit
            )

        return jsonify({
            "status": "success",
            "recommendations": recommendations
        })

    except Exception as e:
        print(f"[ERROR] Recommendation generation failed: {e}")
        return jsonify({
            "status": "error",
            "message": f"Failed to generate recommendations: {str(e)}"
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
