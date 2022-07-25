package io.mobidoo.a3app.ui.bottomsheet

import android.app.WallpaperManager
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

class SelectWallpaperDestinationDialog(
    private val onDestClicked: (Int)->Unit
) : BottomSheetDialogFragment() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var btnOk: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_select_wallapper_destination, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        behavior = BottomSheetBehavior.from(view.parent as View)
        view.findViewById<Button>(R.id.btnSetToSystemScreen).setOnClickListener {
            onDestClicked(WallpaperManager.FLAG_SYSTEM)
        }
        view.findViewById<Button>(R.id.btnSetToLockScreen).setOnClickListener {
            onDestClicked(WallpaperManager.FLAG_LOCK)
        }
        view.findViewById<Button>(R.id.btnSetToBothScreen).setOnClickListener {
            onDestClicked(0)
        }
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