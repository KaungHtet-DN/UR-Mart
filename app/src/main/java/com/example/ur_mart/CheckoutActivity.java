package com.example.ur_mart;

import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    TextView checkOutAmount, checkOutHST, checkOutTotalAmount;
    EditText checkoutCardNumber, checkoutCardHolderName, checkoutExpiryDate, checkoutCVV, checkoutPersonName, checkoutPhoneNumber, checkoutEmail, checkoutAddress, checkoutPostalCode;
    Button checkOutClearAll, checkOutBackToShop, checkOutMakePayment;
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    final double HST_RATE = 0.13; // 13% HST tax
    double taxAmount = 0.00;
    AwesomeValidation mAwesomeValidation;
    String PhoneNumber_Regex = "^(\\+1[-\\s]?|1[-\\s]?|\\()?(\\d{3})(\\)|[-\\s])?(\\d{3})[-\\s]?(\\d{4})$";
    String PostalCode_Regex = "^[A-Za-z]\\d[A-Za-z] ?\\d[A-Za-z]\\d$";
    String CreditCard_Number_Regex = "^(?:4[0-9]{12}(?:[0-9]{3})?|" +  // Visa
            "5[1-5][0-9]{14}|" +              // MasterCard
            "3[47][0-9]{13}|" +               // American Express
            "6(?:011|5[0-9]{2})[0-9]{12}|" +  // Discover
            "(?:2131|1800|35\\d{3})\\d{11})$";

    String CreditCard_Expiry_Regex = "^(0[1-9]|1[0-2])\\/\\d{2}$";
    String CreditCard_CVV_Regex = "^\\d{3}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mAwesomeValidation = new AwesomeValidation(BASIC);

        checkOutAmount = findViewById(R.id.checkOutAmount);
        checkOutHST = findViewById(R.id.checkOutHST);
        checkOutTotalAmount = findViewById(R.id.checkOutTotalAmount);
        checkoutCardNumber = findViewById(R.id.checkoutCardNumber);
        checkoutCardHolderName = findViewById(R.id.checkoutCardHolderName);
        checkoutExpiryDate = findViewById(R.id.checkoutExpiryDate);
        checkoutCVV = findViewById(R.id.checkoutCVV);
        checkoutPersonName = findViewById(R.id.checkoutPersonName);
        checkoutPhoneNumber = findViewById(R.id.checkoutPhoneNumber);
        checkoutEmail = findViewById(R.id.checkoutEmail);
        checkoutAddress = findViewById(R.id.checkoutAddress);
        checkoutPostalCode = findViewById(R.id.checkoutPostalCode);
        checkOutClearAll = findViewById(R.id.checkOutClearAll);
        checkOutBackToShop = findViewById(R.id.checkOutBackToShop);
        checkOutMakePayment = findViewById(R.id.checkOutMakePayment);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            double amount = Double.parseDouble(bundle.getString("Amount"));

            checkOutAmount.setText(String.format("%.2f",amount));
            checkOutHST.setText(String.format("%.2f", calculateTaxAmount(amount)));
            checkOutTotalAmount.setText(String.format("%.2f", calculateTotalAmountWithTax(amount)));
        }

        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutPersonName, RegexTemplate.NOT_EMPTY, R.string.err_name);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutPhoneNumber, PhoneNumber_Regex, R.string.err_phoneno);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutEmail, android.util.Patterns.EMAIL_ADDRESS, R.string.err_email);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutAddress, RegexTemplate.NOT_EMPTY, R.string.err_address);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutPostalCode, PostalCode_Regex , R.string.err_postalcode);

        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutCardNumber, CreditCard_Number_Regex, R.string.err_creditcard_number);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutCardHolderName, RegexTemplate.NOT_EMPTY, R.string.err_creditcard_holder);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutExpiryDate, CreditCard_Expiry_Regex, R.string.err_creditcard_expirydate);
        mAwesomeValidation.addValidation(CheckoutActivity.this, R.id.checkoutCVV, CreditCard_CVV_Regex, R.string.err_creditcard_securitynumber);

        checkOutClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        checkOutBackToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckoutActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        checkOutMakePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mAwesomeValidation.validate()) {
                    clearAllItemsFromCart();
                }
            }
        });
    }

    private double calculateTaxAmount(double amount){
        taxAmount = amount * HST_RATE;
        return taxAmount;
    }

    private double calculateTotalAmountWithTax(double amount){
        double totalAmount = amount + taxAmount;
        return totalAmount;
    }

    private void clearAllItemsFromCart(){
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("shopping_cart")
                .child(currentUser.getUid());

        // Remove the cart by userId
        cartRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CheckoutActivity.this, "Successfully make payment.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CheckoutActivity.this, ThankYouActivity.class);
                        intent.putExtra("Username", checkoutPersonName.getText());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CheckoutActivity.this, "Error occurred while making payment.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}