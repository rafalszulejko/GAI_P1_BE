import openai
import requests
from datetime import datetime
import random
import logging
import sys
import os

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler('init-db.log')
    ]
)
logger = logging.getLogger(__name__)

# Configure OpenAI
from dotenv import load_dotenv
load_dotenv()
openai.api_key = os.getenv('OPENAI_KEY')
logger.info("Starting database initialization script")

# User IDs - these are fixed and already exist in the system
USER_IDS = {
    'john': 'ba95fadb-0897-4f02-86ed-7463d1bf2177',
    'adam': 'a8327e7f-0539-4c9e-a957-2e6d2d2303a9',
    'bob': '5542af4a-2d62-400c-9760-43824c3128ac',
    'anne': '9cf84327-f563-47b5-b1e6-e994420a59fa',
    'alice': '3d80dda6-f59f-4628-9b43-ce9e63e44389'
}
logger.info(f"Loaded {len(USER_IDS)} user IDs")

# User roles for context generation
ROLES = {
    'john': 'Product Manager',
    'adam': 'UI/UX Designer',
    'bob': 'Software Developer',
    'anne': 'Software Developer',
    'alice': 'Software Developer'
}

API_BASE = 'http://localhost:8080'
logger.info(f"Using API base URL: {API_BASE}")

def generate_team_combinations(users, min_size=2, max_size=4, num_combinations=15):
    """Generate random team combinations of varying sizes."""
    logger.info(f"Generating {num_combinations} team combinations (size {min_size}-{max_size})")
    combinations = []
    while len(combinations) < num_combinations:
        size = random.randint(min_size, min(max_size, len(users)))
        team = random.sample(users, size)
        
        # Avoid duplicate combinations
        team_set = frozenset(team)
        if team_set not in (frozenset(c) for c in combinations):
            combinations.append(team)
            logger.debug(f"Generated team: {team}")
    
    logger.info(f"Generated {len(combinations)} unique team combinations")
    return combinations

def generate_chat_config(members):
    logger.info(f"Generating chat config for team: {members}")
    team_context = f"""This is a software development team working on their first product release.
    Team members and roles:
    {', '.join([f'{name} ({ROLES[name]})' for name in members])}
    """
    
    prompt = f"""{team_context}
    Generate a chat channel configuration.
    For the chat channel, provide:
    1. A short, specific channel name (max 5 words). Be creative, avoid generic names like product planning. Imagine what members of this team could be discussing.
    2. A brief description of the channel's purpose (max 15 words)
    
    Format: NAME: channel name | DESC: channel description
    
    Make sure the configuration is realistic for a software development team."""

    try:
        response = openai.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7
        )
        
        name, description = response.choices[0].message.content.strip().split('|')
        name = name.split(':')[1].strip()
        description = description.split(':')[1].strip()
        
        logger.info(f"Generated chat config - Name: {name} | Description: {description}")
        return {
            "members": members,
            "name": name,
            "description": description
        }
    except Exception as e:
        logger.error(f"Error generating chat config: {str(e)}")
        raise

def generate_conversation(members, topic, num_messages=25):
    logger.info(f"Generating conversation for topic: {topic} ({num_messages} messages)")
    prompt = f"""Generate a realistic work conversation about {topic}.
    Conversation between team members:
    {', '.join([f'{name} ({ROLES[name]})' for name in members])}
    
    Format: USERNAME: message
    Generate exactly {num_messages} messages. Do not add any extra characters or formatting, especially to the user names! Be extremely strict about the provided format.
    Keep it natural and work-focused.
    Include technical discussions, planning, and problem-solving."""

    try:
        response = openai.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7
        )
        
        messages = response.choices[0].message.content.strip().split('\n')
        logger.info(f"Generated {len(messages)} messages for conversation")
        return messages
    except Exception as e:
        logger.error(f"Error generating conversation: {str(e)}")
        raise

