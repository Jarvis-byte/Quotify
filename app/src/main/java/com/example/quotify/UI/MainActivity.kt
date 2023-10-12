package com.example.quotify.UI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.quotify.Handler.DeviceIdHandler
import com.example.quotify.RandomQuotesDataItem
import com.example.quotify.R
import com.example.quotify.ViewModel.MainViewModel
import com.example.quotify.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val TAGHttp = "Http Call"


class MainActivity : AppCompatActivity() {
    lateinit var mainViewModel: MainViewModel
    lateinit var quoteText: TextView
    lateinit var quoteAuthor: TextView
    lateinit var anim: LottieAnimationView
    lateinit var quote_image: ImageView
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        quote_image = findViewById(R.id.quote_image)
        anim = findViewById(R.id.loading_animation)
        anim.playAnimation()

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.quote = mainViewModel.getQuote()

        mainViewModel.getQuotelist().observe(this, Observer {
            anim.visibility = View.GONE
            binding.quoteText.visibility = View.VISIBLE
            binding.quoteAuthor.visibility = View.VISIBLE
            quote_image.visibility = View.VISIBLE
            setQuote(mainViewModel.getQuote())

        })
        mainViewModel.apicall()

        GlobalScope.launch(Dispatchers.Main) {
            val Token = getToken()
            if (Token != null) {
                Log.i("NEW_TOKEN", Token)
                val tokenData = mapOf("token" to Token)
                val DeviceId = DeviceIdHandler.getToken()
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("DeviceToken").document(DeviceId).set(tokenData)

            } else {
                // Handle the error
                Log.e("TOKEN_ERROR", "Failed to retrieve FCM token")
            }
        }


    }

    fun setQuote(quote: RandomQuotesDataItem?) {

        binding.quoteText.text = quote?.content
        binding.quoteAuthor.text = quote?.author

    }

    fun onShare(view: View) {
        val shareText = "${quoteText.text}\n\n— ${quoteAuthor.text}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)

    }


    fun onNext(view: View) {
        mainViewModel.nextQuote()
        setQuote(mainViewModel.getQuote())
//        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show()


    }

    fun onPrevious(view: View) {
        mainViewModel.PrevQuote()
        setQuote(mainViewModel.getQuote())


    }

    suspend fun getToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val task = FirebaseMessaging.getInstance().token.await()
                task
            } catch (e: Exception) {
                null
            }
        }
    }

}

