package io.mobidoo.a3app.ui.bottomsheet

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import io.mobidoo.a3app.R

class AllowChangeSettingsPermissions(
    private val allowClicked: ()->Unit
) : BottomSheetDialogFragment() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var btnOk: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.dialog_allow_settings_permissions, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        behavior = BottomSheetBehavior.from(view.parent as View)
        view.findViewById<Button>(R.id.btnAllowSettingPermission).setOnClickListener {

            allowClicked()
            dismiss()
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