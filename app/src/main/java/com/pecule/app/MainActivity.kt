package com.pecule.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.data.repository.IUserPreferencesRepository
import com.pecule.app.ui.navigation.BottomNavBar
import com.pecule.app.ui.navigation.PeculeNavHost
import com.pecule.app.ui.navigation.Routes
import com.pecule.app.ui.screens.onboarding.OnboardingDialog
import com.pecule.app.ui.screens.onboarding.OnboardingViewModel
import com.pecule.app.ui.theme.PeculeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: IUserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreference by userPreferencesRepository.userPreferences
                .map { it.theme }
                .collectAsState(initial = ThemePreference.AUTO)

            PeculeTheme(themePreference = themePreference) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                val isFirstLaunch by onboardingViewModel.isFirstLaunch.collectAsState()

                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val showBottomBar = currentRoute != Routes.Profile.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    PeculeNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (isFirstLaunch) {
                    OnboardingDialog(
                        onComplete = { firstName, amount, date ->
                            onboardingViewModel.completeOnboarding(firstName, amount, date)
                        }
                    )
                }
            }
        }
    }
}
