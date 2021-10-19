package org.syncloud.android.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.syncloud.android.R
import org.syncloud.android.core.platform.model.IdentifiedEndpoint
import org.syncloud.android.ui.DevicesDiscoveryActivity

class DevicesDiscoveredAdapter(private val activity: DevicesDiscoveryActivity) :
    ArrayAdapter<IdentifiedEndpoint>(
        activity, R.layout.layout_device_item
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = activity.layoutInflater
        val rowView = inflater.inflate(R.layout.layout_device_item, null)
        val txtBoldTitle = rowView.findViewById<View>(R.id.txt_bold_title) as TextView
        val txtAdditionalLine = rowView.findViewById<View>(R.id.txt_additional_line) as TextView
        val ie = getItem(position)!!
        txtBoldTitle.text = ie.id.title
        txtAdditionalLine.text = ie.endpoint.host
        return rowView
    }
}