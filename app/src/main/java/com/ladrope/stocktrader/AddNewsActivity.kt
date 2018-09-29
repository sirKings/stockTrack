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
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.ladrope.stocktrader.model.Expert
import com.ladrope.stocktrader.model.NewsModel
import com.ladrope.stocktrader.model.NotificationsModel
import kotlinx.android.synthetic.main.activity_add_news.*
import java.io.ByteArrayOutputStream
import java.util.*

class AddNewsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    var nwsMsg: TextView? = null
    var imageUrl: String? = null
    var nwsLink: TextView? = null

    val titleList = arrayListOf<String>()
    val titleObjectList = ArrayList<Expert>()

    var nwsTitle: Spinner? = null
    var ttl: String? = null
    var titleId: Int? = null
    var titleAdapter: ArrayAdapter<String>? = null


    private var dialog: Dialog? = null

    private val imagePermissions = arrayOf("android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE")
    private var falg_camera_permission = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        nwsMsg = addNewsMgs
        nwsTitle = addNewsTitle
        nwsLink = addNewsLink

        getTitles()

        nwsTitle?.onItemSelectedListener = this

        titleAdapter = ArrayAdapter(this, R.layout.spiner_row, titleList)
        // Set layout to use when the list of choices appear
        titleAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        nwsTitle?.adapter = titleAdapter
        nwsTitle?.setSelection(0)

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.e("Nothing", "Selected")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Log.e("Adapter", "1")
                nwsTitle?.setSelection(p2)
                ttl = titleList[p2]
    }

    fun addImage(view: View){
        imagePermission()
    }


    fun postWithNotification(view: View){
        view.isClickable = false
        postAsTip(true)
    }

    fun postWithoutNotification(view: View){
        view.isClickable = false
        postAsTip(false)
    }

    fun postNews(withNofify: Boolean){
        if (nwsMsg?.text.isNullOrEmpty()){
            Toast.makeText(this, "Please enter news details", Toast.LENGTH_SHORT).show()
        }else {
            val news = NewsModel()
            news.image = imageUrl
            news.title = ttl
            //news.link = tipId
            news.msg = nwsMsg?.text.toString()
            news.date = System.currentTimeMillis() * -1
            //postAsTip()

            if (!nwsLink?.text.isNullOrEmpty()){
                news.link = nwsLink?.text.toString()
            }

            FirebaseDatabase.getInstance().reference.child("news")
                    .push()
                    .setValue(news)
                    .addOnCompleteListener {
                        Toast.makeText(this@AddNewsActivity, "News posted", Toast.LENGTH_SHORT).show()
                        this@AddNewsActivity.finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@AddNewsActivity, "News posting failed, check your internet connection", Toast.LENGTH_SHORT).show()
                    }
            if(withNofify){
                notifyUsers()
            }
        }

    }

    fun postAsTip(withNofify: Boolean){
//        val newsAsTp = Tip()
//        newsAsTp.isNewstype = true
//        newsAsTp.comment = nwsMsg?.text.toString()
//        newsAsTp.date = System.currentTimeMillis()
//        newsAsTp.rank = newsAsTp.date!! * -1
//        newsAsTp.expertName = ttl
//
//        //val key = FirebaseDatabase.getInstance().reference.child("tips").push().key
//
//        newsAsTp.id = key
//
//        FirebaseDatabase.getInstance().reference
//                .child("tips")
//                .child(key!!)
//                .setValue(newsAsTp)
        postNews(withNofify)
    }


    fun notifyUsers(){
        val news = NotificationsModel()
        news.title = ttl
        news.msg = nwsMsg?.text.toString()
        news.date = System.currentTimeMillis() * -1


        FirebaseDatabase.getInstance().reference.child("notifications")
                .push()
                .setValue(news)
                .addOnCompleteListener {
                    //Toast.makeText(this@AddNewsActivity, "Notifications posted", Toast.LENGTH_SHORT).show()
                    //this@AddNewsActivity.finish()
                }.addOnFailureListener {
                    Toast.makeText(this@AddNewsActivity, "Notifications posting failed, check your internet connection", Toast.LENGTH_SHORT).show()
                }

        sendNotification(news.msg!!, "Stock Track", "NEWS", "link")

    }

    private fun imagePermission() {
        Log.e("Image", "Permision")
        if (ContextCompat.checkSelfPermission(this, imagePermissions[0]) == 0 || ContextCompat.checkSelfPermission(this, imagePermissions[1]) == 0) {
            falg_camera_permission = "1"
            choosePhoto()
            return
        }
        val builder: AlertDialog.Builder
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, imagePermissions[0]) && ActivityCompat.shouldShowRequestPermissionRationale(this, imagePermissions[1])) {
            builder = AlertDialog.Builder(this)
            builder.setTitle("Need Multiple Permissions")
            builder.setMessage("This app needs Camera and Location permissions.")
            builder.setPositiveButton("Grant") { dialog, which ->
                dialog.cancel()
                ActivityCompat.requestPermissions(this@AddNewsActivity, imagePermissions, 100)
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
        } else if (ContextCompat.checkSelfPermission(this, imagePermissions[0]) == 0) {
            builder = AlertDialog.Builder(this)
            builder.setTitle("Need Multiple Permissions")
            builder.setMessage("This app needs Camera and Location permissions.")
            builder.setPositiveButton("Grant") { dialog, which ->
                dialog.cancel()
                val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                intent.data = Uri.fromParts("package", packageName, null)
                startActivityForResult(intent, 101)
                Toast.makeText(baseContext, "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show()
            }
            builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
            builder.show()
        } else {
            ActivityCompat.requestPermissions(this, imagePermissions, 100)
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
        showProgressHUD(this, "Uploading Image, please wait")
        val fileRef = FirebaseStorage.getInstance().getReference().child("profileImages").child(System.currentTimeMillis().toString() + FirebaseAuth.getInstance().uid!!)

        val uploadTask = fileRef.putFile(data)


        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                hideProgressHud()
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                //throw task.exception
            }

            // Continue with the task to get the download URL

            fileRef.getDownloadUrl()
        }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                imageUrl = downloadUri.toString()
                hideProgressHud()
                Log.e("downloadUrl", downloadUri.toString())
            } else {
                // Handle failures
                // ...
            }
        })
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

    public override fun onActivityResult(r1: Int, r2: Int, data: Intent) {
        var imageUri: Uri?
        when (r1) {
            0 -> {
                try {
                    imageUri = getImageUri(this, data.extras!!.get("data") as Bitmap)

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

    fun getTitles(){
        FirebaseDatabase.getInstance().reference
                .child("headings")
                .orderByChild("title")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@AddNewsActivity, "Could not fetch experts, Check your internet connections", Toast.LENGTH_SHORT).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            for (i in p0.children){
                                val expert = i.getValue(Expert::class.java)
                                titleList.add(expert?.title!!)
                                titleObjectList.add(expert)
                                titleAdapter?.notifyDataSetChanged()
                            }
                        }
                    }

                })
    }
}
