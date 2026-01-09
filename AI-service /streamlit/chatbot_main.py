import re
import streamlit as st
import time
import requests
import uuid
import webbrowser

# Display the logo in the sidebar
logo = 'https://product.hstatic.net/200000140863/product/set_mini_crusty__1a324c901b0347c6afa7bc818d387e46_1024x1024.png'
link_page = 'https://chewychewy.vn/'
st.sidebar.caption(':red[VGEAR]')
st.sidebar.image(logo, width=500)

# Display the clickable link
if st.sidebar.button("Visit Chewy Chewy Website 😊"):
    webbrowser.open_new_tab(link_page)

st.title("VGear ^v^ ")
st.write("Chào mừng bạn đến với cửa hàng VGear")

# Define a list of greeting phrases
greeting_phrases = ["xin chào", "hi", "hello", "xin chao", 'chào', 'chao', 'helloo', 'helo']

greeting_response = ("Chào bạn! Chào mừng bạn đến với VGear của chúng mình. "
                     "Bạn có câu hỏi hoặc cần tư vấn gì không ạ? Mình sẽ cố gắng giúp đỡ bạn "
                     "tìm bánh hoặc trà phù hợp nhất với yêu cầu mà cửa hàng chúng mình có. Chúc bạn một ngày tốt lành! 😊🌸")

# Clean and normalize the input prompt
def clean_input(input_text):
    return re.sub(r'\W+', '', input_text.lower().strip())

def clear_session_state():
    for key in st.session_state.keys():
        del st.session_state[key]

# Generate a random session ID
if "session_id" not in st.session_state:
    st.session_state.session_id = str(uuid.uuid4())

session_id = st.session_state.session_id

# URL of the Flask API
st.session_state.flask_api_url = "http://localhost:5001/api/v1/chewy_chewy"
st.session_state.recommendation_api_url = "http://localhost:5001/api/v1/recommendations"

# Initialize chat history in session state
if "chat_history" not in st.session_state:
    st.session_state.chat_history = []

# Initialize recommendations in session state
if "recommendations" not in st.session_state:
    st.session_state.recommendations = []

def display_recommendations(recommendations):
    if recommendations:
        st.subheader("Sản phẩm gợi ý cho bạn:")
        cols = st.columns(3)
        for idx, rec in enumerate(recommendations):
            with cols[idx % 3]:
                st.image(rec.get('image', ''), width=200)
                st.write(f"**{rec.get('title', '')}**")
                st.write(f"Giá: {rec.get('price', '')}")
                st.write(f"{rec.get('description', '')[:100]}...")

# Display the chat history using chat UI
for message in st.session_state.chat_history:
    with st.chat_message(message["role"]):
        st.markdown(message["content"])

# Display current recommendations
display_recommendations(st.session_state.recommendations)

if prompt := st.chat_input("VGear có thể giúp bạn đặt loại laptop nào nhỉ?"):
    # Add user message to chat history
    st.session_state.chat_history.append({"role": "user", "content": prompt})
    with st.chat_message('user'):
        time.sleep(0.5)
        st.markdown(prompt)

    if clean_input(prompt) in [clean_input(greet) for greet in greeting_phrases]:
        # If it's a greeting, respond with the greeting response without calling any API
        with st.chat_message("assistant"):
            full_res = ''
            message_placeholder = st.empty()
            for res in greeting_response.split():
                full_res += res + " "
                message_placeholder.markdown(full_res + "▌")
                time.sleep(0.04)
            message_placeholder.markdown(full_res)

        # Add the assistant's greeting response to the chat history
        st.session_state.chat_history.append({"role": "assistant", "content": greeting_response})
    else:
        # Prepare the payload for the request
        with st.chat_message("assistant"):
            payload = {
                "query": prompt,
                "session_id": session_id
            }

            # Send the POST request to the Flask API
            response = requests.post(st.session_state.flask_api_url, json=payload)
            if response.status_code == 200:
                # Get the response from the API
                api_response = response.json()

                # Check if 'choices' and 'message' are present
                if 'choices' in api_response and len(api_response['choices']) > 0:
                    assistant_message = api_response['choices'][0].get('message', {})
                    if 'content' in assistant_message:
                        content = assistant_message['content']
                        if 'parts' in content:
                            # Loop over parts in the response
                            full_res = ""
                            message_placeholder = st.empty()

                            for part in content['parts']:
                                if part.get('type') == 'text':
                                    full_res += part.get('text', '') + " "
                                    message_placeholder.markdown(full_res + "▌")
                                    time.sleep(0.04)  # Adjust typing speed if needed

                            # Final display without the cursor
                            message_placeholder.markdown(full_res)

                            # Add the assistant's response to the chat history
                            st.session_state.chat_history.append({"role": "assistant", "content": full_res})

                            # Get recommendations based on the conversation
                            recommendation_payload = {
                                "user_id": session_id,
                                "query": prompt,
                                "limit": 3
                            }
                            recommendation_response = requests.post(
                                st.session_state.recommendation_api_url,
                                json=recommendation_payload
                            )
                            
                            if recommendation_response.status_code == 200:
                                recommendation_data = recommendation_response.json()
                                if recommendation_data["status"] == "success":
                                    st.session_state.recommendations = recommendation_data["recommendations"]
                                    # Clear previous recommendations and display new ones
                                    st.experimental_rerun()
                        else:
                            st.error("Error: 'parts' not found in the response content.")
                    else:
                        st.error("Error: No valid content found in the API response.")
                else:
                    st.error("Error: Invalid API response structure.")
            else:
                st.error(f"Error: {response.status_code}")
