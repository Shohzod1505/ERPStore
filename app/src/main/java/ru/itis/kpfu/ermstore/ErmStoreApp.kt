package ru.itis.kpfu.ermstore

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.itis.kpfu.ermstore.data.demo.DemoDataSeeder
import javax.inject.Inject

@HiltAndroidApp
class ErmStoreApp : Application() {
	@Inject
	lateinit var demoDataSeeder: DemoDataSeeder

	override fun onCreate() {
		super.onCreate()
		CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
			demoDataSeeder.seedIfEmpty()
		}
	}
}
