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

// 🔥 LOG FULL REQUEST
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

// ================= MAIL (FIX CHUẨN) =================
const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 465,
    secure: true, // ⚠️ QUAN TRỌNG
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    },
    tls: {
        rejectUnauthorized: false
    },
    connectionTimeout: 10000,
    greetingTimeout: 10000,
    socketTimeout: 10000
})

// 🔥 VERIFY SMTP
transporter.verify((err, success) => {
    if (err) {
        console.log("❌ SMTP ERROR:", err)
    } else {
        console.log("✅ SMTP READY - Có thể gửi email")
    }
})

// ================= SEND MAIL WITH RETRY =================
const sendMailWithRetry = async (mailOptions, retries = 2) => {
    try {
        console.log("📤 ĐANG GỬI EMAIL...")
        const info = await transporter.sendMail(mailOptions)
        console.log("✅ EMAIL SENT:", info.messageId)
        return true
    } catch (err) {
        console.log("❌ EMAIL ERROR:", err.message)

        if (retries > 0) {
            console.log("🔁 RETRY GỬI EMAIL...")
            return sendMailWithRetry(mailOptions, retries - 1)
        }

        return false
    }
}

// ================= REGISTER =================
app.post("/register", async (req, res) => {
    console.log("🔥 HIT /register")

    try {
        const { username, password, email } = req.body

        if (!username || !password) {
            return res.json({
                ok: false,
                message: "Thiếu username hoặc password"
            })
        }

        const exist = await Account.findOne({ username })

        if (exist) {
            return res.json({
                ok: false,
                message: "Username đã tồn tại"
            })
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
        res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= SEND OTP =================
app.post("/send-otp", async (req, res) => {
    console.log("🔥 HIT /send-otp")

    try {
        const { email } = req.body

        if (!email) {
            return res.json({ ok: false, message: "Thiếu email" })
        }

        const otp = Math.floor(100000 + Math.random() * 900000).toString()

        otpStore[email] = {
            otp,
            expire: Date.now() + 5 * 60 * 1000
        }

        console.log("📩 EMAIL:", email)
        console.log("🔑 OTP:", otp)
        console.log("📦 OTP STORE:", otpStore[email])

        const success = await sendMailWithRetry({
            from: `"OTP APP" <${process.env.EMAIL_USER}>`,
            to: email,
            subject: "Mã OTP xác nhận đăng ký",
            text: `Mã OTP của bạn là: ${otp}`
        })

        if (!success) {
            return res.json({
                ok: false,
                message: "Không gửi được email (SMTP bị chặn)"
            })
        }

        res.json({
            ok: true,
            message: "OTP đã gửi"
        })

    } catch (err) {
        console.log("❌ SEND OTP ERROR:", err)
        res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= SEND OTP FORGOT =================
app.post("/send-otp-forgot", async (req, res) => {
    console.log("🔥 HIT /send-otp-forgot")

    try {
        const { username, email } = req.body

        if (!username || !email) {
            return res.json({ ok: false, message: "Thiếu dữ liệu" })
        }

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "User không tồn tại" })
        }

        if (user.email !== email) {
            return res.json({ ok: false, message: "Email không đúng" })
        }

        const otp = Math.floor(100000 + Math.random() * 900000).toString()

        otpStore[email] = {
            otp,
            expire: Date.now() + 5 * 60 * 1000
        }

        console.log("📩 EMAIL:", email)
        console.log("🔑 OTP:", otp)

        const success = await sendMailWithRetry({
            from: `"OTP APP" <${process.env.EMAIL_USER}>`,
            to: email,
            subject: "OTP khôi phục mật khẩu",
            text: `Mã OTP của bạn là: ${otp}`
        })

        if (!success) {
            return res.json({
                ok: false,
                message: "Không gửi được email"
            })
        }

        res.json({ ok: true, message: "OTP đã gửi (forgot)" })

    } catch (err) {
        console.log("❌ SEND OTP FORGOT ERROR:", err)
        res.json({ ok: false, message: err.message })
    }
})

// ================= VERIFY OTP =================
app.post("/verify-otp", (req, res) => {
    console.log("🔥 HIT /verify-otp")

    const { email, otp } = req.body

    const data = otpStore[email]

    if (!data) {
        return res.json({ ok: false, message: "OTP không tồn tại" })
    }

    if (Date.now() > data.expire) {
        delete otpStore[email]
        return res.json({ ok: false, message: "OTP hết hạn" })
    }

    if (data.otp !== otp) {
        return res.json({ ok: false, message: "OTP sai" })
    }

    delete otpStore[email]
    console.log("✅ OTP VERIFIED")

    return res.json({ ok: true })
})

// ================= CHANGE PASSWORD =================
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
        console.log("❌ CHANGE PASSWORD ERROR:", err)
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
            return res.json({
                ok: false,
                message: "Sai tài khoản hoặc mật khẩu"
            })
        }

        const isMatch = await bcrypt.compare(password, user.password)

        if (!isMatch) {
            return res.json({
                ok: false,
                message: "Sai tài khoản hoặc mật khẩu"
            })
        }

        console.log("✅ LOGIN SUCCESS:", username)

        res.json({
            ok: true,
            message: "Login success",
            data: user
        })

    } catch (err) {
        console.log("❌ LOGIN ERROR:", err)
        res.json({
            ok: false,
            message: err.message
        })
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