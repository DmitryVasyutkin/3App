package io.mobidoo.a3app.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cursoradapter.widget.SimpleCursorAdapter
import coil.load
import io.mobidoo.a3app.R

class ContactCursorAdapter(
    private val context: Context,
    private val layout: Int,
    private val from: Array<String>,
    private val to: IntArray
) : SimpleCursorAdapter(context, layout, null, from, to) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(layout, null)
    }

    @SuppressLint("Range")
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        super.bindView(view, context, cursor)
        val tvName = view?.findViewById<TextView>(R.id.tv_contact_name)!!
        val tvNameAbc = view?.findViewById<TextView>(R.id.tv_contact_name_abc)!!
        val iv = view?.findViewById<ImageView>(R.id.iv_contact_image)!!
        var name = ""
        var family = ""
        var photoId = ""
        cursor?.use{
            while (it.moveToNext()){
                val mime = it.getString(0)
                if (mime == "vnd.android.cursor.item/name"){
                    name = it.getString(4)
                    family = it.getString(2)
                }else{
                    Log.i("Parser", "mime $mime")
                }

                photoId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
            }
            it.close()
        }

        val person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, photoId.toLong())
        //CONTENT_DIRECTORY
        val photoUriString = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)

        tvName.text = name + " " + family
        if (photoId.isEmpty()){
            tvNameAbc.visibility = View.VISIBLE
            tvNameAbc.text = name.subSequence(0,1)[0].toString()
        }else{
            tvNameAbc.visibility = View.GONE
            iv.load(photoUriString)
        }
    }
}