/*
 *
 *  * Copyright 2017, Peter Vincent
 *  * Licensed under the Apache License, Version 2.0, Promise.
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package dev4vin.promise.cac.notif;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

import androidx.annotation.DrawableRes;
import dev4vin.promise.data.log.LogUtil;
import dev4vin.promise.util.ApiHelper;

public class Notification {
  private String TAG = LogUtil.makeTag(Notification.class);

  private NType nType;
  private Duration duration;

  private Context context;
  private NDialog nDialog;
  private ProgressDialog progressDialog;

  public Notification(Context context) {
    this.setContext(context);
  }

  public void showDialog(
      String title,
      String message,
      NDialog.DButton positive,
      NDialog.DButton negative,
      NDialog.DButton neutral) {

    NDialog nDialog = new NDialog(getContext(), title, message, positive, negative, neutral);

    nDialog.show();
  }

  public void showDialog(
      String title,
      String message,
      NDialog.DButton positive,
      NDialog.DButton negative,
      NDialog.DButton neutral,
      boolean close) {

    NDialog nDialog =
        new NDialog(getContext(), title, message, positive, negative, neutral).close(close);

    nDialog.show();
  }

  public void showDialog(
      String title,
      String message,
      NDialog.DButton positive,
      NDialog.DButton negative,
      NDialog.DButton neutral,
      NDialog.OnAnswer onAnswer) {

    nDialog = new NDialog(getContext(), title, message, positive, negative, neutral, onAnswer);
    nDialog.show();
  }

  public void showProgress(String message) {
    if (progressDialog == null) progressDialog = new ProgressDialog(getContext());
    progressDialog.setMessage(message);
    progressDialog.show();
  }

  public void showProgress(String title, String message) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getContext());
    }
    progressDialog.setTitle(title);
    progressDialog.setMessage(message);
    progressDialog.show();
  }

  public void showProgress(String message, boolean a) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getContext());
      progressDialog.setCancelable(a);
    }
    progressDialog.setMessage(message);
    progressDialog.show();
  }

  public void showProgress(String title, String message, boolean a) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getContext());
      progressDialog.setCancelable(a);
    }
    progressDialog.setTitle(title);
    progressDialog.setMessage(message);
    progressDialog.show();
  }

  public void showProgress(String message, int percent) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getContext());
      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressDialog.setProgress(0);
      progressDialog.setMax(100);
    }
    if (percent <= 100) {
      progressDialog.setMessage(message);
      progressDialog.incrementProgressBy(percent - progressDialog.getProgress());
    }
    if (!progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  public void showProgress(String title, String message, int percent) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(getContext());
      progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      progressDialog.setProgress(0);
      progressDialog.setMax(100);
    }
    if (percent <= 100) {
      progressDialog.setTitle(title);
      progressDialog.setMessage(message);
      progressDialog.incrementProgressBy(percent - progressDialog.getProgress());
    }
    if (!progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  public void dismiss() {
    if (nDialog != null) {
      nDialog.dismiss();
    }
    if (progressDialog != null) {
      progressDialog.dismiss();
    }
  }

  public void showToast(String message) {
    NToast toast = new NToast(getContext(), message);
    toast.show();
  }

  public void showToast(String message, Duration duration) {
    NToast toast = new NToast(getContext(), message);
    toast.show();
  }

  public void soundTone(String audioFileName) {
    AudioManager meng = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
    if (volume != 0) {
      MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(audioFileName));
      if (mediaPlayer != null) {
        mediaPlayer.start();
      }
    }
  }

  public void vibrate() {
    final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    if (vibrator.hasVibrator()) {
      ApiHelper.checkPermission(
          getContext(),
          new ApiHelper.CallBack() {
            @Override
            public void onSuccess(String permission) {
              vibrator.vibrate(500);
            }

            @Override
            public void onFailure(String permission) {}
          },
          Manifest.permission.VIBRATE);
    }
  }

  public void vibrate(Duration duration) {
    final int length;
    switch (duration.getLength()) {
      case Duration.LON:
        {
          length = 1000;
          break;
        }
      case Duration.SHO:
        {
          length = 500;
          break;
        }
      default:
        {
          length = 500;
        }
    }
    final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    if (vibrator.hasVibrator()) {
      ApiHelper.checkPermission(
          getContext(),
          new ApiHelper.CallBack() {
            @Override
            public void onSuccess(String permission) {
              vibrator.vibrate(length);
            }

            @Override
            public void onFailure(String permission) {
              LogUtil.e(TAG, permission);
            }
          },
          Manifest.permission.VIBRATE);
    }
  }

  public void showNotification(
      Bitmap bitmap,
      String title,
      String message,
      @DrawableRes int small_icon,
      PendingIntent pendingIntent) {
    NBar.notify(getContext(), title, message, bitmap, small_icon, pendingIntent);
  }

  public void showNotification(
      Bitmap bitmap, String title, String message, @DrawableRes int small_icon, int progress) {
    NBar.notifyWithProgress(getContext(), title, message, bitmap, small_icon, progress);
  }

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }
}
