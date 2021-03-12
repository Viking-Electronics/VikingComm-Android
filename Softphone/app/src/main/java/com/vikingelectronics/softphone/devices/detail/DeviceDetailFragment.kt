package com.vikingelectronics.softphone.devices.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.activity.ActivityEntryCard
import com.vikingelectronics.softphone.devices.Device
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.coil.CoilImage

@AndroidEntryPoint
class DeviceDetailFragment: Fragment(R.layout.fragment_generic_compose) {

    private val args: DeviceDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                MaterialTheme {
                    DeviceDetailScreen(args.device, findNavController())
                }
            }
        }
    }
}

@Composable
fun DeviceDetailScreen(
    device: Device,
    navController: NavController
) {
    val viewModel: DeviceDetailViewModel = viewModel()
    viewModel.getActivityFeedForDevice(device)
    LazyColumn (
        modifier = Modifier
            .background(color = colorResource(id = R.color.light_grey_color))
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "Latest Activity:",
                    style = MaterialTheme.typography.h5
                )

                CoilImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    data = device.latestActivityEntry.snapshotUrl,
                    contentDescription = "Latest snapshot from ${device.name}",
                    contentScale = ContentScale.Inside,
                )

                Text(
                    text = device.latestActivityEntry.description,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = device.latestActivityEntry.timestamp.toDate().toString(),
                    style = MaterialTheme.typography.body1
                )

                val activityText = if (viewModel.activityList.isNotEmpty()) "Previous Activity" else "No Previous Activity"

                Text(
                    text = activityText,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

        }

        items(viewModel.activityList) { entry ->
            ActivityEntryCard(
                entry = entry,
                modifier = Modifier.clickable {
                    val directions = DeviceDetailFragmentDirections.actionDevicesDetailFragmentToActivityDetailFragment(entry.timestamp.toDate().toString(), entry)
                    navController.navigate(directions)
                }
            )
        }
    }
}