require("dotenv").config()

const express = require("express")
const mongoose = require("mongoose")
const cors = require("cors")
const bcrypt = require("bcrypt")

const app = express()

// ================= MIDDLEWARE =================
app.use(express.json())
app.use(cors())

app.use((req, res, next) => {
    console.log("===================================")
    console.log("📌 METHOD:", req.method)
    console.log("📌 URL:", req.url)
    console.log("📌 BODY:", req.body)
    console.log("===================================")
    next()
})

// ================= MONGODB =================
mongoose.connect(process.env.MONGO_URI)
.then(() => console.log("✅ MongoDB Connected"))
.catch(err => console.log("❌ Mongo Error:", err))

// ================= SCHEMA =================
const HistorySchema = new mongoose.Schema({
    score: Number,
    level: String,
    date: Date,

    // 🔥 FIX: thêm chi tiết
    quizScore: Number,
    voiceResult: Number,
    faceResult: Boolean

}, { _id: false })

const AccountSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    email: String,

    finalScore: { type: Number, default: null },
    level: { type: String, default: null },
    lastTestTime: { type: Date, default: null },

    // 🔥 history list
    history: {
        type: [HistorySchema],
        default: []
    }
})

const Account = mongoose.model("accounts", AccountSchema)


// ================= REGISTER =================
app.post("/register", async (req, res) => {
    try {
        const { username, password, email } = req.body

        if (!username || !password) {
            return res.json({ ok: false, message: "Thiếu dữ liệu" })
        }

        const exist = await Account.findOne({ username })
        if (exist) {
            return res.json({ ok: false, message: "Username đã tồn tại" })
        }

        const hashed = await bcrypt.hash(password, 10)

        const user = new Account({
            username,
            password: hashed,
            email,
            history: []
        })

        await user.save()

        res.json({ ok: true, message: "Đăng ký thành công" })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})


// ================= LOGIN =================
app.post("/login", async (req, res) => {
    try {
        const { username, password } = req.body

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "Sai tài khoản hoặc mật khẩu" })
        }

        const isMatch = await bcrypt.compare(password, user.password)

        if (!isMatch) {
            return res.json({ ok: false, message: "Sai tài khoản hoặc mật khẩu" })
        }

        res.json({
            ok: true,
            message: "Login success",
            data: user
        })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})


// ================= GET USER =================
app.get("/get-user/:username", async (req, res) => {
    try {
        const { username } = req.params

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "User không tồn tại" })
        }

        res.json({
            ok: true,
            data: user
        })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})


// ================= UPDATE RESULT =================
app.post("/update-result", async (req, res) => {
    try {
        const {
            username,
            finalScore,
            level,
            lastTestTime,
            quizScore,
            voiceResult,
            faceResult
        } = req.body

        if (!username) {
            return res.json({ ok: false, message: "Thiếu username" })
        }

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "User không tồn tại" })
        }

        const date = lastTestTime
            ? new Date(lastTestTime)
            : new Date()

        // 🔥 PUSH HISTORY (mới nhất lên đầu)
        user.history.unshift({
            score: finalScore,
            level: level,
            date: date,
            quizScore: quizScore || 0,
            voiceResult: voiceResult || 0,
            faceResult: faceResult || false
        })

        // 🔥 LIMIT 20 record (tránh phình DB)
        if (user.history.length > 20) {
            user.history = user.history.slice(0, 20)
        }

        // 🔥 update latest
        user.finalScore = finalScore
        user.level = level
        user.lastTestTime = date

        await user.save()

        res.json({
            ok: true,
            message: "Saved to history"
        })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})


// ================= TEST =================
app.get("/", (req, res) => {
    res.send("API RUNNING 🚀")
})


// ================= START =================
const PORT = process.env.PORT || 3000

app.listen(PORT, () => {
    console.log("🚀 Server running on port " + PORT)
})