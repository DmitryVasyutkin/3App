package io.mobidoo.a3app.ui.bottomsheet

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import io.mobidoo.a3app.R
import io.mobidoo.a3app.utils.AppUtils.createFullLink
import io.mobidoo.domain.entities.ringtone.Ringtone

class RingtoneDownloadedDialog(
    private val allowClicked: ()->Unit,
    private val ringTone: Ringtone,
    private val path: String
) : BottomSheetDialogFragment() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var btnOk: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_ringtone_downloaded, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        behavior = BottomSheetBehavior.from(view.parent as View)
        view.findViewById<Button>(R.id.btnListenRingtone).setOnClickListener {
            allowClicked()
        }
        view.findViewById<ImageButton>(R.id.ib_close_dialog_ring_loaded).setOnClickListener {
            dismiss()
        }
        view.findViewById<TextView>(R.id.tv_ringtone_downloaded_title1).text = ringTone.title
        view.findViewById<TextView>(R.id.tv_ringtone_downloaded_title2).text = ringTone.url.split("/").last().split(".").last()
        view.findViewById<ImageView>(R.id.iv_ringtone_downloaded).load(createFullLink(ringTone.imageUrl))
        view.findViewById<TextView>(R.id.tv_ringtone_downloaded_to).text = path
    }

    override fun onStart() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        super.onStart()
    }

    private fun getScreenHeight(): Int{
        return Resources.getSystem().displayMetrics.heightPixels
    }

    override fun getTheme(): Int {
        return R.style.CustomBottomSheetDialog
    }

    override fun onDestroyView() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        super.onDestroyView()
    }
}