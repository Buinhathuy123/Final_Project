from fastapi import FastAPI, HTTPException
from pymongo import MongoClient
from dotenv import load_dotenv
import os
import certifi

# ================= LOAD ENV =================
load_dotenv()

PORT = int(os.getenv("PORT", 3000))
MONGO_URI = os.getenv("MONGO_URI")

if not MONGO_URI:
  raise RuntimeError("❌ MONGO_URI is missing")

# ================= MONGO CONNECT =================
try:
  client = MongoClient(
    MONGO_URI,
    tls=True,
    tlsCAFile=certifi.where()
  )
  db = client["phq_app"]
  questions_collection = db["questions"]
  print("✅ Connected to MongoDB")
except Exception as e:
  print("❌ MongoDB connection failed:", e)
  raise e

# ================= FASTAPI =================
app = FastAPI()

# ================= ROOT =================
@app.get("/")
def root():
  return {
    "ok": True,
    "message": "Server is running"
  }

# ================= GET QUESTIONS =================
@app.get("/questions")
def get_questions(size: int = 5):
  try:
    questions = list(questions_collection.find().limit(size))
    for q in questions:
      q["_id"] = str(q["_id"])  # convert ObjectId
    return {
      "ok": True,
      "size": len(questions),
      "data": questions
    }
  except Exception as e:
    print("❌ Database error:", e)
    raise HTTPException(status_code=500, detail="Database error")
