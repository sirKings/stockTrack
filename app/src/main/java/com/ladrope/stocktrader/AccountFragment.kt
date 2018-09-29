package com.ladrope.stocktrader


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_account.view.*
import java.io.ByteArrayOutputStream


/**
 * A simple [Fragment] subclass.
 *
 */
class AccountFragment : Fragment() {


    private var dialog: Dialog? = null

    private val imagePermissions = arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private var falg_camera_permission = "0"

    var userImg: ImageView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        view.expertBtn.setOnClickListener {
            expertBtnSelected()
        }

        view.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(activity, MainActivity::class.java))
            activity?.finish()
        }

        userImg = view.userImg

        view.termsBtn.setOnClickListener {
            val intent = Intent(activity, TermsActivity::class.java)
            intent.putExtra("src", "src='https://docs.google.com/document/d/1-SQPksSYX9RmLMs5qCEx1PLwE-UYqmyP9nza37jYuMY/edit'" )
            intent.putExtra("title", "Terms and Conditions")
            startActivity(intent)
        }

        view.privacyBtn.setOnClickListener {
            val intent = Intent(activity, TermsActivity::class.java)
            intent.putExtra("src", "src='https://docs.google.com/document/d/1B_NILaqBqhzfP4ZjqdCAHx-ywgVzckqbb1w1bbAOuZk/edit'" )
            intent.putExtra("title", "Privacy")
            startActivity(intent)
        }

        view.aboutUs.setOnClickListener {
            val intent = Intent(activity, TermsActivity::class.java)
            intent.putExtra("src", "src='https://docs.google.com/document/d/1aooMdFa10Kxc8ilyTjNWhf4Z7JD2Map1PymK6K90ypg/edit'" )
            intent.putExtra("title", "Disclaimer")
            startActivity(intent)
        }

        view.adminBtn.setOnClickListener {
            startActivity(Intent(activity, HeadingActivity::class.java))
        }


        if (!isAdmin){
            view.adminBtn.visibility = View.GONE
            view.expertBtn.visibility = View.GONE
        }


        if(isAdmin){
            view.userNameAct.text = "Welcome Admin"
        }else{
            view.userNameAct.text = "Welcome "+FirebaseAuth.getInstance().currentUser?.displayName
        }


        Picasso.with(activity).load(FirebaseAuth.getInstance().currentUser?.photoUrl).into(view.userImg)

        view.addUserImageBtn.setOnClickListener {
            imagePermission()
        }

        view.openCallBtn.setOnClickListener {
            val intent = Intent(activity, TradeActivity::class.java)
            startActivity(intent)
        }

        view.discussionBtn.setOnClickListener {
            val intent = Intent(activity, ReadMoreActivity::class.java)
            intent.putExtra("where", "forum")
            startActivity(intent)
        }

        return view
    }

    fun expertBtnSelected(){
        startActivity(Intent(activity, ExpertActivity::class.java))
    }

    private fun imagePermission() {
        Log.e("Image", "Permision")
        if (ContextCompat.checkSelfPermission(activity!!, imagePermissions[0]) == 0 || ContextCompat.checkSelfPermission(activity!!, imagePermissions[1]) == 0) {
            falg_camera_permission = "1"
            choosePhoto()
            return
        }
        val builder: AlertDialog.Builder
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, imagePermissions[0]) && ActivityCompat.shouldShowRequestPermissionRationale(activity!!, imagePermissions[1])) {
            builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Need Multiple Permissions")
            builder.setMessage("This app needs Camera and Location permissions.")
            builder.setPositiveButton("Grant") { dialog, which ->
                dialog.cancel()
                ActivityCompat.requestPermissions(activity!!, imagePermissions, 100)
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
        } else if (ContextCompat.checkSelfPermission(activity!!, imagePermissions[0]) == 0) {
            builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Need Multiple Permissions")
            builder.setMessage("This app needs Camera and Location permissions.")
            builder.setPositiveButton("Grant") { dialog, which ->
                dialog.cancel()
                val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                intent.data = Uri.fromParts("package", activity!!.packageName, null)
                startActivityForResult(intent, 101)
                Toast.makeText(activity!!, "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show()
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
        } else {
            ActivityCompat.requestPermissions(activity!!, imagePermissions, 100)
        }
    }

    @SuppressLint("Override")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (grantResults.size <= 0 || grantResults[0] != 0) {
                falg_camera_permission = "0"
            } else {
                falg_camera_permission = "1"
            }
        }
    }

    fun choosePhoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = "android.intent.action.GET_CONTENT"
        startActivityForResult(Intent.createChooser(intent, "Select file to upload "), 1)
    }

    fun uploadImageToFirebase(data: Uri) {
        showProgressHUD(activity!!, "Uploading Image, please wait")
        val fileRef = FirebaseStorage.getInstance().getReference().child("profileImages").child(System.currentTimeMillis().toString() + FirebaseAuth.getInstance().uid!!)

        val uploadTask = fileRef.putFile(data)


        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                hideProgressHud()
                Toast.makeText(activity!!, "Upload failed", Toast.LENGTH_SHORT).show()
                //throw task.exception
            }

            // Continue with the task to get the download URL

            fileRef.getDownloadUrl()
        }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                saveImageUrl(downloadUri.toString())
                hideProgressHud()
                Log.e("downloadUrl", downloadUri.toString())
            } else {
                // Handle failures
                // ...
            }
        })
    }

    fun saveImageUrl(str: String){
        val profileUpdates = UserProfileChangeRequest.Builder()
        .setPhotoUri(Uri.parse(str))
        .build()
        FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
            Picasso.with(activity).load(str).into(userImg)
        }?.addOnFailureListener {
            Toast.makeText(activity, "Update failed, check internet connections", Toast.LENGTH_SHORT).show()
        }
    }

    fun showProgressHUD(context: Context, message: String) {
        try {
            dialog = ProgressDialog.show(context, context.getString(R.string.app_name), message)
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.setCancelable(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun hideProgressHud() {
        try {
            if (dialog != null && dialog?.isShowing!!) {
                dialog?.dismiss()
            }
        } catch (e: Exception) {
        }

    }

    override fun onActivityResult(r1: Int, r2: Int, data: Intent) {
        var imageUri: Uri?
        when (r1) {
            0 -> {
                try {
                    imageUri = getImageUri(activity!!, data.extras!!.get("data") as Bitmap)

                    uploadImageToFirebase(imageUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    imageUri = data.data
                    uploadImageToFirebase(imageUri)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }

            }
            1 -> try {
                imageUri = data.data
                Log.e("imageUri", imageUri!!.toString())
                uploadImageToFirebase(imageUri)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, ByteArrayOutputStream())
        return Uri.parse(MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null))
    }

}
