import os
import chromadb
from dotenv import load_dotenv
from chromadb.utils import embedding_functions
import random
from typing import List, Dict, Any
load_dotenv()

# Default number of top matches to retrieve from vector search
DEFAULT_SEARCH_LIMIT = int(os.getenv('DEFAULT_SEARCH_LIMIT'))
sentence_transformer_ef = embedding_functions.SentenceTransformerEmbeddingFunction(model_name="keepitreal/vietnamese-sbert")


class RAG():
    def __init__(self, collection_name: str, db_path: str):
        # Initialize ChromaDB client and collection
        self.client = chromadb.PersistentClient(path=db_path)
        self.collection = self.client.get_or_create_collection(name=collection_name, embedding_function=sentence_transformer_ef)


    def weighted_reciprocal_rank(self, doc_lists):
        """
        Perform weighted Reciprocal Rank Fusion on multiple rank lists.
        You can find more details about RRF here:
        https://plg.uwaterloo.ca/~gvcormac/cormacksigir09-rrf.pdf

        Args:
            doc_lists: A list of rank lists, where each rank list contains unique items.

        Returns:
            list: The final aggregated list of items sorted by their weighted RRF
                  scores in descending order.
        """
        c = 60  # Parameter from the paper
        weights = [1] * len(doc_lists)  # Weights for each list

        if len(doc_lists) != len(weights):
            raise ValueError("Number of rank lists must equal the number of weights.")

        # Collect all unique documents
        all_documents = {doc["description"] for doc_list in doc_lists for doc in doc_list}
        rrf_score_dic = {doc: 0.0 for doc in all_documents}

        # Calculate RRF scores
        for doc_list, weight in zip(doc_lists, weights):
            for rank, doc in enumerate(doc_list, start=1):
                rrf_score = weight * (1 / (rank + c))
                rrf_score_dic[doc["description"]] += rrf_score

        # Sort documents by RRF scores in descending order
        sorted_documents = sorted(
            rrf_score_dic.keys(), key=lambda x: rrf_score_dic[x], reverse=True
        )

        # Map sorted content back to original documents
        page_content_to_doc_map = {
            doc["description"]: doc for doc_list in doc_lists for doc in doc_list
        }
        return [page_content_to_doc_map[content] for content in sorted_documents]

    def hybrid_search(self, query: str, query_embedding: list, limit=DEFAULT_SEARCH_LIMIT):
        if query_embedding is None:
            return "Invalid query or embedding generation failed."

        # Perform vector search in ChromaDB
        vector_results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=limit
        )

        print("[DEBUG] ChromaDB collection count:", self.collection.count())

        vector_results = [
            {
                "_id": vector_results['ids'][0][i],
                "title": vector_results['metadatas'][0][i].get('title'),
                "description": vector_results['metadatas'][0][i].get('description'),
                "selling_price": vector_results['metadatas'][0][i].get('selling_price'),
                "images": vector_results['metadatas'][0][i].get('images'),
                "category_id": vector_results['metadatas'][0][i].get('category_id'),
                "distance": vector_results['distances'][0][i]
            }
            for i in range(len(vector_results['ids'][0]))
        ]

        # Perform keyword search in ChromaDB
        keyword_results = self.collection.query(
            query_texts=[query],
            n_results=limit
        )

        keyword_results = [
            {
                "_id": keyword_results['ids'][0][i],
                "title": keyword_results['metadatas'][0][i].get('title'),
                "description": keyword_results['metadatas'][0][i].get('description'),
                "selling_price": keyword_results['metadatas'][0][i].get('selling_price'),
                "images": keyword_results['metadatas'][0][i].get('images'),
                "category_id": keyword_results['metadatas'][0][i].get('category_id'),
                "distance": keyword_results['distances'][0][i]
            }
            for i in range(len(keyword_results['ids'][0]))
        ]

        # Merge results and apply rank fusion
        doc_lists = [vector_results, keyword_results]
        fused_documents = self.weighted_reciprocal_rank(doc_lists)
        return fused_documents

    def enhance_prompt(self, query: str, query_embedding: list):
        get_knowledge = self.hybrid_search(query, query_embedding)
        print('hybrid_search_result:', get_knowledge)
        enhanced_prompt = ""
        for result in get_knowledge:
            enhanced_prompt += f"Title: {result.get('title', 'N/A')}, Content: {result.get('description', 'N/A')},Selling Price: {result.get('selling_price', 'N/A')}, Images: {result.get('images', 'N/A')}\n"
        return enhanced_prompt

    def get_random_products(self, exclude_ids=None, limit=5):
        all_ids = self.collection.get()['ids']
        print("DEBUG - Tổng số sản phẩm trong ChromaDB:", len(all_ids))
        if exclude_ids:
            all_ids = [i for i in all_ids if i not in exclude_ids]
        print("DEBUG - all_ids after exclude:", all_ids)
        if not all_ids:
            return []
        random.shuffle(all_ids)
        random_ids = all_ids[:limit]
        print("DEBUG - random_ids:", random_ids)
        results = self.collection.get(ids=random_ids)
        products = []
        for i, meta in enumerate(results['metadatas']):
            products.append({
                "id": results['ids'][i],
                "title": meta.get('title', ''),
                "description": meta.get('description', ''),
                "selling_price": meta.get('selling_price', ''),
                "images": meta.get('images', ''),
                "category_id": meta.get('category_id', ''),
                "relevance_score": None,
                "source": "random"
            })
        return products

    def get_recommendations(self, user_id: str, limit: int = 5) -> List[Dict[str, Any]]:
        print("DEBUG - get_recommendations CALLED")
        interests = self.get_user_interests(user_id)
        print("DEBUG - interests:", interests)
        combined_query = " ".join(interests)
        query_embedding = self.embedding_model.get_embedding(combined_query) if interests else None
        recommendations = []
        if interests:
            recs = self.hybrid_search(
                query=combined_query,
                query_embedding=query_embedding,
                limit=limit
            )
            print("DEBUG - recs from hybrid_search:", len(recs), recs)
            for rec in recs:
                recommendations.append({
                    "title": rec.get('title', ''),
                    "description": rec.get('description', ''),
                    "selling_price": rec.get('selling_price', ''),
                    "images": rec.get('images', ''),
                    "category_id": rec.get('category_id', ''),
                    "relevance_score": 1 - rec.get('distance', 0),
                    "source": "recommendation"
                })
        # Nếu chưa đủ, bổ sung random
        print("DEBUG - recommendations:", len(recommendations))
        if len(recommendations) < limit:
            print("DEBUG - CALLING get_random_products")
            exclude_ids = [rec.get('_id') for rec in recommendations]
            random_products = self.get_random_products(exclude_ids=exclude_ids, limit=limit-len(recommendations))
            recommendations.extend(random_products)
        return recommendations
