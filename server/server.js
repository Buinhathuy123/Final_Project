require("dotenv").config()

const express = require("express")
const mongoose = require("mongoose")
const cors = require("cors")
const bcrypt = require("bcrypt")

const app = express()

// ================= MIDDLEWARE =================
app.use(express.json())
app.use(cors())

// LOG REQUEST
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
const AccountSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    email: String,
    finalScore: { type: Number, default: null },
    level: { type: String, default: null },
    lastTestTime: { type: String, default: null }
})

const Account = mongoose.model("accounts", AccountSchema)

// ================= REGISTER =================
app.post("/register", async (req, res) => {
    console.log("🔥 HIT /register")

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
            email
        })

        await user.save()

        console.log("✅ REGISTER SUCCESS:", username)

        res.json({
            ok: true,
            message: "Đăng ký thành công"
        })

    } catch (err) {
        console.log("❌ REGISTER ERROR:", err)
        res.json({ ok: false, message: err.message })
    }
})

// ================= LOGIN =================
app.post("/login", async (req, res) => {
    console.log("🔥 HIT /login")

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

// ================= CHANGE PASSWORD (FORGOT) =================
app.post("/change-password", async (req, res) => {
    console.log("🔥 HIT /change-password")

    try {
        const { username, newPassword } = req.body

        if (!username || !newPassword) {
            return res.json({ ok: false, message: "Thiếu dữ liệu" })
        }

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "User không tồn tại" })
        }

        const isSame = await bcrypt.compare(newPassword, user.password)

        if (isSame) {
            return res.json({
                ok: false,
                message: "Mật khẩu mới không được trùng mật khẩu cũ"
            })
        }

        user.password = await bcrypt.hash(newPassword, 10)
        await user.save()

        console.log("✅ CHANGE PASSWORD SUCCESS:", username)

        res.json({
            ok: true,
            message: "Đổi mật khẩu thành công"
        })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})
// ================= UPDATE RESULT =================
app.post("/update-result", async (req, res) => {
    console.log("🔥 HIT /update-result")

    try {
        const { username, finalScore, level, lastTestTime } = req.body

        if (!username) {
            return res.json({ ok: false, message: "Thiếu username" })
        }

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "User không tồn tại" })
        }

        user.finalScore = finalScore
        user.level = level

        // ✅ Ưu tiên dùng time từ client
        if (lastTestTime) {
            user.lastTestTime = new Date(lastTestTime)
        } else {
            // fallback nếu client không gửi
            user.lastTestTime = new Date()
        }

        await user.save()

        res.json({
            ok: true,
            message: "Cập nhật thành công"
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