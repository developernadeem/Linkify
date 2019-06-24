package pk.edu.uaf.linkify;

import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        getSupportActionBar().setTitle("About Us");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView=findViewById(R.id.aboutUsText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textView.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
