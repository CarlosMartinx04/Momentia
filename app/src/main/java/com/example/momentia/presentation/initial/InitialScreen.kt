package com.example.momentia.presentation.initial

import android.icu.text.CaseMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentia.R
import com.example.momentia.ui.theme.Azul
import com.example.momentia.ui.theme.Blanco
import com.example.momentia.ui.theme.GrisClaro
import com.example.momentia.ui.theme.Negro
import com.example.momentia.ui.theme.Purple40
import com.google.firebase.annotations.concurrent.Background

@Preview
@Composable
fun InitialScreen( navigateToLogin:() -> Unit={}, navigateToSignUp:() -> Unit={}){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            modifier = Modifier.clip(CircleShape)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Descubre lugares",
            color = Color.Black,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Unete a ellos",
            color = Color.Black,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                navigateToSignUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Azul)
        ) {
            Text(text = "Registrate", color = Blanco, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomButton(Modifier.clickable { }, painterResource(id = R.drawable.cromo), " Continua con Google")
        Spacer(modifier = Modifier.height(8.dp))
        CustomButton(Modifier.clickable { }, painterResource(id = R.drawable.facebook), "Continua con Facebook")
        Text(
            text = "Inciar Sesion",
            color = Negro,
            modifier = Modifier
                .padding(24.dp)
                .clickable { navigateToLogin() },
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun CustomButton(modifier: Modifier, painter: Painter, title: String){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 32.dp)
            .background(Blanco)
            .border(2.dp, GrisClaro, CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 16.dp)
                .size(16.dp)
        )
        Text(
            text = title,
            color = Negro,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}