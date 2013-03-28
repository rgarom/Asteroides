package org.example.asteroides;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore.Audio;
import android.widget.Toast;

public class ServicioMusica extends Service {
    MediaPlayer reproductor;
    private NotificationManager nm;
    private static final int ID_NOTIFICACION_CREAR = 1;

    @Override
    public void onCreate() {
	Toast.makeText(this, "Servicio creado", Toast.LENGTH_SHORT).show();
	reproductor = MediaPlayer.create(this, R.raw.audio);
	nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
	Toast.makeText(this, "Servicio arrancado " + idArranque,
		Toast.LENGTH_SHORT).show();
	reproductor.start();

	Notification notificacion = new Notification(R.drawable.ic_launcher,
		"Creando Servicio de Mœsica", System.currentTimeMillis());

	PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0,
		new Intent(this, Asteroides.class), 0);
	notificacion.setLatestEventInfo(this, "Reproduciendo mœsica",
		"informaci—n adicional", intencionPendiente);

	// Activar sonido a la notificaci—n
	notificacion.defaults |= Notification.DEFAULT_SOUND;
	// notificacion.sound = Uri.parse("file:///sdcard/carpeta/tono.mp3");
	// notificacion.sound = Uri.withAppendedPath(
	// Audio.Media.INTERNAL_CONTENT_URI, "6");

	// Vibraci—n
	// notificacion.defaults |= Notification.DEFAULT_VIBRATE;
	long[] vibrate = { 0, 100, 200, 300 };
	notificacion.vibrate = vibrate;

	// Parpadeo de LED
	//notificacion.defaults |= Notification.DEFAULT_LIGHTS;

	notificacion.ledARGB = 0xff00ff00;
	notificacion.ledOnMS = 300;
	notificacion.ledOffMS = 1000;
	notificacion.flags |= Notification.FLAG_SHOW_LIGHTS;

	nm.notify(ID_NOTIFICACION_CREAR, notificacion);

	return START_STICKY;
    }

    @Override
    public void onDestroy() {
	Toast.makeText(this, "Servicio detenido", Toast.LENGTH_SHORT).show();
	reproductor.stop();
	nm.cancel(ID_NOTIFICACION_CREAR);
    }

    @Override
    public IBinder onBind(Intent intencion) {
	return null;
    }
}