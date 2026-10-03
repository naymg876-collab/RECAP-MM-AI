package com.recapmm.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.content.Intent;
import android.media.MediaPlayer;
import android.text.InputType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.google.firebase.auth.*;
import com.google.firebase.FirebaseException;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;

public class MainActivity extends Activity {

    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private String verificationId;

    private EditText phoneNumberInput;
    private EditText otpInput;
    private EditText textInput;
    private Spinner countrySpinner;
    private Spinner voiceSpinner;
    private String selectedVoice = "thiha";
    private int voiceSpeed = 100;
    private int voicePitch = 0;
    private int voiceVolume = 100;
    private Spinner styleSpinner;
    private Button speakButton;
    private MediaPlayer mediaPlayer;

    private final int BG = Color.rgb(5, 4, 14);
    private final int CARD = Color.rgb(16, 13, 28);
    private final int CARD2 = Color.rgb(25, 17, 38);
    private final int PINK = Color.rgb(255, 20, 165);
    private final int PURPLE = Color.rgb(145, 25, 255);
    private final int SOFT = Color.rgb(210, 185, 225);

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        if (firebaseAuth.getCurrentUser() != null) {
            showVoiceScreen();
        } else {
            showLoginScreen();
        }
    }

    private TextView text(String value, float size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private GradientDrawable gradient(int[] colors, float radius) {
        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private Button actionButton(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), 0, dp(8), 0);
        b.setBackground(bg(Color.rgb(35, 25, 48), 22));
        return b;
    }

    private LinearLayout pageRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        return root;
    }

    private ScrollView scroll(LinearLayout content) {
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setClipToPadding(false);
        s.addView(content);
        return s;
    }

    private TextView gradientTitle(String first, String second) {
        TextView t = text(first + " " + second, 25);
        t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private LinearLayout header(String section, boolean credits) {
        LinearLayout h = new LinearLayout(this);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(4), dp(4), dp(4), dp(2));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);

        TextView title = text("RECAP MM AI", 24);
        title.setTypeface(null, Typeface.BOLD);
        brand.addView(title);

        TextView sub = text("AI TRANSLATE • RECAP • VOICE", 10);
        sub.setTextColor(Color.rgb(205, 150, 220));
        brand.addView(sub);

        h.addView(
                brand,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        if (credits) {

            TextView credit = text("🪙 100+", 11);
            credit.setGravity(Gravity.CENTER);
            credit.setTextColor(Color.WHITE);
            credit.setTypeface(null, Typeface.BOLD);

            GradientDrawable creditBg =
                    new GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT,
                            new int[]{
                                    Color.rgb(45, 15, 55),
                                    Color.rgb(25, 12, 35)
                            });

            creditBg.setCornerRadius(dp(16));
            creditBg.setStroke(
                    dp(1),
                    Color.rgb(255, 45, 150)
            );

            credit.setBackground(creditBg);
            credit.setPadding(
                    dp(8),
                    dp(4),
                    dp(8),
                    dp(4)
            );

            h.addView(
                    credit,
                    new LinearLayout.LayoutParams(
                            dp(70),
                            dp(36)
                    )
            );

            TextView crown = text("♛", 24);
            crown.setGravity(Gravity.CENTER);
            crown.setTextColor(
                    Color.rgb(255, 205, 35)
            );

            h.addView(
                    crown,
                    new LinearLayout.LayoutParams(
                            dp(42),
                            dp(46)
                    )
            );
        }

        return h;
    }

    private LinearLayout makeNav(int selected) {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(6), dp(6), dp(6), dp(6));

        GradientDrawable navBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.rgb(35, 23, 48),
                        Color.rgb(12, 8, 20)
                });
        navBg.setCornerRadius(dp(28));
        navBg.setStroke(dp(1), Color.rgb(70, 42, 85));
        nav.setBackground(navBg);
        nav.setElevation(dp(12));

        String[] icons = {"🎙", "▶", "●"};
        String[] labels = {"VOICE", "TRANSLATE VIDEO", "PROFILE"};

        for (int i = 0; i < 3; i++) {
            final int index = i;

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(3), dp(5), dp(3), dp(5));

            if (i == selected) {
                GradientDrawable selectedBg =
                        new GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{
                                        Color.rgb(255, 55, 190),
                                        Color.rgb(205, 25, 190),
                                        Color.rgb(125, 20, 150)
                                });
                selectedBg.setCornerRadius(dp(24));
                selectedBg.setStroke(dp(2), Color.rgb(255, 135, 225));
                item.setBackground(selectedBg);
                item.setElevation(dp(14));
            } else {
                GradientDrawable normal =
                        new GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{
                                        Color.rgb(38, 25, 48),
                                        Color.rgb(17, 11, 25)
                                });
                normal.setCornerRadius(dp(23));
                normal.setStroke(dp(1), Color.rgb(55, 35, 70));
                item.setBackground(normal);
                item.setElevation(dp(4));
            }

            TextView icon = text(icons[i], i == selected ? 25 : 23);
            icon.setGravity(Gravity.CENTER);
            item.addView(icon, new LinearLayout.LayoutParams(-1, dp(34)));

            TextView label = text(labels[i], i == 1 ? 8 : 9);
            label.setGravity(Gravity.CENTER);
            label.setTypeface(null, Typeface.BOLD);
            label.setTextColor(i == selected ? Color.WHITE : Color.rgb(195, 180, 205));
            item.addView(label, new LinearLayout.LayoutParams(-1, dp(24)));

            item.setOnClickListener(v -> {
                if (index == 0) showVoiceScreen();
                else if (index == 1) showTranslateVideoScreen();
                else showProfileScreen();
            });

            nav.addView(item, new LinearLayout.LayoutParams(0, dp(70), 1));
        }

        return nav;
    }

    private void showLoginScreen() {
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.setBackgroundColor(BG);

        LinearLayout root = pageRoot();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.recap_mm_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        root.addView(logo, new LinearLayout.LayoutParams(dp(105), dp(105)));

        TextView title = text("RECAP MM AI", 28);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(46)));

        TextView sub = text("AI VIDEO RECAP • VOICE", 12);
        sub.setGravity(Gravity.CENTER);
        sub.setTextColor(Color.rgb(245, 80, 190));
        root.addView(sub, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView welcome = text("RECAP MM AI မှ ကြိုဆိုပါ၏", 16);
        welcome.setGravity(Gravity.CENTER);
        welcome.setTextColor(Color.rgb(255, 100, 205));
        root.addView(welcome, new LinearLayout.LayoutParams(-1, dp(42)));

        Button google = actionButton("G   Continue with Google");
        google.setTextSize(15);
        google.setBackground(bg(Color.rgb(31, 25, 52), 22));
        root.addView(google, new LinearLayout.LayoutParams(-1, dp(56)));
        google.setOnClickListener(v -> signInWithGoogle());

        TextView or = text("────────  OR  ────────", 12);
        or.setGravity(Gravity.CENTER);
        or.setTextColor(Color.GRAY);
        root.addView(or, new LinearLayout.LayoutParams(-1, dp(40)));

        countrySpinner = new Spinner(this);
        String[] countries = {
                "🇲🇲 Myanmar (+95)", "🇹🇭 Thailand (+66)",
                "🇺🇸 USA (+1)", "🇬🇧 UK (+44)",
                "🇸🇬 Singapore (+65)", "🇲🇾 Malaysia (+60)",
                "🇯🇵 Japan (+81)", "🇰🇷 South Korea (+82)",
                "🇨🇳 China (+86)", "🇦🇺 Australia (+61)",
                "🇮🇳 India (+91)", "🇻🇳 Vietnam (+84)",
                "🇵🇭 Philippines (+63)", "🇮🇩 Indonesia (+62)",
                "🇩🇪 Germany (+49)", "🇫🇷 France (+33)",
                "🇮🇹 Italy (+39)", "🇪🇸 Spain (+34)",
                "🇨🇦 Canada (+1)", "🇧🇷 Brazil (+55)",
                "🇦🇪 UAE (+971)", "🇸🇦 Saudi Arabia (+966)"
        };

        countrySpinner.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, countries));
        root.addView(countrySpinner, new LinearLayout.LayoutParams(-1, dp(54)));

        phoneNumberInput = new EditText(this);
        phoneNumberInput.setHint("Phone number");
        phoneNumberInput.setHintTextColor(Color.GRAY);
        phoneNumberInput.setTextColor(Color.WHITE);
        phoneNumberInput.setSingleLine(true);
        phoneNumberInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneNumberInput.setPadding(dp(15), 0, dp(15), 0);
        phoneNumberInput.setBackground(bg(Color.rgb(25, 20, 36), 20));
        LinearLayout.LayoutParams phoneLp = new LinearLayout.LayoutParams(-1, dp(54));
        phoneLp.topMargin = dp(8);
        root.addView(phoneNumberInput, phoneLp);

        Button send = actionButton("📩  Send OTP");
        send.setTextSize(15);
        send.setBackground(gradient(new int[]{PINK, PURPLE}, 22));
        root.addView(send, new LinearLayout.LayoutParams(-1, dp(56)));
        send.setOnClickListener(v -> sendOtpFromLocalNumber());

        otpInput = new EditText(this);
        otpInput.setHint("Enter OTP code");
        otpInput.setHintTextColor(Color.GRAY);
        otpInput.setTextColor(Color.WHITE);
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        otpInput.setSingleLine(true);
        otpInput.setPadding(dp(15), 0, dp(15), 0);
        otpInput.setBackground(bg(Color.rgb(25, 20, 36), 20));
        LinearLayout.LayoutParams otpLp = new LinearLayout.LayoutParams(-1, dp(54));
        otpLp.topMargin = dp(8);
        root.addView(otpInput, otpLp);

        Button verify = actionButton("✓  Verify OTP");
        verify.setTextSize(15);
        verify.setBackground(bg(Color.rgb(95, 40, 170), 22));
        root.addView(verify, new LinearLayout.LayoutParams(-1, dp(56)));
        verify.setOnClickListener(v -> verifyOtp());

        TextView info = text("Secure sign in • Google or Phone OTP", 12);
        info.setGravity(Gravity.CENTER);
        info.setTextColor(Color.GRAY);
        root.addView(info, new LinearLayout.LayoutParams(-1, dp(45)));

        s.addView(root);
        setContentView(s);
    }

    private String getCountryCode() {
        String selected = countrySpinner.getSelectedItem().toString();
        int start = selected.indexOf("+");
        int end = selected.indexOf(")", start);
        return selected.substring(start, end);
    }

    private String normalizePhone(String number) {
        number = number.replaceAll("[^0-9]", "");
        while (number.startsWith("0")) number = number.substring(1);
        return getCountryCode() + number;
    }

    private void sendOtpFromLocalNumber() {
        String local = phoneNumberInput.getText().toString().trim();
        if (local.length() < 6) {
            Toast.makeText(this, "ဖုန်းနံပါတ်မှန်အောင်ထည့်ပါ", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                normalizePhone(local), 60, TimeUnit.SECONDS, this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        firebaseAuth.signInWithCredential(credential)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) showVoiceScreen();
                                });
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(MainActivity.this,
                                "OTP ပို့မရပါ: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String id,
                                            PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = id;
                        Toast.makeText(MainActivity.this,
                                "OTP Code ပို့ပြီးပါပြီ", Toast.LENGTH_SHORT).show();
                        otpInput.requestFocus();
                    }
                });
    }

    private void verifyOtp() {
        String code = otpInput.getText().toString().trim();

        if (verificationId == null || code.length() != 6) {
            Toast.makeText(this, "OTP Code 6 လုံးထည့်ပါ", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithCredential(
                PhoneAuthProvider.getCredential(verificationId, code))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) showVoiceScreen();
                    else Toast.makeText(this, "OTP မမှန်ပါ", Toast.LENGTH_SHORT).show();
                });
    }

    private void signInWithGoogle() {
        startActivityForResult(
                googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7001 && data != null && data.getData() != null) {
            android.net.Uri uri = data.getData();
            getSharedPreferences("profile", MODE_PRIVATE)
                    .edit()
                    .putString("photo_uri", uri.toString())
                    .apply();
            showProfileScreen();
            return;
        }

        if (requestCode != RC_SIGN_IN) return;

        try {
            GoogleSignInAccount account =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);

            AuthCredential credential =
                    GoogleAuthProvider.getCredential(account.getIdToken(), null);

            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) showVoiceScreen();
                        else Toast.makeText(this,
                                "Google Login မအောင်မြင်ပါ",
                                Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this,
                    "Google Login မအောင်မြင်ပါ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showVoiceScreen() {
        LinearLayout root = pageRoot();
        root.setPadding(dp(16), dp(10), dp(16), dp(8));

        root.addView(header("AI VOICE", true),
                new LinearLayout.LayoutParams(-1, dp(62)));

        ScrollView contentScroll = new ScrollView(this);
        contentScroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(4), 0, dp(110));

        LinearLayout banner = new LinearLayout(this);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(dp(16), dp(10), dp(16), dp(10));

        GradientDrawable bannerBg = gradient(
                new int[]{Color.rgb(175, 20, 105), Color.rgb(55, 10, 85)}, 20);
        bannerBg.setStroke(dp(1), Color.rgb(255, 55, 190));
        banner.setBackground(bannerBg);

        TextView mic = text("🎙", 38);
        mic.setGravity(Gravity.CENTER);
        banner.addView(mic, new LinearLayout.LayoutParams(dp(55), dp(68)));

        LinearLayout bt = new LinearLayout(this);
        bt.setOrientation(LinearLayout.VERTICAL);

        TextView btitle = text("AI VOICE", 21);
        btitle.setTypeface(null, Typeface.BOLD);
        bt.addView(btitle);

        TextView bsub = text("Text to Speech with Natural Voice", 11);
        bsub.setTextColor(Color.rgb(235, 205, 240));
        bt.addView(bsub);

        banner.addView(bt, new LinearLayout.LayoutParams(0, -2, 1));

        TextView wave = text("〰〰〰", 27);
        wave.setTextColor(Color.rgb(220, 75, 255));
        banner.addView(wave);

        content.addView(banner);

        FrameLayout textCard = new FrameLayout(this);
        GradientDrawable cardBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(58, 5, 28),
                        Color.rgb(28, 5, 30),
                        Color.rgb(10, 7, 18)
                });
        cardBg.setCornerRadius(dp(24));
        cardBg.setStroke(dp(2), Color.rgb(255, 35, 105));
        textCard.setBackground(cardBg);
        textCard.setPadding(dp(14), dp(12), dp(14), dp(10));

        textInput = new EditText(this);
        textInput.setTextColor(Color.WHITE);
        textInput.setHintTextColor(Color.rgb(160, 145, 165));
        textInput.setHint("Recap စာသားရေးပါ...");
        textInput.setTextSize(15);
        textInput.setGravity(Gravity.TOP | Gravity.START);
        textInput.setBackgroundColor(Color.TRANSPARENT);
        textInput.setPadding(dp(3), dp(3), dp(3), dp(42));
        textInput.setSingleLine(false);
        textInput.setMaxLines(20);
        textInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        textInput.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(3000)
        });
        textCard.addView(textInput, new FrameLayout.LayoutParams(-1, -1));

        TextView del = text("🗑", 21);
        del.setGravity(Gravity.CENTER);
        del.setBackground(bg(Color.rgb(220, 25, 55), 17));
        FrameLayout.LayoutParams dl =
                new FrameLayout.LayoutParams(dp(50), dp(40),
                        Gravity.BOTTOM | Gravity.LEFT);
        textCard.addView(del, dl);

        TextView counter = text("0000/3000", 11);
        counter.setGravity(Gravity.CENTER);
        counter.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams cl =
                new FrameLayout.LayoutParams(dp(82), dp(40),
                        Gravity.BOTTOM | Gravity.RIGHT);
        textCard.addView(counter, cl);

        textInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                counter.setText(String.format(
                        java.util.Locale.US, "%04d/3000", s.length()));
            }
            public void afterTextChanged(android.text.Editable e) {}
        });

        del.setOnClickListener(v -> {
            textInput.setText("");
            textInput.requestFocus();
        });

        content.addView(textCard,
                new LinearLayout.LayoutParams(-1, dp(285)));

        TextView select = text("Select Voice", 15);
        select.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(-1, dp(36));
        sl.topMargin = dp(8);
        content.addView(select, sl);

        LinearLayout voices = new LinearLayout(this);
        voices.setGravity(Gravity.CENTER);

        voices.addView(voiceCard("👦", "Thiha", "Male (MM)", true),
                new LinearLayout.LayoutParams(0, dp(78), 1));
        Space vg = new Space(this);
        voices.addView(vg, new LinearLayout.LayoutParams(dp(8), 1));
        voices.addView(voiceCard("👩", "Nilar", "Female (MM)", false),
                new LinearLayout.LayoutParams(0, dp(78), 1));

        content.addView(voices);

        TextView style = text("Voice Style", 14);
        style.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams styLp = new LinearLayout.LayoutParams(-1, dp(35));
        styLp.topMargin = dp(6);
        content.addView(style, styLp);

        styleSpinner = new Spinner(this);
        String[] styles = {"Normal (သဘာဝ)", "Story"};
        styleSpinner.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, styles));
        styleSpinner.setBackground(bg(Color.rgb(18, 18, 35), 18));
        content.addView(styleSpinner, new LinearLayout.LayoutParams(-1, dp(50)));

        SeekBar speed = neonSeek();
        SeekBar pitch = neonSeek();
        SeekBar volume = neonSeek();

        TextView speedLabel = text("Speed     1.0x", 13);
        TextView pitchLabel = text("Pitch      0", 13);
        TextView volumeLabel = text("Volume   100%", 13);

        content.addView(speedLabel, new LinearLayout.LayoutParams(-1, dp(30)));
        content.addView(speed, new LinearLayout.LayoutParams(-1, dp(36)));
        content.addView(pitchLabel, new LinearLayout.LayoutParams(-1, dp(30)));
        content.addView(pitch, new LinearLayout.LayoutParams(-1, dp(36)));
        content.addView(volumeLabel, new LinearLayout.LayoutParams(-1, dp(30)));
        content.addView(volume, new LinearLayout.LayoutParams(-1, dp(36)));

        speed.setMax(20);
        speed.setProgress(10);

        pitch.setMax(20);
        pitch.setProgress(10);

        volume.setMax(100);
        volume.setProgress(100);

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                voiceSpeed = 50 + (p * 5);
                speedLabel.setText(String.format(
                        java.util.Locale.US, "Speed     %.1fx", voiceSpeed / 100.0f));
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });

        pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                voicePitch = p - 10;
                pitchLabel.setText("Pitch      " + voicePitch);
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean u) {
                voiceVolume = p;
                volumeLabel.setText("Volume   " + p + "%");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });

        speakButton = actionButton("🎙  Generate Voice");
        speakButton.setTextSize(16);
        speakButton.setTypeface(null, Typeface.BOLD);
        speakButton.setBackground(gradient(new int[]{PINK, PURPLE}, 27));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(56));
        sp.topMargin = dp(8);
        content.addView(speakButton, sp);
        speakButton.setOnClickListener(v -> generateVoice());

        LinearLayout player = new LinearLayout(this);
        player.setGravity(Gravity.CENTER_VERTICAL);
        player.setPadding(dp(10), dp(5), dp(10), dp(5));
        player.setBackground(bg(Color.rgb(14, 17, 32), 22));

        Button play = actionButton("▶");
        play.setTextSize(20);
        play.setBackground(bg(Color.rgb(18, 20, 38), 30));
        player.addView(play, new LinearLayout.LayoutParams(dp(55), dp(52)));

        TextView time = text("00:00", 11);
        time.setGravity(Gravity.CENTER);
        player.addView(time, new LinearLayout.LayoutParams(dp(55), dp(52)));

        SeekBar track = neonSeek();
        track.setMax(100);
        track.setProgress(0);
        player.addView(track, new LinearLayout.LayoutParams(0, dp(45), 1));

        TextView end = text("00:00", 11);
        end.setGravity(Gravity.CENTER);
        player.addView(end, new LinearLayout.LayoutParams(dp(55), dp(52)));

        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(-1, dp(62));
        pl.topMargin = dp(10);
        content.addView(player, pl);

        LinearLayout actions = new LinearLayout(this);
        Button download = actionButton("⇩  Download");
        Button share = actionButton("⌯  Share");
        actions.addView(download, new LinearLayout.LayoutParams(0, dp(50), 1));
        Space ag = new Space(this);
        actions.addView(ag, new LinearLayout.LayoutParams(dp(8), 1));
        actions.addView(share, new LinearLayout.LayoutParams(0, dp(50), 1));
        LinearLayout.LayoutParams al = new LinearLayout.LayoutParams(-1, dp(50));
        al.topMargin = dp(8);
        content.addView(actions, al);

        download.setOnClickListener(v ->
                Toast.makeText(this, "အသံဖန်တီးပြီးမှ Download လုပ်နိုင်ပါမယ်",
                        Toast.LENGTH_SHORT).show());

        share.setOnClickListener(v ->
                Toast.makeText(this, "အသံဖန်တီးပြီးမှ Share လုပ်နိုင်ပါမယ်",
                        Toast.LENGTH_SHORT).show());

        contentScroll.addView(content);
        root.addView(contentScroll, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(makeNav(0),
                new LinearLayout.LayoutParams(-1, dp(82)));

        setContentView(root);
    }

    private LinearLayout voiceCard(String avatar, String name,
                                   String gender, boolean selected) {
    LinearLayout card = new LinearLayout(this);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(10), dp(5), dp(8), dp(5));
    card.setClickable(true);
    card.setFocusable(true);

    applyVoiceCardStyle(card, selected);

    TextView av = text(avatar, 27);
    av.setGravity(Gravity.CENTER);
    card.addView(av, new LinearLayout.LayoutParams(dp(48), dp(58)));

    LinearLayout box = new LinearLayout(this);
    box.setOrientation(LinearLayout.VERTICAL);

    TextView n = text(name, 15);
    n.setTypeface(null, Typeface.BOLD);
    box.addView(n);

    TextView gr = text(gender, 11);
    gr.setTextColor(Color.LTGRAY);
    box.addView(gr);

    card.addView(box, new LinearLayout.LayoutParams(0, -2, 1));

    TextView check = text(selected ? "✓" : "○", 21);
    check.setTextColor(
            selected ? Color.WHITE : Color.rgb(110, 100, 130)
    );
    check.setGravity(Gravity.CENTER);
    card.addView(
            check,
            new LinearLayout.LayoutParams(dp(30), dp(50))
    );

    card.setOnClickListener(v -> {
        selectedVoice = name.equals("Nilar") ? "nilar" : "thiha";

        android.view.ViewParent vp = card.getParent();

        if (vp instanceof LinearLayout) {
            LinearLayout voiceContainer = (LinearLayout) vp;

            for (int i = 0; i < voiceContainer.getChildCount(); i++) {
                View child = voiceContainer.getChildAt(i);

                if (child instanceof LinearLayout) {
                    LinearLayout otherCard = (LinearLayout) child;
                    boolean isSelected = otherCard == card;

                    applyVoiceCardStyle(otherCard, isSelected);

                    if (otherCard.getChildCount() >= 3) {
                        View checkView = otherCard.getChildAt(2);

                        if (checkView instanceof TextView) {
                            TextView checkText = (TextView) checkView;

                            checkText.setText(
                                    isSelected ? "✓" : "○"
                            );

                            checkText.setTextColor(
                                    isSelected
                                            ? Color.WHITE
                                            : Color.rgb(110, 100, 130)
                            );
                        }
                    }
                }
            }
        }

        Toast.makeText(
                this,
                name + " voice selected",
                Toast.LENGTH_SHORT
        ).show();
    });

    return card;
}

