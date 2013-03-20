package org.example.asteroides;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Asteroides extends Activity {

    private Button bSalir;
    public static AlmacenPuntuaciones almacen = new AlmacenPuntuacionesArray();
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	
	mp = MediaPlayer.create(this, R.raw.audio);

	bSalir = (Button) findViewById(R.id.button4);
	bSalir.setOnClickListener(new OnClickListener() {
	    public void onClick(View view) {
		finish();
	    }
	});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.menu, menu);
	return true;
    }

    public void lanzarAcercaDe(View view) {

	Intent i = new Intent(this, AcercaDe.class);
	startActivity(i);
    }

    public void lanzarPreferencias(View view) {

	Intent i = new Intent(this, Preferencias.class);
	startActivity(i);
    }

    public void lanzarPuntuaciones(View view) {

	Intent i = new Intent(this, Puntuaciones.class);
	startActivity(i);
    }

    public void lanzarJuego(View view) {

	Intent i = new Intent(this, Juego.class);
	startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.acercaDe:
	    lanzarAcercaDe(null);
	    break;
	case R.id.config:
	    lanzarPreferencias(null);
	    break;
	}
	return true;
	/** true -> consumimos el item, no se propaga */
    }

    @Override
    protected void onStart() {
	super.onStart();
	Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
	super.onResume();
	Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
	mp.start();
    }

    @Override
    protected void onPause() {
	Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
	super.onPause();
    }

    @Override
    protected void onStop() {
	Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
	mp.pause();
	super.onStop();
    }

    @Override
    protected void onRestart() {
	super.onRestart();
	Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
	Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
	super.onDestroy();
    }
}
