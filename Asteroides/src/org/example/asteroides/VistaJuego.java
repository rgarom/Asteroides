package org.example.asteroides;

import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VistaJuego extends View implements SensorEventListener {

    // //// NAVE //////
    private Grafico nave;// Gráfico de la nave
    private int giroNave; // Incremento de dirección
    private float aceleracionNave; // aumento de velocidad

    // Incremento estándar de giro y aceleración
    private static final int PASO_GIRO_NAVE = 5;
    private static final float PASO_ACELERACION_NAVE = 0.5f;

    // //// ASTEROIDES //////
    private Vector<Grafico> Asteroides; // Vector con los Asteroides
    private int numAsteroides = 5; // Número inicial de asteroides
    private int numFragmentos = 3; // Fragmentos en que se divide

    // //// THREAD Y TIEMPO //////
    // Thread encargado de procesar el juego
    private ThreadJuego thread = new ThreadJuego();
    // Cada cuanto queremos procesar cambios (ms)
    private static int PERIODO_PROCESO = 50;
    // Cuando se realiz— el œltimo proceso
    private long ultimoProceso = 0;

    private float mX = 0, mY = 0;
    private boolean disparo = false;

    private boolean hayValorInicial = false;
    private float valorInicial;

    // //// MISIL //////
    private Grafico misil;
    private static int PASO_VELOCIDAD_MISIL = 12;
    private boolean misilActivo = false;
    private int tiempoMisil;
    private SensorManager mSensorManager;
    private Sensor orientationSensor;

    // //// MULTIMEDIA //////
    SoundPool soundPool;
    int idDisparo, idExplosion;

    private int puntuacion = 0;

    private Activity padre;

    public static boolean hardcoreMode = false;

    public VistaJuego(Context context, AttributeSet attrs) {

	super(context, attrs);
	Drawable drawableNave, drawableAsteroide, drawableMisil;
	if (hardcoreMode) {
	    drawableAsteroide = context.getResources().getDrawable(
		    R.drawable.david);
	    drawableNave = context.getResources().getDrawable(R.drawable.pene);
	    drawableMisil = context.getResources().getDrawable(
		    R.drawable.splash);
	} else {
	    drawableAsteroide = context.getResources().getDrawable(
		    R.drawable.asteroide1);
	    drawableNave = context.getResources().getDrawable(R.drawable.nave);
	    drawableMisil = context.getResources().getDrawable(
		    R.drawable.misil1);
	}

	nave = new Grafico(this, drawableNave);
	misil = new Grafico(this, drawableMisil);
	Asteroides = new Vector<Grafico>();

	for (int i = 0; i < numAsteroides; i++) {
	    if (hardcoreMode) {
		switch (Math.round((float) Math.random() * 3)) {
		case 0:
		    drawableAsteroide = context.getResources().getDrawable(
			    R.drawable.abraham);
		    break;
		case 1:
		    drawableAsteroide = context.getResources().getDrawable(
			    R.drawable.david);
		    break;
		case 2:
		    drawableAsteroide = context.getResources().getDrawable(
			    R.drawable.jose);
		    break;
		default:
		    drawableAsteroide = context.getResources().getDrawable(
			    R.drawable.jose);
		    break;
		}
	    }
	    Grafico asteroide = new Grafico(this, drawableAsteroide);
	    asteroide.setIncY(Math.random() * 4 - 2);
	    asteroide.setIncX(Math.random() * 4 - 2);
	    asteroide.setAngulo((int) (Math.random() * 360));
	    asteroide.setRotacion((int) (Math.random() * 8 - 4));
	    Asteroides.add(asteroide);
	}

	mSensorManager = (SensorManager) context
		.getSystemService(Context.SENSOR_SERVICE);
	List<Sensor> listSensors = mSensorManager
		.getSensorList(Sensor.TYPE_ORIENTATION);
	if (!listSensors.isEmpty()) {
	    orientationSensor = listSensors.get(0);
	    mSensorManager.registerListener(this, orientationSensor,
		    SensorManager.SENSOR_DELAY_GAME);
	}

	// Misil con gráficos vectoriales
	ShapeDrawable dMisil = new ShapeDrawable(new RectShape());
	dMisil.getPaint().setColor(Color.WHITE);
	dMisil.getPaint().setStyle(Style.STROKE);
	dMisil.setIntrinsicWidth(15);
	dMisil.setIntrinsicHeight(3);
	drawableMisil = dMisil;

	// soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	// idDisparo = soundPool.load(context, R.raw.disparo, 0);
	// idExplosion = soundPool.load(context, R.raw.explosion, 0);
    }

    @Override
    synchronized protected void onSizeChanged(int ancho, int alto,
	    int ancho_anter, int alto_anter) {

	super.onSizeChanged(ancho, alto, ancho_anter, alto_anter);

	// Una vez que conocemos nuestro ancho y alto.
	for (Grafico asteroide : Asteroides) {
	    do {
		asteroide.setPosX(Math.random()
			* (ancho - asteroide.getAncho()));
		asteroide.setPosY(Math.random() * (alto - asteroide.getAlto()));
	    } while (asteroide.distancia(nave) < (ancho + alto) / 5);
	}

	nave.setPosX(ancho / 2);
	nave.setPosY(alto / 2);

	ultimoProceso = System.currentTimeMillis();
	thread.start();
    }

    @Override
    synchronized protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	for (Grafico asteroide : Asteroides) {
	    asteroide.dibujaGrafico(canvas);
	}

	nave.dibujaGrafico(canvas);

	if (misilActivo) {
	    misil.dibujaGrafico(canvas);
	}
    }

    protected void actualizaFisica() {
	long ahora = System.currentTimeMillis();
	// No hagas nada si el per’odo de proceso no se ha cumplido.
	if (ultimoProceso + PERIODO_PROCESO > ahora) {
	    return;
	}
	// Para una ejecuci—n en tiempo real calculamos retardo
	double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO;
	ultimoProceso = ahora; // Para la pr—xima vez
	// Actualizamos velocidad y direcci—n de la nave a partir de
	// giroNave y aceleracionNave (segœn la entrada del jugador)
	nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));
	double nIncX = nave.getIncX() + aceleracionNave
		* Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
	double nIncY = nave.getIncY() + aceleracionNave
		* Math.sin(Math.toRadians(nave.getAngulo())) * retardo;
	// Actualizamos si el m—dulo de la velocidad no excede el m‡ximo
	if (Math.hypot(nIncX, nIncY) <= Grafico.getMaxVelocidad()) {
	    nave.setIncX(nIncX);
	    nave.setIncY(nIncY);
	}
	// Actualizamos posiciones X e Y
	nave.incrementaPos(retardo);
	for (Grafico asteroide : Asteroides) {
	    asteroide.incrementaPos(retardo);
	}

	// Actualizamos posición de misil
	if (misilActivo) {
	    misil.incrementaPos(retardo);
	    tiempoMisil -= retardo;
	    if (tiempoMisil < 0) {
		misilActivo = false;
	    } else {
		for (int i = 0; i < Asteroides.size(); i++)
		    if (misil.verificaColision(Asteroides.elementAt(i))) {
			destruyeAsteroide(i);
			break;
		    }
	    }
	}

	for (Grafico asteroide : Asteroides) {
	    if (asteroide.verificaColision(nave)) {
		salir();
	    }
	}
    }

    class ThreadJuego extends Thread {
	private boolean pausa, corriendo;

	public synchronized void pausar() {
	    pausa = true;
	}

	public synchronized void reanudar() {
	    pausa = false;
	    notify();
	}

	public void detener() {
	    corriendo = false;
	    if (pausa)
		reanudar();
	}

	@Override
	public void run() {
	    corriendo = true;
	    while (corriendo) {
		actualizaFisica();
		synchronized (this) {
		    while (pausa) {
			try {
			    wait();
			} catch (Exception e) {
			}
		    }
		}
	    }
	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
	super.onTouchEvent(event);
	float x = event.getX();
	float y = event.getY();
	switch (event.getAction()) {
	case MotionEvent.ACTION_DOWN:
	    disparo = true;
	    break;
	case MotionEvent.ACTION_MOVE:
	    float dx = Math.abs(x - mX);
	    float dy = Math.abs(y - mY);
	    if (dy < 6 && dx > 6) {
		giroNave = Math.round((x - mX) / 2);
		disparo = false;
	    } else if (dx < 6 && dy > 6) {

		// TODO: No decelerar.
		// Para detener la nave dar giro de 180º y acelerar poco.
		aceleracionNave = Math.round((mY - y) / 25);
		disparo = false;
	    }
	    break;
	case MotionEvent.ACTION_UP:
	    giroNave = 0;
	    aceleracionNave = 0;
	    if (disparo) {
		activaMisil();
	    }
	    break;
	}
	mX = x;
	mY = y;
	return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // TODO: Modifica el ejemplo anterior para utilizar el sensor de aceleración
    // en lugar del de orientación. Gracias a la fuerza de gravedad que la
    // Tierra ejerce sobre el terminal podremos saber si este está horizontal.
    // En caso de que la nave este horizontal (o casi) no ha de girar, pero
    // cuando el terminal se incline, la nave a de girar proporcionalmente a
    // esta inclinación. Utiliza los programas anteriores para descubrir que eje
    // (x, y o z) es el que te interesa y el rango de valores que proporciona.
    @Override
    public void onSensorChanged(SensorEvent event) {
	float valor = event.values[1];
	if (!hayValorInicial) {
	    valorInicial = valor;
	    hayValorInicial = true;
	}
	giroNave = (int) (valor - valorInicial) / 3;
    }

    private void destruyeAsteroide(int i) {
	Asteroides.remove(i);
	misilActivo = false;

	puntuacion += 1000;

	if (Asteroides.isEmpty()) {
	    salir();
	}

	// soundPool.play(idExplosion, 1, 1, 0, 0, 1);
    }

    private void activaMisil() {
	misil.setPosX(nave.getPosX() + nave.getAncho() / 2 - misil.getAncho()
		/ 2);
	misil.setPosY(nave.getPosY() + nave.getAlto() / 2 - misil.getAlto() / 2);
	misil.setAngulo(nave.getAngulo());
	// Para descomponer la velocidad de la nave en sus componentes X e Y
	// utilizamos el coseno y el seno
	misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo()))
		* PASO_VELOCIDAD_MISIL);
	misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo()))
		* PASO_VELOCIDAD_MISIL);
	tiempoMisil = (int) Math.min(
		this.getWidth() / Math.abs(misil.getIncX()), this.getHeight()
			/ Math.abs(misil.getIncY())) - 2;
	misilActivo = true;

	// soundPool.play(idDisparo, 1, 1, 1, 0, 1);
    }

    public ThreadJuego getThread() {
	return thread;
    }

    public SensorManager getmSensorManager() {
	return mSensorManager;
    }

    public Sensor getOrientationSensor() {
	return orientationSensor;
    }

    public void setPadre(Activity padre) {
	this.padre = padre;
    }

    private void salir() {
	Bundle bundle = new Bundle();
	bundle.putInt("puntuacion", puntuacion);
	Intent intent = new Intent();
	intent.putExtras(bundle);
	padre.setResult(Activity.RESULT_OK, intent);
	padre.finish();
    }
}