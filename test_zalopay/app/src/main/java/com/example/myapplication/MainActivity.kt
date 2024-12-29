package com.example.myapplication

import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.Api.CreateOrder
import org.json.JSONObject
import android.content.Intent
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import vn.zalopay.sdk.ZaloPayError

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cấu hình StrictMode
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Khởi tạo ZaloPay SDK
        ZaloPaySDK.init(2553, Environment.SANDBOX)

        setContent {
            PaymentScreen()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}

@Composable
fun PaymentScreen() {
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // TextField để nhập số tiền
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Nhập số tiền") },
            placeholder = { Text("VND") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nút thanh toán
        Button(
            onClick = {
                if (amount.text.isNotEmpty()) {
                    isProcessing = true

                    // Gọi API CreateOrder
                    val orderApi = CreateOrder()

                    try {
                        // Tạo đơn hàng và lấy dữ liệu trả về
                        val data = orderApi.createOrder(amount.text)
                        Log.d("Amount", amount.text)

                        // Hiển thị thông tin trả về từ API
                        val code = data.getString("return_code")
                        Toast.makeText(context, "return_code: $code", Toast.LENGTH_LONG).show()

                        // Kiểm tra mã trả về từ API
                        if (code == "1") {

                            var token = data.getString("zp_trans_token")  // Lưu token vào biến
                            Log.d("Token", token)

                            // Gọi hàm thanh toán
                            ZaloPaySDK.getInstance().payOrder(
                                context as MainActivity,  // `MainActivity` thay vì `MainActivity.this` trong Java
                                token,
                                "demozpdk://app",  // Scheme URL
                                object : PayOrderListener {
                                    override fun onPaymentSucceeded(result: String?, message: String?, zpTransToken: String?) {
                                        // Xử lý khi thanh toán thành công
                                        Log.d("Payment", "Thanh toán thành công: $result")
                                        Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onPaymentCanceled(p0: String?, p1: String?) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onPaymentError(
                                        p0: ZaloPayError?,
                                        p1: String?,
                                        p2: String?
                                    ) {
                                        TODO("Not yet implemented")
                                    }

                                    fun onPaymentFailed(error: ZaloPayError?, message: String?, description: String?) {
                                        // Xử lý khi thanh toán thất bại
                                        Log.d("Payment", "Thanh toán thất bại: $message, $description")
                                        Toast.makeText(context, "Thanh toán thất bại: $message", Toast.LENGTH_SHORT).show()
                                    }

                                    fun onPaymentCanceled() {
                                        // Xử lý khi người dùng hủy thanh toán
                                        Log.d("Payment", "Thanh toán bị hủy")
                                        Toast.makeText(context, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )


                            IsDone()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Lỗi khi thanh toán", Toast.LENGTH_SHORT).show()
                    }

                    isProcessing = false
                } else {
                    Toast.makeText(context, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text(text = if (isProcessing) "Đang xử lý..." else "Thanh toán")
        }
    }
}

// Giả sử IsDone là một hàm mà bạn muốn gọi sau khi thanh toán hoàn tất
fun IsDone() {
    // Thực hiện hành động sau khi thanh toán hoàn tất, ví dụ: chuyển màn hình, hiển thị thông báo, v.v.
    Log.d("Payment", "Thanh toán hoàn tất")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PaymentScreen()
}
