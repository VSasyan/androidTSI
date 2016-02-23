package eu.ensg.forester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateUserActivity extends AppCompatActivity {

    protected EditText name, lastName, serialNumber;
    protected Button createUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Load GUI
        name = (EditText)findViewById(R.id.name);
        lastName = (EditText)findViewById(R.id.lastName);
        serialNumber = (EditText)findViewById(R.id.serialNumber);
        createUser = (Button)findViewById(R.id.createUser);

        // Listener: createUSer
        createUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(getString(R.string.serialNumber), serialNumber.getText().toString());

                CharSequence text = name.getText() + " " + lastName.getText() + " " + serialNumber.getText();
                Toast.makeText(CreateUserActivity.this, text, Toast.LENGTH_SHORT).show();

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

}
