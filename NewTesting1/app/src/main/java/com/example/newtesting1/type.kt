import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.newtesting1.R

val customFont = FontFamily(
    Font(R.font.philosopher_bold, weight = FontWeight.Normal),
    Font(R.font.philosopher_italic, weight = FontWeight.Light),
    Font(R.font.philosopher_regular, weight = FontWeight.ExtraLight),
)