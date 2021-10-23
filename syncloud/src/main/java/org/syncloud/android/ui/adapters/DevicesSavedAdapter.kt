package org.syncloud.android.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.syncloud.android.R
import org.syncloud.android.core.platform.model.DomainModel
import org.syncloud.android.ui.DevicesSavedActivity

class DevicesSavedAdapter(private val activity: DevicesSavedActivity) : ArrayAdapter<DomainModel>(
    activity, R.layout.layout_device_item
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = activity.layoutInflater
        val rowView = inflater.inflate(R.layout.layout_device_item, null)
        val txtBoldTitle = rowView.findViewById<TextView>(R.id.txt_bold_title)
        val txtAdditionalLine = rowView.findViewById<TextView>(R.id.txt_additional_line)
        val domain = getItem(position)!!
        val fullDomainName = domain.name
        txtBoldTitle.text = fullDomainName
        txtAdditionalLine.text = domain.title
        return rowView
    }
}