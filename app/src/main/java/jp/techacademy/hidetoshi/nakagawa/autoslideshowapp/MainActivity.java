package jp.techacademy.hidetoshi.nakagawa.autoslideshowapp;

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
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;


    final long TIMER_INTERVAL = 2000;
    private Timer mTimer = null;
    private Button mNextButton;
    private Button mBackButton;
    private Button mResumeButton;
    private TextView mAltText;
    private ImageView mImageVIew;
    private Handler mHandler = new Handler();
    private Cursor cursor;
    private int startPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNextButton = (Button) findViewById(R.id.next_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        mResumeButton = (Button) findViewById(R.id.resume_button);
        mAltText = (TextView) findViewById(R.id.alt_text);
        mImageVIew = (ImageView) findViewById(R.id.imageView);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
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
            cursor = resolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                    null, // 項目(null = 全項目)
                    null, // フィルタ条件(null = フィルタなし)
                    null, // フィルタ用パラメータ
                    null // ソート (null ソートなし)
            );
        if (cursor.getCount()>0) {
            int startPosition = cursor.getPosition();
            cursor.moveToFirst();
            imageViewMethod(cursor);

            // 進むボタン処理
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cursor.moveToNext() == false) {
                        cursor.moveToFirst();
                        imageViewMethod(cursor);
                    } else {
                        imageViewMethod(cursor);
                    }
                }
            });

            // 戻るボタン処理
            mBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cursor.moveToPrevious() == false) {
                        cursor.moveToLast();
                        imageViewMethod(cursor);
                    } else {
                        imageViewMethod(cursor);
                    }
                }
            });

            // 再生/停止ボタン処理
            mResumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTimer == null) {
                        mResumeButton.setText("停止");
                        mNextButton.setEnabled(false);
                        mBackButton.setEnabled(false);

                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (cursor.moveToNext() == false) {
                                            cursor.moveToFirst();
                                            imageViewMethod(cursor);
                                        } else {
                                            imageViewMethod(cursor);
                                        }
                                    }
                                });
                            }
                        }, 100, TIMER_INTERVAL);
                    } else {
                        mResumeButton.setText("再生");
                        mNextButton.setEnabled(true);
                        mBackButton.setEnabled(true);
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            });
            cursor.moveToPosition(startPosition);
        } else {
            mAltText.setText("画像がありません。");
            mResumeButton.setEnabled(false);
            mNextButton.setEnabled(false);
            mBackButton.setEnabled(false);
            mImageVIew.setVisibility(View.GONE);
        }
    }

    // 画像表示
    protected void imageViewMethod(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        mImageVIew.setImageURI(imageUri);
        // Log.d("ANDROID", "URI : " + imageUri.toString());
    }
}