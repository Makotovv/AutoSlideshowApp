package jp.techacademy.makoto.kaneko.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int TIMER_INTERVAL = 2000; // タイマー周期
    Timer mTimer;

    Button prevButton;      // 戻る
    Button playButton;      // 再生
    Button nextButton;      // 進む
    Cursor mCursor;          // 画像
    boolean permission = false; // パーミッション判定

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // アクセス許可判定
        if (checkPermission()) {
            permission = true;
            getContentsInfo();
        }
        setClickListener();
    }

    // アクセス許可
    private boolean checkPermission() {

        boolean result = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0以降の場合
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            // Android 6.0未満
            result = true;
        }

        return result;
    }

    @Override
    public void onClick(View v) {

        if( permission ) {
            if (v == prevButton) { // 戻流ボタン側の設定、前に移動、表示、最後に移動
                if (mCursor.moveToPrevious()) {
                    displayImage();
                } else if (mCursor.moveToLast()) {
                    displayImage();
                }
            } else if (v == playButton) { // 再生ボタン側の設定、進む、表示、最初に移動
                onClickPlayButton();
            } else if (v == nextButton) {
                if (mCursor.moveToNext()) {
                    displayImage();
                } else if (mCursor.moveToFirst()) {
                    displayImage();
                }
            }
        }
    }


    // 再生ボタン
    public void onClickPlayButton() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {//次に移動、表示、最初に移動
                            if (mCursor.moveToNext()) {
                                displayImage();
                            } else if (mCursor.moveToFirst()) {
                                displayImage();
                            }
                        }
                    });
                }
            }, TIMER_INTERVAL, TIMER_INTERVAL);

            playButton.setText("停止"); // 表記変更、ボタン無効
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
        } else {// 再生中のコード、タイマー停止、表記変更、ボタン有効
            mTimer.cancel();
            mTimer = null;
            playButton.setText("再生");
            prevButton.setEnabled(true);
            nextButton.setEnabled(true);
        }
    }

    private void setClickListener()    {
        prevButton = (Button) findViewById(R.id.button1);
        prevButton.setOnClickListener(this);

        playButton = (Button) findViewById(R.id.button2);
        playButton.setOnClickListener(this);

        nextButton = (Button) findViewById(R.id.button3);
        nextButton.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) {
            displayImage();
        }
    }

    // 写真表示
    private void  displayImage() {
        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = mCursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }

}