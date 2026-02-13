import os
import shutil
import uuid
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pymongo import MongoClient
from random import sample
from dotenv import load_dotenv
from faster_whisper import WhisperModel

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

# ==========================
# MongoDB
# ==========================

client = MongoClient(
  MONGO_URI,
  tls=True,
  tlsAllowInvalidCertificates=True  # ⚠️ Quan trọng với Render
)

db = client["phq_app"]
questions_col = db["questions"]

# ==========================
# Load Whisper Model (tiny cho RAM thấp)
# ==========================

print("🔄 Loading Whisper model...")
model = WhisperModel("tiny", compute_type="int8")
print("✅ Whisper model loaded!")

# ==========================
# Routes
# ==========================

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
    print("Database error:", e)
    raise HTTPException(status_code=500, detail="Database error")


# ==========================
# Whisper Transcribe Endpoint
# ==========================

@app.post("/transcribe")
async def transcribe(file: UploadFile = File(...)):
  try:
    # Tạo tên file tạm
    filename = f"{uuid.uuid4()}.wav"
    file_path = os.path.join(UPLOAD_DIR, filename)

    # Lưu file upload
    with open(file_path, "wb") as buffer:
      shutil.copyfileobj(file.file, buffer)

    # Chạy Whisper
    segments, _ = model.transcribe(file_path)

    text = ""
    for segment in segments:
      text += segment.text

    # Xóa file sau khi xử lý
    os.remove(file_path)

    return {
      "ok": True,
      "text": text.strip()
    }

  except Exception as e:
    print("❌ Transcribe error:", e)
    raise HTTPException(status_code=500, detail="Transcription failed")