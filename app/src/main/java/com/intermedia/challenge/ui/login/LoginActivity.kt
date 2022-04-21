package com.intermedia.challenge.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.intermedia.challenge.ui.main.MainScreenActivity
import com.intermedia.challenge.R
import java.lang.Exception
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callBackManager: CallbackManager
    private lateinit var facebookButton: LoginButton
    private lateinit var googleButton: SignInButton

    //constants
    private companion object{
        private const val  RC_SIGN_IN=100
        private const val TAG="GOOGLE_SIGN_IN_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callBackManager= CallbackManager.Factory.create()
    }

    override fun onResume() {
        super.onResume()
        startFirebaseAuth()
    }

    private fun startFirebaseAuth() {
        val googleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions)

        firebaseAuth= FirebaseAuth.getInstance()
        checkUser()

        //google sign in button, click to begin
        googleButton=findViewById(R.id.googleButton)
        googleButton.setOnClickListener {
            Log.d(TAG, "onCreate: begin Google Sign in")
            val intent=googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        facebookButton=findViewById(R.id.facebookButton)
        facebookButton.setReadPermissions("email", "public_profile")
        facebookButton.registerCallback(callBackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })

    }

    private fun checkUser() {
        val firebaseUser=firebaseAuth.currentUser
        if(firebaseUser!=null){
            startActivity(Intent(this@LoginActivity,MainScreenActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callBackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask=GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account=accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }catch (e: Exception){
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential= GoogleAuthProvider.getCredential(account!!.idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult->
                //login success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")
                //get logged in user
                val firebaseUser= firebaseAuth.currentUser
                //get user info
                val uid=firebaseUser!!.uid
                val email=firebaseUser!!.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email $email")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid $uid")

                //check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created...\n$email")
                    Toast.makeText(this@LoginActivity, "Account created...\n$email", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user")
                    Toast.makeText(this@LoginActivity, "LoggedIn...\n$email", Toast.LENGTH_SHORT).show()
                }

                //start profile activity
                startActivity(Intent(this@LoginActivity,MainScreenActivity::class.java))
                finish()

            }
            .addOnFailureListener{e->
                //login failed
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login failed due to ${e.message}")
                Toast.makeText(this@LoginActivity, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    startActivity(Intent(this@LoginActivity,MainScreenActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()

                }
            }
    }


}