def create_chat(name, description):
    logger.info(f"Creating chat: {name}")
    chat_data = {
        "name": name,
        "description": description,
        "type": "CHANNEL"
    }
    try:
        response = requests.post(f"{API_BASE}/local/chat", json=chat_data)
        response.raise_for_status()
        chat = response.json()
        logger.info(f"Created chat with ID: {chat['id']}")
        return chat
    except requests.exceptions.RequestException as e:
        logger.error(f"Error creating chat: {str(e)}")
        if hasattr(e.response, 'text'):
            logger.error(f"Response: {e.response.text}")
        raise

def create_chat_member(chat_id, user_id):
    logger.info(f"Creating chat member - Chat ID: {chat_id}, User ID: {user_id}")
    member_data = {
        "chatId": chat_id,
        "userId": user_id,
        "joinedAt": datetime.now().isoformat()
    }
    try:
        response = requests.post(f"{API_BASE}/local/chatmember", json=member_data)
        response.raise_for_status()
        member = response.json()
        logger.info(f"Created chat member with ID: {member['id']}")
        return member
    except requests.exceptions.RequestException as e:
        logger.error(f"Error creating chat member: {str(e)}")
        if hasattr(e.response, 'text'):
            logger.error(f"Response: {e.response.text}")
        raise

def create_message(chat_id, sender_id, content):
    logger.debug(f"Creating message in chat {chat_id} from {sender_id}")
    message_data = {
        "chatId": chat_id,
        "senderId": sender_id,
        "content": content
    }
    try:
        response = requests.post(f"{API_BASE}/local/message", json=message_data)
        response.raise_for_status()
        message = response.json()
        logger.debug(f"Created message with ID: {message['id']}")
        return message
    except requests.exceptions.RequestException as e:
        logger.error(f"Error creating message: {str(e)}")
        if hasattr(e.response, 'text'):
            logger.error(f"Response: {e.response.text}")
        raise

def main():
    logger.info("Starting main execution")
    try:
        # Generate dynamic team combinations
        users = list(USER_IDS.keys())
        team_combinations = generate_team_combinations(users)
        
        logger.info("Generating chat configurations")
        chat_configs = [generate_chat_config(members) for members in team_combinations]
        logger.info(f"Generated {len(chat_configs)} chat configurations")
        
        for idx, chat_config in enumerate(chat_configs, 1):
            logger.info(f"Processing chat configuration {idx}/{len(chat_configs)}")
            
            # Create chat and get its ID from response
            chat = create_chat(chat_config["name"], chat_config["description"])
            chat_id = chat["id"]
            
            # Create chat members
            logger.info(f"Creating {len(chat_config['members'])} chat members")
            for member in chat_config["members"]:
                create_chat_member(chat_id, USER_IDS[member])
            
            # Generate and create messages
            logger.info("Generating conversation messages")
            conversation = generate_conversation(chat_config["members"], chat_config["description"])
            
            logger.info(f"Creating {len(conversation)} messages")
            message_count = 0
            for msg in conversation:
                logger.info(f"Processing message: {repr(msg)}")
                try:
                    if not msg or ':' not in msg:
                        logger.warning(f"Message doesn't contain ':' separator: {repr(msg)}")
                        continue
                        
                    sender, content = msg.split(':', 1)
                    sender = sender.strip().lower()
                    content = content.strip()
                    
                    logger.info(f"Split message - Sender: {repr(sender)}, Content: {repr(content)}")
                    
                    if sender in USER_IDS:
                        create_message(chat_id, USER_IDS[sender], content)
                        message_count += 1
                    else:
                        logger.warning(f"Unknown sender: {repr(sender)}")
                except ValueError as e:
                    logger.warning(f"ValueError processing message: {repr(msg)}", exc_info=True)
                    continue
                except Exception as e:
                    logger.error(f"Error processing message: {repr(msg)}", exc_info=True)
                    continue
            logger.info(f"Created {message_count} messages for chat {chat_id}")
        
        logger.info("Database initialization completed successfully")
    except Exception as e:
        logger.error(f"Fatal error during execution: {str(e)}", exc_info=True)
        raise

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("Script interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger.error("Script failed with error", exc_info=True)
        sys.exit(1)