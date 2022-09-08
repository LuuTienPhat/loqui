package com.example.loqui.activities.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loqui.BaseActivity;
import com.example.loqui.databinding.ActivityLegalPolicyBinding;

public class LegalPolicyActivity  extends BaseActivity {

    private ActivityLegalPolicyBinding binding;
    private static final String termOfServiceLink = "https://m.facebook.com/terms.php";
    private static final String privacyPolicyLink = "https://www.facebook.com/privacy/policy";
    private static final String cookiePolicyLink = "https://www.facebook.com/policy/cookies/";
    private static final String thirdPartiesNoticeLink = "https://portal.facebook.com/legal/third-party-notices-14/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLegalPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportActionBar().setElevation(0);

        setListeners();
    }

    private void setListeners() {
        binding.btnTermOfService.setOnClickListener(v -> {
            onBrowseClick(termOfServiceLink);
        });

        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            onBrowseClick(privacyPolicyLink);
        });

        binding.btnCookiesPolicy.setOnClickListener(v -> {
            onBrowseClick(cookiePolicyLink);
        });

        binding.btnThirdPartiesNotice.setOnClickListener(v -> {
            onBrowseClick(thirdPartiesNoticeLink);
        });
    }

    private void onBrowseClick(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(Intent.createChooser(intent, "Open with"));

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}