private void applyVoiceCardStyle(
        LinearLayout card,
        boolean selected) {

    GradientDrawable g = gradient(
            selected
                    ? new int[]{
                            Color.rgb(65, 12, 65),
                            Color.rgb(25, 12, 40)
                    }
                    : new int[]{
                            Color.rgb(27, 20, 42),
                            Color.rgb(14, 14, 28)
                    },
            18
    );

    g.setStroke(
            dp(1),
            selected ? PINK : Color.rgb(55, 48, 75)
    );

    card.setBackground(g);
}

    private SeekBar neonSeek() {
        SeekBar s = new SeekBar(this);
        s.setMax(20);
        s.setProgress(10);
        return s;
    }

    private void showTranslateVideoScreen() {
        LinearLayout root = pageRoot();
        root.setPadding(dp(16), dp(10), dp(16), dp(8));

        root.addView(header("TRANSLATE VIDEO", true),
                new LinearLayout.LayoutParams(-1, dp(62)));

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(0, dp(4), 0, dp(110));

        LinearLayout hero = new LinearLayout(this);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(14), dp(8), dp(14), dp(8));
        GradientDrawable hg = gradient(
                new int[]{Color.rgb(185, 15, 160), Color.rgb(55, 15, 100)}, 20);
        hg.setStroke(dp(1), Color.rgb(255, 60, 205));
        hero.setBackground(hg);

        TextView hi = text("🎬", 34);
        hero.addView(hi, new LinearLayout.LayoutParams(dp(55), dp(60)));

        LinearLayout hb = new LinearLayout(this);
        hb.setOrientation(LinearLayout.VERTICAL);
        TextView ht = text("TRANSLATE VIDEO", 19);
        ht.setTypeface(null, Typeface.BOLD);
        hb.addView(ht);
        TextView hs = text("Translate & Recap with AI Voice", 11);
        hs.setTextColor(Color.LTGRAY);
        hb.addView(hs);
        hero.addView(hb, new LinearLayout.LayoutParams(0, -2, 1));
        hero.addView(text("▣", 38), new LinearLayout.LayoutParams(dp(58), dp(58)));
        c.addView(hero);

        TextView linkTitle = text("Paste Video Link", 14);
        linkTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(-1, dp(34));
        ltp.topMargin = dp(8);
        c.addView(linkTitle, ltp);

        LinearLayout linkRow = new LinearLayout(this);
        linkRow.setGravity(Gravity.CENTER_VERTICAL);
        linkRow.setPadding(dp(10), 0, dp(5), 0);
        GradientDrawable linkBg = bg(Color.rgb(18, 18, 34), 18);
        linkBg.setStroke(dp(1), Color.rgb(255, 60, 200));
        linkRow.setBackground(linkBg);

        TextView chain = text("🔗", 22);
        chain.setGravity(Gravity.CENTER);
        linkRow.addView(chain, new LinearLayout.LayoutParams(dp(40), dp(52)));

        EditText link = new EditText(this);
        link.setHint("Paste YouTube, TikTok, Facebook, RedNote link here...");
        link.setHintTextColor(Color.rgb(135, 125, 155));
        link.setTextColor(Color.WHITE);
        link.setSingleLine(true);
        link.setBackgroundColor(Color.TRANSPARENT);
        linkRow.addView(link, new LinearLayout.LayoutParams(0, dp(52), 1));

        TextView clear = text("✕", 20);
        clear.setGravity(Gravity.CENTER);
        linkRow.addView(clear, new LinearLayout.LayoutParams(dp(40), dp(52)));
        clear.setOnClickListener(v -> link.setText(""));

        c.addView(linkRow);

        LinearLayout platforms = new LinearLayout(this);
        platforms.setGravity(Gravity.CENTER);
        String[] pn = {"▶ YouTube", "♪ TikTok", "f Facebook", "R RedNote"};
        for (String p : pn) {
            TextView x = text(p, 10);
            x.setGravity(Gravity.CENTER);
            x.setTypeface(null, Typeface.BOLD);
            x.setBackground(bg(Color.rgb(23, 18, 37), 15));
            LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(0, dp(43), 1);
            xp.setMargins(dp(2), dp(8), dp(2), dp(2));
            platforms.addView(x, xp);
        }
        c.addView(platforms);

        c.addView(sectionSpinner("Original Language (Auto Detect)",
                new String[]{"🌐 Auto Detect", "Myanmar", "English", "Chinese", "Korean", "Japanese"}));

        c.addView(sectionSpinner("Translate To",
                new String[]{"🇲🇲 မြန်မာ (Myanmar)", "🇹🇭 Thai", "🇬🇧 English",
                        "🇨🇳 Chinese", "🇰🇷 Korean", "🇯🇵 Japanese"}));

        TextView aiTitle = text("AI Options", 14);
        aiTitle.setTypeface(null, Typeface.BOLD);
        c.addView(aiTitle, new LinearLayout.LayoutParams(-1, dp(36)));

        LinearLayout ai = new LinearLayout(this);
        ai.addView(optionCard("✦", "Generate Recap", "Short & easy to understand", true),
                new LinearLayout.LayoutParams(0, dp(70), 1));
        Space ag = new Space(this);
        ai.addView(ag, new LinearLayout.LayoutParams(dp(8), 1));
        ai.addView(optionCard("▤", "Full Translate", "Translate full content", false),
                new LinearLayout.LayoutParams(0, dp(70), 1));
        c.addView(ai);

        LinearLayout voice = new LinearLayout(this);
        voice.setGravity(Gravity.CENTER_VERTICAL);
        voice.setPadding(dp(12), 0, dp(10), 0);
        voice.setBackground(bg(Color.rgb(18, 18, 34), 17));
        TextView vt = text("🎙  Add Myanmar Voice", 12);
        voice.addView(vt, new LinearLayout.LayoutParams(0, dp(52), 1));
        Switch sw = new Switch(this);
        sw.setChecked(true);
        voice.addView(sw);
        LinearLayout.LayoutParams vl = new LinearLayout.LayoutParams(-1, dp(52));
        vl.topMargin = dp(8);
        c.addView(voice, vl);

        Button translate = actionButton("✦  Translate Video");
        translate.setTextSize(16);
        translate.setTypeface(null, Typeface.BOLD);
        translate.setBackground(gradient(new int[]{PINK, PURPLE}, 28));
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(-1, dp(56));
        tl.topMargin = dp(8);
        c.addView(translate, tl);

        TextView resultTitle = text("Result", 14);
        resultTitle.setTypeface(null, Typeface.BOLD);
        c.addView(resultTitle);

        LinearLayout tabs = new LinearLayout(this);
        String[] tabsText = {"Text", "Translated Text", "Audio", "Video"};
        for (int i = 0; i < 4; i++) {
            TextView tab = text(tabsText[i], 10);
            tab.setGravity(Gravity.CENTER);
            tab.setBackground(bg(i == 0 ? Color.rgb(255, 55, 190) : Color.rgb(19, 19, 35), 15));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(40), 1);
            tp.setMargins(dp(2), dp(2), dp(2), dp(2));
            tabs.addView(tab, tp);
        }
        c.addView(tabs);

        EditText result = new EditText(this);
        result.setHint("ရလဒ် ဒီနေရာမှာပေါ်လာမယ်...");
        result.setHintTextColor(Color.rgb(130, 125, 145));
        result.setTextColor(Color.WHITE);
        result.setGravity(Gravity.TOP);
        result.setMinLines(4);
        result.setPadding(dp(12), dp(10), dp(12), dp(10));
        result.setBackground(bg(Color.rgb(18, 18, 34), 18));
        c.addView(result, new LinearLayout.LayoutParams(-1, dp(120)));

        translate.setOnClickListener(v -> {
            String u = link.getText().toString().trim();
            if (!u.startsWith("http://") && !u.startsWith("https://")) {
                Toast.makeText(this, "Video link မှန်အောင်ထည့်ပါ",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this,
                    "Translation backend ချိတ်ဆက်ပြီးနောက် ဒီနေရာကနေ စတင်လုပ်ဆောင်ပါမယ်",
                    Toast.LENGTH_LONG).show();
        });

        sc.addView(c);
        root.addView(sc, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(makeNav(1), new LinearLayout.LayoutParams(-1, dp(82)));

        setContentView(root);
    }

    private LinearLayout sectionSpinner(String title, String[] items) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);

        TextView t = text(title, 13);
        t.setTypeface(null, Typeface.BOLD);
        box.addView(t, new LinearLayout.LayoutParams(-1, dp(32)));

        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, items));
        sp.setBackground(bg(Color.rgb(18, 18, 34), 18));
        box.addView(sp, new LinearLayout.LayoutParams(-1, dp(50)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(88));
        lp.topMargin = dp(6);
        box.setLayoutParams(lp);
        return box;
    }

    private LinearLayout optionCard(String icon, String title,
                                    String sub, boolean selected) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(4), dp(5), dp(4));

        GradientDrawable g = bg(
                selected ? Color.rgb(48, 14, 55) : Color.rgb(17, 17, 32), 16);
        g.setStroke(dp(1), selected ? PINK : Color.rgb(55, 48, 75));
        card.setBackground(g);

        TextView i = text(icon, 22);
        i.setGravity(Gravity.CENTER);
        card.addView(i, new LinearLayout.LayoutParams(dp(42), dp(55)));

        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        TextView t = text(title, 11);
        t.setTypeface(null, Typeface.BOLD);
        b.addView(t);
        TextView s = text(sub, 8);
        s.setTextColor(Color.LTGRAY);
        b.addView(s);
        card.addView(b, new LinearLayout.LayoutParams(0, -2, 1));

        TextView check = text(selected ? "✓" : "○", 18);
        check.setGravity(Gravity.CENTER);
        card.addView(check, new LinearLayout.LayoutParams(dp(25), dp(55)));

        return card;
    }

    private void showProfileScreen() {
        LinearLayout root = pageRoot();
        root.setPadding(dp(16), dp(10), dp(16), dp(8));

        LinearLayout top = header("PROFILE", true);
        root.addView(top, new LinearLayout.LayoutParams(-1, dp(62)));

        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);

        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(0, dp(4), 0, dp(110));

        LinearLayout profile = new LinearLayout(this);
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable pg = gradient(
                new int[]{Color.rgb(55, 8, 65), Color.rgb(23, 12, 43)}, 24);
        pg.setStroke(dp(1), Color.rgb(255, 45, 190));
        profile.setBackground(pg);

        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setImageResource(android.R.drawable.ic_menu_myplaces);

        GradientDrawable avBg = new GradientDrawable();
        avBg.setShape(GradientDrawable.OVAL);
        avBg.setColor(Color.rgb(45, 15, 48));
        avBg.setStroke(dp(2), PINK);
        avatar.setBackground(avBg);

        profile.addView(avatar, new LinearLayout.LayoutParams(dp(92), dp(92)));

        String savedPhoto = getSharedPreferences("profile", MODE_PRIVATE)
                .getString("photo_uri", null);

        if (savedPhoto != null) {
            try {
                avatar.setImageURI(android.net.Uri.parse(savedPhoto));
            } catch (Exception ignored) {}
        }

        if (savedPhoto == null &&
                firebaseAuth.getCurrentUser() != null &&
                firebaseAuth.getCurrentUser().getPhotoUrl() != null) {
            final String photoUrl = firebaseAuth.getCurrentUser()
                    .getPhotoUrl().toString();

            new Thread(() -> {
                try {
                    HttpURLConnection conn =
                            (HttpURLConnection)new URL(photoUrl).openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.connect();

                    InputStream in = conn.getInputStream();
                    final android.graphics.Bitmap bitmap =
                            android.graphics.BitmapFactory.decodeStream(in);
                    in.close();

                    if (bitmap != null) {
                        runOnUiThread(() -> avatar.setImageBitmap(bitmap));
                    }
                } catch (Exception ignored) {}
            }).start();
        }

        LinearLayout user = new LinearLayout(this);
        user.setOrientation(LinearLayout.VERTICAL);
        user.setPadding(dp(12), 0, dp(4), 0);

        String name = "SayarGyi";
        String email = "";
        String phone = "";

        FirebaseUser u = firebaseAuth.getCurrentUser();
        if (u != null) {
            if (u.getDisplayName() != null &&
                    !u.getDisplayName().trim().isEmpty())
                name = u.getDisplayName();
            if (u.getEmail() != null) email = u.getEmail();
            if (u.getPhoneNumber() != null) phone = u.getPhoneNumber();
        }

        TextView nv = text(name + "  ✓", 21);
        nv.setTypeface(null, Typeface.BOLD);
        user.addView(nv);

        TextView role = text("App Creator & Content Creator", 11);
        role.setTextColor(Color.LTGRAY);
        user.addView(role);

        Button edit = actionButton("📷  Change Photo");
        edit.setTextSize(12);
        edit.setTextColor(Color.WHITE);
        edit.setBackground(bg(Color.rgb(36, 18, 55), 20));
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(dp(125), dp(42));
        ep.topMargin = dp(7);
        user.addView(edit, ep);

        edit.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK);
            pick.setType("image/*");
            startActivityForResult(pick, 7001);
        });

        profile.addView(user, new LinearLayout.LayoutParams(0, -2, 1));
        c.addView(profile);

        c.addView(profileInfoCard("♟", "Name", name));
        c.addView(profileInfoCard("☎", "Phone Number",
                phone.isEmpty() ? "Not set" : phone));
        c.addView(profileInfoCard("▣", "Age", "21"));
        c.addView(profileInfoCard("⌖", "Location", "Thailand"));
        c.addView(profileInfoCard("◎", "Language", "မြန်မာ (Myanmar)"));

        TextView supportTitle = text("👤  Contact & Support", 15);
        supportTitle.setTypeface(null, Typeface.BOLD);
        supportTitle.setPadding(dp(14), 0, dp(10), 0);
        supportTitle.setTextColor(Color.WHITE);
        supportTitle.setBackground(gradient(
                new int[]{Color.rgb(155, 15, 170), Color.rgb(45, 20, 85)}, 17));
        c.addView(supportTitle, new LinearLayout.LayoutParams(-1, dp(50)));

        c.addView(profileInfoCard("✉", "Contact Admin", "RECAP MM AI Support"));
        c.addView(profileInfoCard("♪", "TikTok Account", "@sayargyi"));
        c.addView(profileInfoCard("f", "Facebook Account", "SayarGyi"));
        c.addView(profileInfoCard("➤", "Telegram Account", "@sayargyi"));
        c.addView(profileInfoCard("♧", "App Support / Report Problem", "Get help with RECAP MM AI"));

        Button logout = actionButton("⇱  Logout");
        logout.setTextSize(16);
        logout.setTypeface(null, Typeface.BOLD);
        logout.setBackground(gradient(
                new int[]{Color.rgb(255, 55, 65), Color.rgb(235, 35, 35)}, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
        lp.topMargin = dp(6);
        c.addView(logout, lp);

        logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            showLoginScreen();
        });

        sc.addView(c);
        root.addView(sc, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(makeNav(2), new LinearLayout.LayoutParams(-1, dp(82)));

        setContentView(root);
    }

    private LinearLayout profileInfoCard(String iconText,
                                         String titleText,
                                         String valueText) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(5), dp(8), dp(5));

        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.rgb(26, 18, 38), Color.rgb(12, 11, 23)});
        g.setCornerRadius(dp(17));
        g.setStroke(dp(1), Color.rgb(58, 40, 72));
        row.setBackground(g);

        TextView icon = text(iconText, 20);
        icon.setGravity(Gravity.CENTER);
        icon.setTextColor(Color.rgb(255, 80, 210));
        row.addView(icon, new LinearLayout.LayoutParams(dp(46), dp(50)));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), 0, dp(5), 0);

        TextView title = text(titleText, 13);
        title.setTypeface(null, Typeface.BOLD);
        box.addView(title);

        TextView value = text(valueText, 10);
        value.setTextColor(Color.LTGRAY);
        box.addView(value);

        row.addView(box, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 28);
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Color.rgb(215, 175, 235));
        row.addView(arrow, new LinearLayout.LayoutParams(dp(30), dp(50)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(62));
        lp.topMargin = dp(6);
        row.setLayoutParams(lp);

        return row;
    }

    private void generateVoice() {
        String content = textInput.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Recap စာသားထည့်ပါ",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String voice =
                selectedVoice.equals("thiha")
                        ? "thiha" : "nilar";

        String selectedStyle = styleSpinner.getSelectedItem().toString();

        speakButton.setEnabled(false);
        speakButton.setText("⏳ အသံဖန်တီးနေသည်...");

        new Thread(() -> {
            try {
                URL url = new URL("https://recap-mm-ai.onrender.com/tts");
                HttpURLConnection connection =
                        (HttpURLConnection)url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(120000);
                connection.setRequestProperty(
                        "Content-Type", "application/json");

                JSONObject json = new JSONObject();
                json.put("text", content);
                json.put("voice", voice);
            json.put("style", selectedStyle.split(" ")[0]);
                json.put("speed", voiceSpeed);
                json.put("pitch", voicePitch);
                json.put("volume", voiceVolume);

                OutputStream output = connection.getOutputStream();
                output.write(json.toString().getBytes("UTF-8"));
                output.close();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200)
                    throw new IOException("HTTP " + responseCode);

                File audioFile = new File(getCacheDir(), "recap_voice.mp3");
                InputStream input = connection.getInputStream();
                FileOutputStream out = new FileOutputStream(audioFile);

                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) != -1)
                    out.write(buffer, 0, length);

                out.close();
                input.close();

                runOnUiThread(() -> {
                    speakButton.setEnabled(true);
                    speakButton.setText("🎙  Generate Voice");

                    try {
                        if (mediaPlayer != null) mediaPlayer.release();

                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                        mediaPlayer.setOnPreparedListener(mp -> mp.start());
                        mediaPlayer.prepareAsync();

                        Toast.makeText(this,
                                "Voice Ready ✓",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "အသံဖွင့်မရပါ",
                                Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    speakButton.setEnabled(true);
                    speakButton.setText("🎙  Generate Voice");
                    Toast.makeText(this,
                            "Voice server မချိတ်နိုင်ပါ: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
