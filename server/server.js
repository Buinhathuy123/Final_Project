require("dotenv").config()

const express = require("express")
const mongoose = require("mongoose")
const bodyParser = require("body-parser")
const cors = require("cors")
const bcrypt = require("bcrypt")
const nodemailer = require("nodemailer")

const app = express()
const otpStore = {}

app.use(bodyParser.json())
app.use(cors())

// ================= MongoDB =================
mongoose.connect(process.env.MONGO_URI)
.then(() => console.log("MongoDB Connected"))
.catch(err => console.log("Mongo Error:", err))

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

const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
})

// ================= REGISTER =================
app.post("/register", async (req, res) => {
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

        res.json({
            ok: true,
            message: "Register success",
            data: account
        })

    } catch (err) {
        res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= SEND OTP (REGISTER - GIỮ NGUYÊN) =================
app.post("/send-otp", async (req, res) => {
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

        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: "OTP xác nhận đăng ký",
            text: `Mã OTP của bạn là: ${otp}`
        })

        res.json({ ok: true, message: "OTP đã gửi" })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})

// ================= SEND OTP (QUÊN MẬT KHẨU - MỚI) =================
app.post("/send-otp-forgot", async (req, res) => {
    try {
        const { username, email } = req.body

        if (!username || !email) {
            return res.json({ ok: false, message: "Thiếu dữ liệu" })
        }

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({ ok: false, message: "User không tồn tại" })
        }

        // 🔥 CHECK EMAIL ĐÚNG USER
        if (user.email !== email) {
            return res.json({ ok: false, message: "Email không đúng" })
        }

        const otp = Math.floor(100000 + Math.random() * 900000).toString()

        otpStore[email] = {
            otp,
            expire: Date.now() + 5 * 60 * 1000
        }

        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: "OTP khôi phục mật khẩu",
            text: `Mã OTP của bạn là: ${otp}`
        })

        res.json({ ok: true, message: "OTP đã gửi (forgot)" })

    } catch (err) {
        res.json({ ok: false, message: err.message })
    }
})

// ================= VERIFY OTP (DÙNG CHUNG) =================
app.post("/verify-otp", (req, res) => {
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
    return res.json({ ok: true })
})

// ================= LOGIN =================
app.post("/login", async (req, res) => {
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

        res.json({
            ok: true,
            message: "Login success",
            data: user
        })

    } catch (err) {
        res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= CHANGE PASSWORD (CHO QUÊN PASS - KHÔNG CẦN currentPassword) =================
app.post("/change-password", async (req, res) => {
    try {
        const { username, newPassword } = req.body

        if (!username || !newPassword) {
            return res.json({
                ok: false,
                message: "Thiếu dữ liệu"
            })
        }

        const user = await Account.findOne({ username })

        if (!user) {
            return res.json({
                ok: false,
                message: "User không tồn tại"
            })
        }

        // 🔥 KHÔNG CHO TRÙNG PASSWORD CŨ
        const isSame = await bcrypt.compare(newPassword, user.password)

        if (isSame) {
            return res.json({
                ok: false,
                message: "Mật khẩu mới không được trùng mật khẩu cũ"
            })
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10)

        user.password = hashedPassword
        await user.save()

        return res.json({
            ok: true,
            message: "Đổi mật khẩu thành công"
        })

    } catch (err) {
        return res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= UPDATE RESULT =================
app.post("/update-result", async (req, res) => {
    try {
        const { username, finalScore, level } = req.body

        if (!username) {
            return res.json({
                ok: false,
                message: "Thiếu username"
            })
        }

        const now = new Date().toLocaleString("vi-VN")

        const user = await Account.findOneAndUpdate(
            { username: username },
            {
                finalScore: finalScore,
                level: level,
                lastTestTime: now
            },
            { new: true }
        )

        if (!user) {
            return res.json({
                ok: false,
                message: "Không tìm thấy user"
            })
        }

        res.json({
            ok: true,
            message: "Lưu kết quả thành công",
            data: user
        })

    } catch (err) {
        res.json({
            ok: false,
            message: err.message
        })
    }
})

// ================= GET USER =================
app.get("/user/:username", async (req, res) => {
    try {
        const user = await Account.findOne({ username: req.params.username })

        if (!user) {
            return res.json({
                ok: false,
                message: "Không tìm thấy user"
            })
        }

        res.json({
            ok: true,
            data: user
        })

    } catch (err) {
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
    console.log("Server running on port " + PORT)
})