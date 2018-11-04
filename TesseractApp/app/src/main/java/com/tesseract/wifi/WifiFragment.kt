package com.tesseract.wifi

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tesseract.R
import com.tesseract.wifi.WifiListAdapter.OnWifiItemClickListener

class WifiFragment : Fragment(), OnWifiItemClickListener {

	private fun updateWifiList(wifiList: ArrayList<Wifi>) {
		activity!!.runOnUiThread {
			wifiListAdapter.updateList(wifiList)
		}
	}

	override fun onItemClick(item: Wifi) {
		val wifiSelected = Bundle()
		wifiSelected.putSerializable("wifi", item)
		val fragment = WifiConnect()
		val transaction: FragmentTransaction = fragmentManager!!.beginTransaction()
		fragment.arguments = wifiSelected
		transaction.replace(R.id.home_view_frame, fragment)
		transaction.addToBackStack(null)
		transaction.commit()
	}

	private val clickListener: OnWifiItemClickListener = this
	private lateinit var wifiListAdapter: WifiListAdapter

	private lateinit var wifiController: WifiController

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view: View = inflater.inflate(R.layout.fragment_wifi, container, false)

		wifiController = activity?.run { ViewModelProviders.of(this).get(WifiController::class.java) }!!

		val recyclerViewWifi = view.findViewById<RecyclerView>(R.id.recyclerViewListWifi)

		wifiListAdapter = WifiListAdapter(wifiController.wifiList.value as ArrayList<Wifi>, clickListener)
		recyclerViewWifi.adapter = wifiListAdapter

		wifiController.wifiList.observe(activity!!, Observer<List<Wifi>> { wifiList ->
			updateWifiList(wifiList as ArrayList<Wifi>)
		})
		wifiController.requestAvailableWifi()

		val layoutManager = LinearLayoutManager(this.context)
		recyclerViewWifi.layoutManager = layoutManager

		recyclerViewWifi.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))

		return view
	}


}