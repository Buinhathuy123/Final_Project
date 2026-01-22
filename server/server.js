require("dotenv").config();
const express = require("express");
const cors = require("cors");
const { MongoClient } = require("mongodb");

const multer = require("multer");
const fs = require("fs");
const axios = require("axios");
const FormData = require("form-data");

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const MONGO_URI = process.env.MONGO_URI;
const OPENAI_API_KEY = process.env.OPENAI_API_KEY;

if (!MONGO_URI) {
  console.error("❌ MONGO_URI is missing");
  process.exit(1);
}

if (!OPENAI_API_KEY) {
  console.error("❌ OPENAI_API_KEY is missing");
  process.exit(1);
}

const client = new MongoClient(MONGO_URI);
const upload = multer({ dest: "uploads/" });

/* ================== ROUTES ================== */

// Health check
app.get("/", (req, res) => {
  res.json({ ok: true, message: "Server is running" });
});

// GET questions
app.get("/questions", async (req, res) => {
  try {
    const size = parseInt(req.query.size || "9", 10);
    const db = client.db("phq_app");
    const questionsCol = db.collection("questions");

    const questions = await questionsCol
      .aggregate([{ $sample: { size } }])
      .toArray();

    res.json({ ok: true, count: questions.length, questions });
  } catch (err) {
    console.error(err);
    res.status(500).json({ ok: false, message: "Database error" });
  }
});

// 🎤 SPEECH TO TEXT
app.post("/speech-to-text", upload.single("audio"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ ok: false, error: "No audio file" });
    }

    const form = new FormData();
    form.append("file", fs.createReadStream(req.file.path));
    form.append("model", "whisper-1");
    form.append("language", "vi");

    const response = await axios.post(
      "https://api.openai.com/v1/audio/transcriptions",
      form,
      {
        headers: {
          ...form.getHeaders(),
          Authorization: `Bearer ${OPENAI_API_KEY}`,
        },
      }
    );

    fs.unlinkSync(req.file.path);

    res.json({
      ok: true,
      text: response.data.text,
    });

  } catch (err) {
    console.error(err.response?.data || err.message);
    res.status(500).json({ ok: false, error: "Speech failed" });
  }
});

/* ================== START SERVER ================== */

async function startServer() {
  try {
    await client.connect();
    console.log("✅ Connected to MongoDB");

    app.listen(PORT, () => {
      console.log(`🚀 Server listening on port ${PORT}`);
    });
  } catch (err) {
    console.error("❌ MongoDB connection failed:", err);
    process.exit(1);
  }
}

startServer();
