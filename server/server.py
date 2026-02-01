import os
import shutil
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pymongo import MongoClient
from random import sample
from dotenv import load_dotenv

load_dotenv()

PORT = int(os.getenv("PORT", 3000))
MONGO_URI = os.getenv("MONGO_URI")

if not MONGO_URI:
  raise RuntimeError("❌ MONGO_URI is missing")

app = FastAPI()

app.add_middleware(
  CORSMiddleware,
  allow_origins=["*"],
  allow_credentials=True,
  allow_methods=["*"],
  allow_headers=["*"],
)

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

# MongoDB
client = MongoClient(
  MONGO_URI,
  tls=True,
  tlsAllowInvalidCertificates=True  # ⚠️ QUAN TRỌNG cho Render
)

db = client["phq_app"]
questions_col = db["questions"]

@app.get("/")
def health():
  return {"ok": True, "message": "Server is running 🚀"}

@app.get("/questions")
def get_questions(size: int = 9):
  try:
    total = questions_col.count_documents({})
    size = min(size, total)

    questions = list(questions_col.find())
    sampled = sample(questions, size)

    for q in sampled:
      q["_id"] = str(q["_id"])

    return {
      "ok": True,
      "count": len(sampled),
      "questions": sampled
    }
  except Exception as e:
    print(e)
    raise HTTPException(status_code=500, detail="Database error")
