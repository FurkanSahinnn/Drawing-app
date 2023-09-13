package eu.tutorials.drawing_app

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.divyanshu.draw.widget.DrawView
import com.google.android.material.slider.Slider
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    // palette, eraser, undo, clear, save, draw_view
    private lateinit var paletteIB : ImageButton
    private lateinit var eraserIB : ImageButton
    private lateinit var undoIB : ImageButton
    private lateinit var clearIB : ImageButton
    private lateinit var saveIB : ImageButton
    private lateinit var bgMainScreen : ConstraintLayout
    private lateinit var drawView : DrawView
    private lateinit var slider : Slider

    private var defaultStrokeWidth : Float = 8f
    private var defaultColor = Color.parseColor("#ffffffff")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setLayouts()
        drawingButtonsSetup()

        undoIB.setOnClickListener {
            drawView.undo()
        }

        clearIB.setOnClickListener {
            drawView.clearCanvas()
        }

        eraserIB.setOnClickListener {
            drawView.setColor(Color.rgb(255, 255, 255))
            slider.addOnChangeListener(object : Slider.OnChangeListener {
                override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                    drawView.setStrokeWidth(value)
                }
            })
        }

        saveIB.setOnClickListener {
            saveMediaToStorage(drawView.getBitmap())
        }

        slider.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
                drawView.setStrokeWidth(value)
            }
        })
    }

    private fun setLayouts() {
        paletteIB = findViewById(R.id.palette)
        eraserIB = findViewById(R.id.eraser)
        undoIB = findViewById(R.id.undo)
        clearIB = findViewById(R.id.clear)
        saveIB = findViewById(R.id.save)
        bgMainScreen = findViewById(R.id.bg_mainScreen)
        drawView = findViewById(R.id.dv_drawView)
        slider = findViewById(R.id.sl_slider)
    }

    private fun drawingButtonsSetup() {
        paletteIB.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                openColorPicker()
            }
        })

    }

    private fun openColorPicker() {
        val ambilWarnaDialog = AmbilWarnaDialog(this, defaultColor, object:
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                drawView.setColor(defaultColor)
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                defaultColor = color
                drawView.setStrokeWidth(defaultStrokeWidth)
                drawView.setColor(defaultColor)
            }
        })
        ambilWarnaDialog.show()
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            this.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()

        }
    }
}

