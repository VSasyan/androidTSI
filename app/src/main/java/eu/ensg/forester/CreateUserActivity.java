package eu.ensg.forester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import eu.ensg.forester.DAO.ForesterDAO;
import eu.ensg.forester.POJO.ForesterPOJO;
import eu.ensg.spatialite.SpatialiteDatabase;
import eu.ensg.spatialite.SpatialiteOpenHelper;

public class CreateUserActivity extends AppCompatActivity {

    protected SpatialiteDatabase database;
    protected EditText name, lastName, serialNumber;
    protected Button createUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        // Init DB
        initDatabase();

        // Load GUI
        name = (EditText)findViewById(R.id.name);
        lastName = (EditText)findViewById(R.id.lastName);
        serialNumber = (EditText)findViewById(R.id.serialNumber);
        createUser = (Button)findViewById(R.id.createUser);

        // Listener: createUSer
        createUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Add in database
                String str_name = name.getText().toString();
                String str_lastName = lastName.getText().toString();
                String str_serial = serialNumber.getText().toString();
                ForesterPOJO forester = new ForesterPOJO(0, str_name, str_lastName, str_serial);
                ForesterDAO dao = new ForesterDAO(database);
                forester = dao.create(forester);

                if (forester != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(getString(R.string.serialNumber), serialNumber.getText().toString());

                    Toast.makeText(CreateUserActivity.this, forester.toString(), Toast.LENGTH_SHORT).show();

                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(CreateUserActivity.this, getString(R.string.createUserFail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initDatabase() {
        try {
            SpatialiteOpenHelper helper = new ForesterSpatialiteOpenHelper(this);
            database = helper.getDatabase();
        } catch (jsqlite.Exception | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.cannot_init_database), Toast.LENGTH_LONG).show();
            System.exit(0);
        }
    }

}
