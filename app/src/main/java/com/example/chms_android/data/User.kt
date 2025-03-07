import androidx.annotation.Keep
import com.example.chms_android.data.Role
import java.io.Serializable
import java.time.LocalDateTime

data class User(
    val userId: Int? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val phone: Long,
    val role: Role,
    val age: Int,
    val gender: Int,
    val height: Int,
    val weight: Int,
    val isVerified: Boolean,
    val loginTime: LocalDateTime? = null
) : Serializable {

}
