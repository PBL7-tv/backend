import requests
import time

url = "http://localhost:5001/api/v1/chat"  
user_id = "13235c69-0b74-423d-a80d-e3ceedca6b8b"
session_id = "test_session_1"

test_cases = [
    {
        "query": "Tôi muốn mua laptop dưới 20 triệu",
        "expected": "laptop",  # keyword mong đợi trong câu trả lời
        "type": "product"
    },
    {
        "query": "Bạn khỏe không?",
        "expected": "khỏe",  # keyword mong đợi trong câu trả lời
        "type": "chitchat"
    },
    # Thêm các test case khác...
]

correct = 0
total = 0
response_times = []

for case in test_cases:
    payload = {
        "query": case["query"],
        "user_id": user_id,
        "session_id": session_id
    }
    start = time.time()
    resp = requests.post(url, json=payload)
    end = time.time()
    response_time = end - start
    response_times.append(response_time)

    try:
        data = resp.json()
        # Lấy text trả lời từ response
        assistant_text = data["choices"][0]["message"]["content"]["parts"][0]["text"]
    except Exception as e:
        assistant_text = f"LỖI: {e}"

    is_correct = case["expected"].lower() in assistant_text.lower()
    print(f"Query: {case['query']}")
    print(f"Bot trả lời: {assistant_text}")
    print(f"Đúng mong đợi? {'ĐÚNG' if is_correct else 'SAI'}")
    print(f"Thời gian phản hồi: {response_time:.2f} giây")
    print("-" * 40)
    total += 1
    if is_correct:
        correct += 1

print(f"Tổng số truy vấn: {total}")
print(f"Số câu trả lời đúng: {correct}")
print(f"Độ chính xác: {correct/total*100:.2f}%")
print(f"Thời gian phản hồi trung bình: {sum(response_times)/len(response_times):.2f} giây")
