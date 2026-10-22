package com.fireemu.ui.screens.splash
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*
@Composable
fun SplashScreen(viewModel:SplashViewModel,onNavigate:(String)->Unit) {
    LaunchedEffect(viewModel) {viewModel.navigateToRoute.collect { if(it!=null) onNavigate(it) }}
    Box(Modifier.fillMaxSize().etchedBackdrop(),contentAlignment=Alignment.Center) {
        Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
            FireSpotIcon("brand",Modifier.size(48.dp))
            Text("firespot",fontSize=38.sp,fontWeight=FontWeight.Light,color=FireSpotTextPrimary)
            Text("A LITTLE NOSTALGIA.",fontSize=10.sp,letterSpacing=2.sp,color=FireSpotTextSecondary)
        }
    }
}
