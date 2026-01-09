import json
import uuid
import chromadb
from chromadb.config import Settings
from dotenv import load_dotenv
from chromadb.utils import embedding_functions
from pymongo import MongoClient

# Load environment variables
load_dotenv()

OPEN_AI_ROLE_MAPPING = {
    "human": "user",
    "ai": "assistant"
}

sentence_transformer_ef = embedding_functions.SentenceTransformerEmbeddingFunction(model_name="keepitreal/vietnamese-sbert")


class Reflection():
    def __init__(self,
                 llm,
                 db_path: str,
                 dbChatHistoryCollection: str,
                 semanticCacheCollection: str,
                 mongo_uri: str,  # MongoDB URI
                 mongo_db: str,  # MongoDB database name
                 ):
        # Initialize Chroma client for embeddings
        self.client = chromadb.PersistentClient(path=db_path)
        self.his_collection = self.client.get_or_create_collection(name=dbChatHistoryCollection,
                                                                   embedding_function=sentence_transformer_ef)
        self.semantic_cache_collection = self.client.get_or_create_collection(name=semanticCacheCollection,
                                                                              embedding_function=sentence_transformer_ef)

        # MongoDB Client setup
        self.mongo_client = MongoClient(mongo_uri)
        self.db = self.mongo_client[mongo_db]
        self.chat_history_collection = self.db[dbChatHistoryCollection]
        self.llm = llm
        self.dbChatHistoryCollection = dbChatHistoryCollection

    def chat(self, session_id: str, enhanced_message: str, original_message: str = '', cache_response: bool = False,
             query_embedding: list = [], user_id: str = ''):
        system_prompt_content = """Bạn là một chatbot của cửa hàng bán laptop, gaming laptop, điện thoại, tai nghe. Vai trò của bạn là hỗ trợ khách hàng trong việc tìm hiểu về các sản phẩm và dịch vụ của cửa hàng, cũng như tạo một trải nghiệm mua sắm dễ chịu và thân thiện. Bạn có thể trả lời các câu hỏi về loại sản phẩm, dịch vụ giao hàng. Bạn cũng có thể trò chuyện với khách hàng về các chủ đề không liên quan đến sản phẩm như thời tiết, sở thích cá nhân, và những câu chuyện thú vị để tạo sự gắn kết. 
        Hãy luôn giữ thái độ lịch sự và chuyên nghiệp. Nếu khách hàng hỏi về sản phẩm cụ thể, hãy cung cấp thông tin chi tiết và gợi ý các lựa chọn phù hợp. Nếu khách hàng trò chuyện về các chủ đề không liên quan đến sản phẩm, hãy tham gia vào cuộc trò chuyện một cách vui vẻ và thân thiện.
        một số điểm chính bạn cần lưu ý:
        1. Đáp ứng nhanh chóng và chính xác, sử dụng xưng hô là "Mình và bạn".
        2. Giữ cho cuộc trò chuyện vui vẻ và thân thiện.
        3. Cung cấp thông tin hữu ích về tiệm bánh và dịch vụ của cửa hàng.
        4. Giữ cho cuộc trò chuyện mang tính chất hỗ trợ và giúp đỡ.
        5. Nếu khách hàng hỏi về sản phẩm, hãy cung cấp thông tin chi tiết và gợi ý các lựa chọn phù hợp một cách ngắn gọn và hợp lý.
        Hãy làm cho khách hàng cảm thấy được chào đón và quan tâm!"""

        system_prompt = [{"role": "system", "content": system_prompt_content}]
        human_prompt = [{"role": "user", "content": enhanced_message}]
        session_messages = list(self.chat_history_collection.find({"SessionId": session_id}))
        formatted_session_messages = self.__construct_session_messages__(session_messages)

        # Format for Gemini
        formatted_messages = {
            'parts': [
                {'type': 'text', 'text': system_prompt_content},
                *formatted_session_messages,
                {'type': 'text', 'text': enhanced_message}
            ]
        }

        # Get response from Gemini
        response = self.llm.chat(formatted_messages)
        # print(f"Response structure: {response}")

        if isinstance(response, dict):
            try:
                response_content = response["choices"][0]["message"]["content"]
            except Exception as e:
                response_content = f"Gemini response error: {str(e)}"
        else:
            response_content = str(response)  # fallback if Gemini gave a string

        # Store history
        self.__record_human_prompt__(session_id, enhanced_message, original_message, user_id)
        self.__record_ai_response__(session_id, response_content)

        if cache_response:
            self.__cache_ai_response__(enhanced_message, original_message, response_content, query_embedding)

        return {
            "choices": [
                {
                    "message": {
                        "role": "assistant",
                        "content": response_content
                    }
                }
            ]
        }

    def __construct_session_messages__(self, session_messages: list):
        result = []
        if not session_messages:
            return result

        for session_message in session_messages:
            history_data = session_message['History']
            role = OPEN_AI_ROLE_MAPPING[history_data['type']]
            result.append({
                "type": role,
                "text": history_data['data']['content']
            })
        return result

    def __record_human_prompt__(self, session_id: str, enhanced_message: str, original_message: str, user_id: str):
        self.chat_history_collection.insert_one({
            "SessionId": session_id,
            "UserId": user_id,
            "History": {
                "type": "human",
                "data": {
                    "type": "human",
                    "content": original_message,
                    "enhanced_content": enhanced_message,
                    "additional_kwargs": {},
                    "response_metadata": {},
                    "name": None,
                    "id": None,
                }
            }
        })

    def __record_ai_response__(self, session_id: str, ai_content: str):
        self.chat_history_collection.insert_one({
            "SessionId": session_id,
            "History": {
                "type": "ai",
                "data": {
                    "type": "ai",
                    "content": ai_content,
                    "enhanced_content": None,
                    "additional_kwargs": {},
                    "name": None,
                    "id": str(uuid.uuid4()),  
                    "usage_metadata": {},
                    "response_metadata": {
                        "model_name": "Gemini",
                        "finish_reason": "stop",
                        "logprobs": None
                    },
                }
            }
        })

    def __cache_ai_response__(self, enhanced_message: str, original_message: str, ai_content: str,
                              query_embedding: list):
        embedding = query_embedding
        self.semantic_cache_collection.add(
            ids=[str(uuid.uuid4())],
            embeddings=[embedding],
            documents=[json.dumps({
                "text": [
                    {
                        "type": "human",
                        "content": original_message,
                        "enhanced_content": enhanced_message,
                        "additional_kwargs": {},
                        "response_metadata": {},
                        "name": None,
                        "id": None,
                    }
                ],
                "llm_string": {
                    "model_name": "Gemini",
                    "name": "ChatGemini"
                },
                "return_val": [
                    {
                        "type": "ai",
                        "content": ai_content,
                        "enhanced_content": None,
                        "additional_kwargs": {},
                        "name": None,
                        "id": str(uuid.uuid4()),
                        "usage_metadata": {},
                        "response_metadata": {
                            "model_name": "Gemini",
                            "finish_reason": "stop",
                            "logprobs": None
                        },
                    }
                ]
            })]
        )
