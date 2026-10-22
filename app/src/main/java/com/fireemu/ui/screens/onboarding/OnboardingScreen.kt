package com.fireemu.ui.screens.onboarding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.fireemu.ui.components.*
import com.fireemu.ui.theme.*
@Composable
fun OnboardingScreen(viewModel:OnboardingViewModel,onFinish:()->Unit) {
    val focus=remember {FocusRequester()}
    Box(Modifier.fillMaxSize().etchedBackdrop().padding(48.dp),contentAlignment=Alignment.Center) {
        Column(Modifier.width(540.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            FireSpotIcon("brand",Modifier.size(38.dp))
            Text("Your next little escape.",fontSize=36.sp,fontWeight=FontWeight.Light,color=FireSpotTextPrimary)
            Text("Two original Game Boy games are already in your library. No ROM upload, account or BIOS is needed to start playing.",fontSize=16.sp,lineHeight=24.sp,color=FireSpotTextSecondary)
            Row(horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                Text("D-PAD  Navigate",color=FireSpotTextPrimary,fontSize=12.sp)
                Text("SELECT  Confirm",color=FireSpotTextPrimary,fontSize=12.sp)
                Text("MENU  Pause",color=FireSpotTextPrimary,fontSize=12.sp)
            }
            TvButton("Open my library",{viewModel.completeOnboarding(onFinish)},Modifier.focusRequester(focus),isPrimary=true)
            Text("ORIGINAL HOMEBREW  /  LOCAL SAVES  /  OFFLINE PLAY",fontSize=10.sp,letterSpacing=1.5.sp,color=FireSpotTextSubtitle)
        }
    }
    LaunchedEffect(Unit) {focus.requestFocus()}
}
