package self.chera.spacecrew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import self.chera.spacecrew.ui.theme.Spacecrew2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Spacecrew2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeviceScan(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceScan(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "device list",
            modifier = modifier.weight(1.0F)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "0 device(s) found",
                modifier = modifier.weight(1.0F)
            )
            Button(onClick = { /*TODO*/ }) {
                Text(
                    text = "Scan",
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun Main(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "connected to (0) devices",
            modifier = modifier.weight(1.0F)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(top = 16.dp)
        ) {
            Spacer(modifier = modifier.weight(1.0F))
            Button(onClick = { /*TODO*/ }) {
                Text(
                    text = "Scan for devices",
                    modifier = modifier
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 360, widthDp = 360)
@Composable
fun GreetingPreview() {
    Spacecrew2Theme {
        Main()
    }
}