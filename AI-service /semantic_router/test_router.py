import unittest
from semantic_router import SemanticRouter, Route
from semantic_router.samples import productSample, chitchatSample

class RouterTestCase(unittest.TestCase):
    def setUp(self):
        self.PRODUCT_ROUTE_NAME = 'products'
        self.CHITCHAT_ROUTE_NAME = 'chitchat'

        productRoute = Route(name=self.PRODUCT_ROUTE_NAME, samples=productSample)
        chitchatRoute = Route(name=self.CHITCHAT_ROUTE_NAME, samples=chitchatSample)
        self.semanticRouter = SemanticRouter(routes=[productRoute, chitchatRoute])

    def test_chitchat_route(self):
        chitchat_queries = [
            "chào bạn",
            "bạn có khỏe không?",
        ]
        for query in chitchat_queries:
            print(query)
            self.assertEqual(self.semanticRouter.guide(query)[1], self.CHITCHAT_ROUTE_NAME,
                            'incorrect semantic route for chitchat query')

    def test_products_route(self):
        products_queries = [
            "Bạn có loại tai nghe nào mới về không?",
            "Mình muốn đặt chiếc laptop gaming, bạn có gợi ý nào không?",
            "Cấu hình của máy nào thì phù hợp làm đồ hoạ?",
            "Giá của chiếc điện thoại này là bao nhiêu?",
        ]
        for query in products_queries:
            print(query)
            self.assertEqual(self.semanticRouter.guide(query)[1], self.PRODUCT_ROUTE_NAME,
                            'incorrect semantic route for products query')
