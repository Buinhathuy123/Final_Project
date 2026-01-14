require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { MongoClient } = require('mongodb');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const MONGO_URI = process.env.MONGO_URI;

if (!MONGO_URI) {
  console.error('❌ MONGO_URI is missing in .env');
  process.exit(1);
}

const client = new MongoClient(MONGO_URI);

async function startServer() {
  try {
    await client.connect();
    console.log('✅ Connected to MongoDB');

    const db = client.db('phq_app');
    const questionsCol = db.collection('questions');

    // Health check
    app.get('/', (req, res) => {
      res.json({ ok: true, message: 'Server is running' });
    });

    // GET /questions?size=9
    app.get('/questions', async (req, res) => {
      const size = parseInt(req.query.size || '9', 10);

      try {
        const questions = await questionsCol
          .aggregate([{ $sample: { size } }])
          .toArray();

        res.json({
          ok: true,
          count: questions.length,
          questions
        });
      } catch (err) {
        console.error('❌ Query error:', err);
        res.status(500).json({
          ok: false,
          message: 'Database error'
        });
      }
    });

    app.listen(PORT, () => {
      console.log(`🚀 Server listening on port ${PORT}`);
    });

  } catch (err) {
    console.error('❌ MongoDB connection failed:', err);
    process.exit(1);
  }
}

startServer();
