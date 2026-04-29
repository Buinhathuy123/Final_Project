require("dotenv").config()

const express = require("express")
const mongoose = require("mongoose")
const bodyParser = require("body-parser")
const cors = require("cors")
const bcrypt = require("bcrypt")
const nodemailer = require("nodemailer")

const app = express()
const otpStore = {}

// ================= MIDDLEWARE =================
app.use(bodyParser.json())
app.use(cors())

// 🔥 LOG ALL REQUEST (QUAN TRỌNG NHẤT)
app.use((req, res, next) => {
    console.log("===================================")
    console.log("📌 METHOD:", req.method)
    console.log("📌 URL:", req.url)
    console.log("📌 BODY:", req.body)
    console.log("===================================")
    next()
})

// ================= MongoDB =================
mongoose.connect(process.env.MONGO_URI)
.then(() => console.log("✅ MongoDB Connected"))
.catch(err => console.log("❌ Mongo Error:", err))

// ================= Schema =================
const AccountSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    email: String,
    finalScore: { type: Number, default: null },
    level: { type: String, default: null },
    lastTestTime: { type: String, default: null }
})

const Account = mongoose.model("accounts", AccountSchema)

// ================= MAIL =================
const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 587,
    secure: false, // ⚠️ bắt buộc false cho port 587
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
})

// ================= REGISTER =================
app.post("/register", async (req, res) => {
    console.log("🔥 HIT /register")

    try {
        const { username, password, email } = req.body

        if (!username || !password) {
            return res.json({ ok: false, message: "Thiếu username hoặc password" })
        }

        const exist = await Account.findOne({ username })

        if (exist) {
            return res.json({ ok: false, message: "Username đã tồn tại" })
        }

        const hashedPassword = await bcrypt.hash(password, 10)

        const account = new Account({
            username,
            password: hashedPassword,
            email
        })

        await account.save()

        console.log("✅ REGISTER SUCCESS:", username)

        res.json({
            ok: true,
            message: "Register success",
            data: account
        })

    } catch (err) {
        console.log("❌ REGISTER ERROR:", err)
        res.json({ ok: false, message: err.message })
    }
})

// ================= SEND OTP =================
app.post("/send-otp", async (req, res) => {
    console.log("===================================")
    console.log("🔥 HIT /send-otp")

    try {
        const { email } = req.body

        console.log("📩 EMAIL:", email)

        if (!email) {
            console.log("❌ THIẾU EMAIL")
            return res.json({ ok: false, message: "Thiếu email" })
        }

        // 🔑 TẠO OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString()
        console.log("🔑 OTP:", otp)

        // 📦 LƯU OTP
        otpStore[email] = {
            otp,
            expire: Date.now() + 5 * 60 * 1000
        }

        console.log("📦 OTP STORE:", otpStore[email])

        // ✉️ GỬI EMAIL
        console.log("📤 ĐANG GỬI EMAIL...")

        const info = await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: "OTP xác nhận đăng ký",
            text: `Mã OTP của bạn là: ${otp}`
        })

        // ✅ SUCCESS
        console.log("✅ EMAIL SENT SUCCESS")
        console.log("📨 RESPONSE:", info.response)
        console.log("===================================")

        return res.json({
            ok: true,
            message: "OTP đã gửi"
        })

    } catch (err) {
        // ❌ ERROR
        console.log("❌ SEND OTP ERROR FULL:", err)
        console.log("❌ MESSAGE:", err.message)
        console.log("❌ STACK:", err.stack)
        console.log("===================================")

        return res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= VERIFY OTP =================
app.post("/verify-otp", (req, res) => {
    console.log("🔥 HIT /verify-otp")

    const { email, otp } = req.body

    console.log("📩 EMAIL:", email)
    console.log("🔑 OTP INPUT:", otp)

    const data = otpStore[email]

    if (!data) {
        console.log("❌ OTP NOT FOUND")
        return res.json({ ok: false, message: "OTP không tồn tại" })
    }

    if (Date.now() > data.expire) {
        console.log("❌ OTP EXPIRED")
        delete otpStore[email]
        return res.json({ ok: false, message: "OTP hết hạn" })
    }

    if (data.otp !== otp) {
        console.log("❌ OTP WRONG")
        return res.json({ ok: false, message: "OTP sai" })
    }

    console.log("✅ OTP VERIFIED")

    delete otpStore[email]
    return res.json({ ok: true })
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

        console.log("✅ LOGIN SUCCESS:", username)

        res.json({
            ok: true,
            message: "Login success",
            data: user
        })

    } catch (err) {
        console.log("❌ LOGIN ERROR:", err)
        res.json({ ok: false, message: err.message })
    }
})

// ================= TEST =================
app.get("/", (req, res) => {
    res.send("API is running 🚀")
})

// ================= START =================
const PORT = process.env.PORT || 3000

app.listen(PORT, () => {
    console.log("🚀 Server running on port " + PORT)
})