package com.example.aplikasicua

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.aplikasicua.ml.Modeljagung
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView=findViewById(R.id.iv_picture)
        val fileName="labels.txt"
        val inputString=application.assets.open(fileName).bufferedReader().use { it.readText() }
        val townList=inputString.split("\n")

        var tv:TextView=findViewById(R.id.textView)
        var select:Button=findViewById(R.id.btn_select)
        select.setOnClickListener(View.OnClickListener {

            var intent:Intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.type="image/*"
            startActivityForResult(intent, 100)
        })

        var proses:Button=findViewById(R.id.btn_proses)
        proses.setOnClickListener(View.OnClickListener {
            var resized:Bitmap=Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val model = Modeljagung.newInstance(this)

// Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            var tbuffer=TensorImage.fromBitmap(resized)
            var byteBuffer=tbuffer.buffer
            inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            var max=getMax(outputFeature0.floatArray)

            tv.setText(townList[max])

// Releases model resources if no longer used.
            model.close()
        })

        //btn_take.isEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),100)
        }else{
            btn_take.isEnabled = true
        }

        btn_take.setOnClickListener{
            val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(i, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            var pic : Bitmap? = data?.getParcelableExtra<Bitmap>("data")
            iv_picture.setImageBitmap(pic)
        }
        imageView.setImageURI(data?.data)
        var uri: Uri?= data?.data
        bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,uri)

    }
    fun getMax(arr: FloatArray):Int{
        var ind=0
        var min=0.0f
        for(i in 0..1000){
            if (arr[i]>min){
                ind=i
                min= arr[i]
            }
        }
        return ind

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            btn_take.isEnabled = true
        }
    }

}