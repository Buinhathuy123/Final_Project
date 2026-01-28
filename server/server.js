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
  console.error('MONGO_URI missing. Please copy .env.example to .env and set MONGO_URI');
  process.exit(1);
}

const client = new MongoClient(MONGO_URI);

async function start() {
  try {
    await client.connect();
    const db = client.db('phq_app'); // tên db
    const questionsCol = db.collection('questions');

    // Thêm route kiểm tra server
    app.get('/', (req, res) => {
      res.send('Server is running!');
    });

    // GET /questions?size=9
    app.get('/questions', async (req, res) => {
      const size = parseInt(req.query.size || '9', 10);
      try {
        const docs = await questionsCol.aggregate([
          { $sample: { size } }
        ]).toArray();

        res.json({ ok: true, count: docs.length, questions: docs });
      } catch (err) {
        console.error(err);
        res.status(500).json({ ok: false, error: 'DB error' });
      }
    });

    app.listen(PORT, () => {
      console.log(`Server listening on http://localhost:${PORT}`);
    });

  } catch (err) {
    console.error('Failed to connect to MongoDB', err);
    process.exit(1);
  }
}

start();
