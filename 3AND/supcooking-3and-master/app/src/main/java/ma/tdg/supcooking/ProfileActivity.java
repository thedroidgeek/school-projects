package ma.tdg.supcooking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import ma.tdg.supcooking.model.User;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (User.isLoggedIn()) {
            User profile = User.getProfile();
            TextView firstName = findViewById(R.id.profile_first_name);
            TextView lastName = findViewById(R.id.profile_last_name);
            TextView username = findViewById(R.id.profile_username);
            TextView email = findViewById(R.id.profile_email);
            TextView address = findViewById(R.id.profile_address);
            TextView phoneNumber = findViewById(R.id.profile_phone_number);
            firstName.setText(profile.getFirstName());
            lastName.setText(profile.getLastName());
            username.setText(profile.getUsername());
            email.setText(profile.getEmail());
            address.setText(profile.getAddress());
            phoneNumber.setText(profile.getPhoneNumber());
        }
        Toast.makeText(getApplicationContext(), "Profile settings are currently not implemented in this version, please check back later!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
