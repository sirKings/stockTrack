package com.ladrope.stocktrader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.branch.referral.Branch
import io.branch.referral.BranchError
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {



    var RC_SIGN_IN = 1298
    private var progressBar: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null

    //facebook sign in manager
    private var mCallbackManager: CallbackManager? = null
    private var mlogin: LoginButton? = null

    //google sign in client
    private var mGoogleSignInClient: GoogleSignInClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        progressBar = this.progressBar1
        progressBar?.visibility = View.GONE

        if (mAuth?.currentUser?.uid != null){

                val homeIntent = Intent(this, Home::class.java)
                startActivity(homeIntent)
                finish()

        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbackManager = CallbackManager.Factory.create()

        mlogin = login_button
        mlogin?.setReadPermissions("email", "public_profile")
        LoginManager.getInstance().registerCallback(mCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        // App code
                        firebaseAuthWithFacebook(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        // App code
                        Toast.makeText(this@MainActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                        //val error: Int = resources.getIdentifier("error", "string", packageName)
                        Toast.makeText(this@MainActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                })

    }


    //google signin code
    fun signInWithGoogle(view: View) {

        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                println("Google sign in failed")
                // ...
                Toast.makeText(this, "Login failed, check internet connection", Toast.LENGTH_SHORT).show()
            }

        }

        // facebook callbackmanager
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        println("firebaseAuthWithGoogle:" + acct.id!!)
        progressBar?.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        println("signInWithCredential:success")
                        progressBar?.visibility = View.GONE

                        goHome()
                    } else {
                        // If sign in fails, display a message to the user.
                        println("signInWithCredential:failure")
                        progressBar?.visibility = View.GONE
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                    // ...
                }
    }

    // facebook signin
    fun signInWithFacebook(view: View){
        mlogin?.performClick()
    }


    private fun firebaseAuthWithFacebook(token: AccessToken) {
        println("firebaseAuthWithFacebook:" + token)
        progressBar?.visibility = View.VISIBLE
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        println("signInWithCredential:success")
                        progressBar?.visibility = View.GONE

                        goHome()
                    } else {
                        // If sign in fails, display a message to the user.
                        println("signInWithCredential:failure")
                        progressBar?.visibility = View.GONE
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                    // ...
                }
    }

    fun goHome(){
        persistInfo()
        startActivity(Intent(this, Home::class.java))
        finish()
    }

    fun  adminLogin(view: View){
        startActivity(Intent(this, CallOpenActivity::class.java))
    }

    fun openTerms(view: View){
        val intent = Intent(this, TermsActivity::class.java)
        intent.putExtra("src", "src='https://docs.google.com/document/d/1-SQPksSYX9RmLMs5qCEx1PLwE-UYqmyP9nza37jYuMY/edit'" )
        intent.putExtra("title", "Terms and Conditions")
        startActivity(intent)
    }

    fun persistInfo(){
        val editor = getSharedPreferences("com.ladrope.stockTrader", Context.MODE_PRIVATE).edit()
        editor.putBoolean("isAdmin", false)
        editor.apply()
    }

    override fun onStart() {
        super.onStart()
        val branch = Branch.getInstance()
        // Branch init
        branch.initSession(object: Branch.BranchReferralInitListener {
            override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
                if (error == null)
                {
                    // params are the deep linked params associated with the link that the user clicked -> was re-directed to this app
                    // params will be empty if no data found
                    // ... insert custom logic here ...
                    Log.i("BRANCH SDK", referringParams.toString())
                }
                else
                {
                    Log.i("BRANCH SDK", error.getMessage())
                }
            }
        }, this.getIntent().getData(), this)
    }
    override fun onNewIntent(intent:Intent) {
        this.setIntent(intent)
    }




}
