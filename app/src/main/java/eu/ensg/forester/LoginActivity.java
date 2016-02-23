package eu.ensg.forester;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    static final int SERIAL_NUMBER_REQUEST = 1;

    protected EditText serialNumber;
    protected Button login, createUser;
    protected SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init preferences
        preferences = getSharedPreferences("formPreferences", Context.MODE_PRIVATE);

        // Load GUI
        serialNumber = (EditText)findViewById(R.id.serialNumber);
        createUser = (Button)findViewById(R.id.createUser);
        login = (Button)findViewById(R.id.login);

        // Load preferences
        serialNumber.setText(preferences.getString("str_serialNumber", ""));

        // Listener: createUSer
        createUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(LoginActivity.this, CreateUserActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, SERIAL_NUMBER_REQUEST);
            }
        });

        // Listener: login
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Load Form
                String str_serialNumber = serialNumber.getText().toString();

                // Save preferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("str_serialNumber", str_serialNumber);
                editor.apply();

                // Intent
                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                intent.putExtra(getString(R.string.serialNumber), str_serialNumber);
                startActivity(intent);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SERIAL_NUMBER_REQUEST) {
            if (resultCode == RESULT_OK) {
                serialNumber.setText(data.getStringExtra(getString(R.string.serialNumber)));
            }
        }
    }
}
