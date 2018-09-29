package com.ladrope.stocktrader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_call_open.*



class CallOpenActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var progress: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_open)

        mAuth = FirebaseAuth.getInstance()

        progress = adminProgress
        progress?.visibility = View.GONE
        adminLogin.setOnClickListener {
            login()
        }
    }


    fun login(){
        //startLogin(false)
        val email = adminEmail.text.toString()
        val password = adminPass.text.toString()
        if (email.isNotEmpty() && password.length > 6){
            progress?.visibility = View.VISIBLE
            mAuth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener( object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            if (task.isSuccessful()) {
                                progress?.visibility = View.GONE
                                // Sign in success, update UI with the signed-in user's information
                                Log.e("Login","signInWithEmail:success")
                                isAdmin = true
                                persistInfo()
                                goHome()
                            } else {
                                progress?.visibility = View.GONE
                                // If sign in fails, display a message to the user.

                                Log.e("Login","signInWithEmail:failure")
                                //startLogin(true)
                                Log.e("Error", "")
                                Toast.makeText(this@CallOpenActivity, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                //updateUI(null)
                            }

                            // ...
                        }
                    })?.addOnFailureListener {
                        Log.e("Login", "failed")

                        Toast.makeText(this, "Login failed, check internet connection", Toast.LENGTH_SHORT).show()
                    }
        }else{
            Toast.makeText(this, "Please enter email and password correctly", Toast.LENGTH_SHORT).show()
        }
    }


    fun goHome(){
        startActivity(Intent(this, Home::class.java))
        finish()
    }


    fun persistInfo(){
        val editor = getSharedPreferences("com.ladrope.stockTrader", Context.MODE_PRIVATE).edit()
        editor.putBoolean("isAdmin", true)
        editor.apply()
    }
}
