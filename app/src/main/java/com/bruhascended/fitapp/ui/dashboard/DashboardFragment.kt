package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentDashboardBinding
import com.bruhascended.fitapp.ui.dashboard.components.AnimatedCircle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        view.apply {
            setContent {
                var value by remember {
                    mutableStateOf(45f)
                }
                CoroutineScope(IO).launch {
                    delay(5000)
                    value = 100f
                    Log.d("juju",value.toString())
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedCircle(value)
                }
            }
        }
        return view
    }

}
