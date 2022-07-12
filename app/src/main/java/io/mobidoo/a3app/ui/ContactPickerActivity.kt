package io.mobidoo.a3app.ui

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import io.mobidoo.a3app.R
import io.mobidoo.a3app.adapters.ContactCursorAdapter
import io.mobidoo.a3app.databinding.ActivityContactPickerBinding

@SuppressLint("InlinedApi")
private val FROM_COLUMNS: Array<String> = arrayOf(
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
)
private val TO_IDS: IntArray = intArrayOf(android.R.id.text1)

@SuppressLint("InlinedApi")
private val PROJECTION: Array<out String> = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.LOOKUP_KEY,
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
)

@SuppressLint("InlinedApi")
private val DETAILS_PROJECTION: Array<out String> = arrayOf(
    ContactsContract.CommonDataKinds.Phone.NUMBER,
    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
)
// Defines the selection clause
private const val SELECTION: String = "${ContactsContract.Data.LOOKUP_KEY} = ?"

// Defines the array to hold the search criteria
private val selectionArgs: Array<String> = arrayOf("")
/*
 * Defines a variable to contain the selection value. Once you
 * have the Cursor from the Contacts table, and you've selected
 * the desired row, move the row's LOOKUP_KEY value into this
 * variable.
 */
private var lookupKey: String? = null
/*
 * Defines a string that specifies a sort order of MIME type
 */
private const val SORT_ORDER = ContactsContract.Data.MIMETYPE

// The column index for the _ID column
private const val CONTACT_ID_INDEX: Int = 0
// The column index for the CONTACT_KEY column
private const val CONTACT_KEY_INDEX: Int = 1

class ContactPickerActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    // Define global mutable variables
    // Define a ListView object
    lateinit var contactsList: ListView
    // Define variables for the contact the user selects
    // The contact's _ID value
    var contactId: Long = 0
    // The contact's LOOKUP_KEY
    var contactKey: String? = null
    // A content URI for the selected contact
    var contactUri: Uri? = null
    // An adapter that binds the result Cursor to the ListView
    private var cursorAdapter: ContactCursorAdapter? = null

    private lateinit var binding: ActivityContactPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LoaderManager.getInstance(this).initLoader(0, null, this)

        contactsList = binding.lvContactList
        // Gets a CursorAdapter
        cursorAdapter = ContactCursorAdapter(this, R.layout.item_phone_contact, FROM_COLUMNS, TO_IDS)        // Sets the adapter for the ListView
        contactsList.adapter = cursorAdapter
        contactsList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        contactsList.onItemClickListener = this
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(
            this,
            ContactsContract.Contacts.CONTENT_URI,
            PROJECTION,
            "${ContactsContract.Contacts.HAS_PHONE_NUMBER} =1",
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        cursorAdapter?.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }
}




















