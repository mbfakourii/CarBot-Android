package ir.flashone.car_bot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zcw.togglebutton.ToggleButton;

public class MainActivity extends AppCompatActivity {
    int counter = 0;
    boolean bol_check = false;
    private String str_Tag1 = "";
    private String str_Tag2 = "";
    ToggleButton tob_on_off;
    private boolean bolAppEnable = false;
    VerticalSeekBar sbr_speed;

    //motor_1
    int ENA_Value = 200;
    int IN1_Value = 0;
    int IN2_Value = 0;

    //motor_2
    int IN3_Value = 0;
    int IN4_Value = 0;
    int ENB_Value = 200;

    TcpClient mTcpClient;


    @SuppressLint({"ClickableViewAccessibility", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //set Thumb picture for seekBar
        getPercent percent = new getPercent(this);
        VerticalSeekBar sw = (VerticalSeekBar) findViewById(R.id.sbr_speed);
        Drawable dr = getResources().getDrawable(R.mipmap.ic_thumbler_small);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable myIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, percent.percent(1, 3), percent.percent(2, 7), true));
        sw.setThumb(myIcon);


        sbr_speed = (VerticalSeekBar) findViewById(R.id.sbr_speed);
        sbr_speed.setOnTouchListener(new View.OnTouchListener()

        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (bolAppEnable == false) return false;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    setSpeedMotors(sbr_speed.getProgress());
                }
                return false;
            }
        });
        sbr_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (bolAppEnable == false) sbr_speed.setProgress(0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tob_on_off = (ToggleButton) findViewById(R.id.tob_on_off);
        tob_on_off.setOnToggleChanged(new ToggleButton.OnToggleChanged()

        {
            @Override
            public void onToggle(boolean on) {
                bolAppEnable = on;
                if (on) {
                    ShowSnackbar("فعال شد!");
                    sbr_speed.setProgress(200);
                    setSpeedMotors(200);
                } else {
                    ShowSnackbar("غیر فعال شد!");
                    sbr_speed.setProgress(0);
                    stopMotors();
                }
            }
        });


        counter = 0;
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (bolAppEnable == false) return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (counter < 2) {
                            if (str_Tag1.equals("")) {
                                str_Tag1 = v.getTag().toString();
                            } else if (str_Tag2.equals("")) {
                                str_Tag2 = v.getTag().toString();
                            }

                            checkClick();
                            counter++;
                        } else {
                            bol_check = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (bol_check) {
                            bol_check = false;
                        } else {
                            counter = 0;
                            if (v.getTag().equals("up") || v.getTag().equals("down")) {
                                str_Tag1 = "";
                                str_Tag2 = "";
                                stopMotors();
                            } else {
                                str_Tag2 = "";
                                checkClick();
                            }
                        }
                        break;

                }

                return true;
            }
        };

        //initialize buttons
        findViewById(R.id.btn_up).setOnTouchListener(touchListener);

        findViewById(R.id.btn_down).setOnTouchListener(touchListener);

        findViewById(R.id.btn_left).setOnTouchListener(touchListener);

        findViewById(R.id.btn_right).setOnTouchListener(touchListener);


    }

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {
        Activity activity;
        public ConnectTask(Activity activity){
            this.activity=activity;
        }

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            },activity);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

        }


    }
    private void checkClick() {

        Log.i("TAG", "----->>>>>> " + str_Tag1);
        Log.i("TAG", "----->>>>>> " + str_Tag2);

        if (str_Tag1.equals("up") && str_Tag2.equals("")) {
            up();
            return;
        }
        if (str_Tag1.equals("down") && str_Tag2.equals("")) {
            down();
            return;
        }

        if (str_Tag1.equals("up") && str_Tag2.equals("left")) {
            up_left();
            return;
        }
        if (str_Tag1.equals("up") && str_Tag2.equals("right")) {
            up_right();
            return;
        }

        if (str_Tag1.equals("down") && str_Tag2.equals("left")) {
            down_left();
            return;
        }
        if (str_Tag1.equals("down") && str_Tag2.equals("right")) {
            down_right();
            return;
        }

    }


    private void setSpeedMotors(int value) {
        ENA_Value = value;
        ENB_Value = value;
        sendMassage();
    }

    private void stopMotors() {
        IN1_Value = 0;
        IN2_Value = 0;
        IN3_Value = 0;
        IN4_Value = 0;

        sendMassage();
    }

    private void up() {
        IN1_Value = 1;
        IN2_Value = 0;
        IN3_Value = 1;
        IN4_Value = 0;

        sendMassage();
    }

    private void down() {
        IN1_Value = 0;
        IN2_Value = 1;
        IN3_Value = 0;
        IN4_Value = 1;

        sendMassage();
    }


    private void up_left() {
        IN1_Value = 1;
        IN2_Value = 0;
        IN3_Value = 0;
        IN4_Value = 0;

        sendMassage();
    }

    private void up_right() {
        IN1_Value = 0;
        IN2_Value = 0;
        IN3_Value = 1;
        IN4_Value = 0;

        sendMassage();
    }


    private void down_left() {
        IN1_Value = 0;
        IN2_Value = 1;
        IN3_Value = 0;
        IN4_Value = 0;

        sendMassage();
    }

    private void down_right() {
        IN1_Value = 0;
        IN2_Value = 0;
        IN3_Value = 0;
        IN4_Value = 1;

        sendMassage();
    }

    @Override
    protected void onPause() {
        disconnect();
        finish();
        super.onPause();
    }

    public void connect() {
        try {
            new ConnectTask(this).execute("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        try {

            mTcpClient.stopClient();
            tob_on_off.setToggleOff();
            sbr_speed.setProgress(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendMassage() {
        String value = "ENA=" + ENA_Value + "-IN1=" + IN1_Value + "-IN2=" + IN2_Value + "-IN3=" + IN3_Value + "-IN4=" + IN4_Value + "-ENB=" + ENB_Value;

        if (mTcpClient == null) {
            connect();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mTcpClient.sendMessage(value);
    }

    private void ShowSnackbar(String value) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.lay), value, Snackbar.LENGTH_LONG);

        TextView view1 = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        view1.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        snackbar.show();
    }

}
