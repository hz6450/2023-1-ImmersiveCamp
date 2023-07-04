package net.npaka.textrecognitionex;
import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.format.DateFormat;
import android.view.Window;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//AppDelegate
public class AppDelegate extends Activity {
    //Permission
    private final static int REQUEST_PERMISSONS = 0;
    private final static String[] PERMISSIONS = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE};
    private boolean permissionGranted = false;

    //Image Picker
    private final static int REQUEST_PICKER = 0;
    private Uri cameraUri;
    private ICompletion openPickerCompletion;
    public interface ICompletion {
        void onCompletion(Bitmap image);
    }


//====================
//Life Cycle
//====================
    //앱기동시 호출
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new ViewController(this));

        //유저 이용허가 확인
        checkPermissions();
    }


//====================
//Permission
//====================
    //유저 이용허가 확인 
    private void checkPermissions() {
        //허가
        if (isGranted()) {
            permissionGranted = true;
        }
        //미허가
        else {
            //허가 다이얼로그 표시
            ActivityCompat.requestPermissions(this, PERMISSIONS,
                    REQUEST_PERMISSONS);
        }
    }

    //유저 이용허가 완료상태인지 아닌지 확인
    private boolean isGranted() {
        for (int i  = 0; i < PERMISSIONS.length; i++) {
            if (PermissionChecker.checkSelfPermission(
                AppDelegate.this, PERMISSIONS[i]) !=
                PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //허가 다이얼로그 선택시 호출
    @Override
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] results) {
        if (requestCode == REQUEST_PERMISSONS) {
            permissionGranted = true;
        } else {
            super.onRequestPermissionsResult(
                requestCode, permissions, results);
        }
    }


//====================
//image picker
//====================
    //이미지 피커 열기
    public void openPicker(int sourceType, ICompletion completion) {
        openPickerCompletion = completion;

        //퍼미션 확인
        if (!permissionGranted) return;

        //카메라
        if (sourceType == 0) {
            String photoName = DateFormat.format("yyyyMMddkkmmss",
                System.currentTimeMillis()).toString() + ".jpg";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE, photoName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            this.cameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, this.cameraUri);
            startActivityForResult(intentCamera, REQUEST_PICKER);
        }
        //갤러리
        else if (sourceType == 1) {
            this.cameraUri = null;
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            startActivityForResult(intent, REQUEST_PICKER);
        }
    }

    //이미지 피커 결과획득시 호출
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_PICKER) {
            if (resultCode != Activity.RESULT_OK) {
                openPickerCompletion.onCompletion(null);
            }
            Bitmap image = null;
            InputStream in = null;

            //카메라에서 이미지 획득
            if (this.cameraUri != null) {
                final List<String> paths = cameraUri.getPathSegments();
                String strId = paths.get(3);
                Cursor crsCursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.MediaColumns.DATA},
                    "_id=?",
                    new String[]{strId},
                    null
                );
                crsCursor.moveToFirst();
                String filePath = crsCursor.getString(0);
                try {
                    in = new FileInputStream(filePath);
                    image = BitmapFactory.decodeFile(filePath);
                    image = modifyOrientation(image, in);
                    in.close();
                } catch (Exception e) {
                    try {
                        if (in != null) in.close();
                    } catch (Exception e2) {
                    }
                }
            }
            //사진 라이브러리에서 이미지 획득
            else if (resultData != null) {
                try {
                    in = getContentResolver().openInputStream(resultData.getData());
                    image = BitmapFactory.decodeStream(in);
                    in.close();
                    in = getContentResolver().openInputStream(resultData.getData());
                    image = modifyOrientation(image, in);
                    in.close();
                } catch (Exception e) {
                    try {
                        if (in != null) in.close();
                    } catch (Exception e2) {
                    }
                }
            }
            openPickerCompletion.onCompletion(image);
        }
    }

    //이미지방향 적용
    public static Bitmap modifyOrientation(Bitmap bitmap, InputStream in) {
        try {
            ExifInterface ei = new ExifInterface(in);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return rotate(bitmap, 90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return rotate(bitmap, 180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return rotate(bitmap, 270);
            } else if (orientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL) {
                return flip(bitmap, true, false);
            } else if (orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL) {
                return flip(bitmap, false, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //이미지 회전
    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //이미지 반전
    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0,
            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}