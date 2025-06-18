package ru.crmkurgan.main.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.Window
import androidx.appcompat.app.AlertDialog
import ru.crmkurgan.main.R
import ru.crmkurgan.main.items.Chat

class Functions {
    companion object {
        fun viewDialog(errorField: Int, context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.error)
            builder.setMessage(errorField)
            builder.setPositiveButton(R.string.Ok) { _, _ -> }
            builder.show()
        }

        fun viewDialog(text: String, context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(text)
            builder.setPositiveButton(R.string.Ok) { _, _ -> }
            builder.show()
        }

        fun getCircleBitmap(bitmap: Bitmap): Bitmap {
            val srcRect: Rect
            val dstRect: Rect
            val r: Float
            val paint = Paint()
            val width: Int = bitmap.width
            val height: Int = bitmap.height
            val widthToGenerate = 100F
            val heightToGenerate = 100F
            val borderWidth: Float = 1.toFloat()
            val output: Bitmap
            val canvas: Canvas
            if (width > height) {
                output = Bitmap.createBitmap(
                    widthToGenerate.toInt(),
                    heightToGenerate.toInt(),
                    Bitmap.Config.ARGB_8888
                )
                canvas = Canvas(output)
                val scale: Float = (widthToGenerate / width)
                val xTranslation = 0.0f
                val yTranslation: Float = (heightToGenerate - height * scale) / 2.0f
                val transformation = Matrix()
                transformation.postTranslate(xTranslation, yTranslation)
                transformation.preScale(scale, scale)
                val color: Int = Color.WHITE
                paint.isAntiAlias = true
                paint.color = color
                canvas.drawBitmap(bitmap, transformation, paint)
            } else {
                output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
                canvas = Canvas(output)
                val top: Int = (height - width) / 2
                val bottom: Int = top + width
                srcRect = Rect(0, top, width, bottom)
                dstRect = Rect(0, 0, width, width)
                r = (width / 2).toFloat()
                val color: Int = Color.GRAY
                paint.isAntiAlias = true
                canvas.drawARGB(0, 0, 0, 0)
                paint.color = color
                canvas.drawCircle(r + borderWidth, r + borderWidth, r + borderWidth, paint)
                canvas.drawCircle(r, r, r, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            }
            return output
        }

        fun getStatusBarHeight(resources:Resources,window:Window): Int {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
            else Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top
        }

        fun getItem(id:String, chatList:ArrayList<Chat>): Boolean {
            for(i in 0 until chatList.size){
                if(chatList[i].id==id) {
                    return true
                }
            }
            return false
        }
    }
}