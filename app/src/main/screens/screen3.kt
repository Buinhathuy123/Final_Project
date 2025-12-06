package com.example.myapp
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
class screen3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dangky)
        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/ibe2gnFUzm/4ad1p1p1_expires_30_days.png").into(findViewById(R.id.rvu0n5tpd19))
        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/ibe2gnFUzm/lxuqcr0v_expires_30_days.png").into(findViewById(R.id.rsw9hdk7pik))
        Glide.with(this).load("https://storage.googleapis.com/tagjs-prod.appspot.com/v1/ibe2gnFUzm/8va3z231_expires_30_days.png").into(findViewById(R.id.rehbttukevro))
    }
}