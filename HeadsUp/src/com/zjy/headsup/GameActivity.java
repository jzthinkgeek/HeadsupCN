package com.zjy.headsup;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GameActivity extends Activity {

	Intent intentGame;
	Intent intentMain;
	Bundle bundleGame;
	View layoutReady, layoutGame;
	SensorManager sensorManager;
	Sensor accSensor;
	SensorEventListener accListener;
	SQLiteDatabase wordDatabase;
	String wordtype;
	int score = 0, pass = 0;
	int wordNumber = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		LayoutInflater inflater = getLayoutInflater();
		
		layoutReady = inflater.inflate(R.layout.ready, null);
		layoutGame = inflater.inflate(R.layout.activity_game, null);
		setContentView(layoutReady);
		
		intentGame = this.getIntent();
		bundleGame = intentGame.getExtras();
		wordtype = bundleGame.getString("wordtype");
		
		AssetsDatabaseManager.initManager(getApplication());
		AssetsDatabaseManager assetDbManager = AssetsDatabaseManager.getManager();
		wordDatabase = assetDbManager.getDatabase("gamedata.db");
		getReady();
	}
	
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(accListener);
	}
	
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(accListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	protected void onStop() {
		super.onStop();
		Log.i("tag", "Game stopped");
	}
	
	protected void onDestroy() {
		super.onDestroy();
		Log.i("tag", "Game destroyed");
	}
	
	private void getReady() {
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accListener = new SensorEventListener() {
			int count = 0;
			
			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {
			}

			@Override
			public void onSensorChanged(SensorEvent e) {
				count = countSensorChanged(e, count);
				if (count == 4) {
					sensorManager.unregisterListener(accListener);
					startCountDown();
				}
			}
		};
		
		sensorManager.registerListener(accListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
	}
	
	private int countSensorChanged(SensorEvent e, int count) {
		switch (count) {
		case 0: case 2:
			if (e.values[2] < -5) {
				count++;
			}
			return count;
		case 1: case 3:
			if (e.values[2] > -2) {
				count++;
			}
			return count;
		default:
			return count;
		}
	}
	
	private void startCountDown() {
		sensorManager.unregisterListener(accListener);
		ReadyCount readyCount = new ReadyCount(5100, 1000);
		readyCount.start();
	}
	
	private class ReadyCount extends CountDownTimer {
		
		TextView textviewReady;
		
		public ReadyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			textviewReady = (TextView) findViewById(R.id.textviewReady_id);
		}
		
		@Override
		public void onFinish() {
			gameRunning();
		}
		
		public void onTick(long millisUntilFinished) {
			int secondsUntilFinished = (int) millisUntilFinished / 1000;
			if (secondsUntilFinished > 4) {
				textviewReady.setTextSize(150);
				textviewReady.setText("Ready?");
			} else if (secondsUntilFinished < 2){
				textviewReady.setText("Go!");
			} else {
				textviewReady.setText(Integer.toString(secondsUntilFinished-1));
			}
		}
	}
	
	private void gameRunning() {
		setContentView(layoutGame);
		final TextView textviewWord = (TextView) findViewById(R.id.textviewWord_id);
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Button buttonBack = (Button) findViewById(R.id.buttonBack_id);
		
		int randomId = 0;
		String word;
		String randomSelectSQL = "SELECT Value FROM %s WHERE id = %d";
		String countWordNumberSQL = "SELECT count(*) FROM %s";
		Cursor cursor;
		GameCountDown gameClock = new GameCountDown(60000, 1000);
		final Map<Integer, Integer> idTable = new HashMap<Integer, Integer>();
		
		accListener = new SensorEventListener() {
			int count = 0;
			
			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {
			}

			@Override
			public void onSensorChanged(SensorEvent e) {
				count = UserAction(e, count, textviewWord, idTable);
			}
		};
		
		buttonBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intentMain = new Intent(GameActivity.this, MainActivity.class);
				sensorManager.unregisterListener(accListener);
				startActivity(intentMain);
				finish();
			}
		});
		
		cursor = wordDatabase.rawQuery(String.format(countWordNumberSQL, wordtype), null);
		cursor.moveToFirst();
		wordNumber = cursor.getInt(0);
		randomId = (int) (Math.random()*wordNumber+1);
		idTable.put(randomId, randomId);
		cursor = wordDatabase.rawQuery(String.format(randomSelectSQL, wordtype, randomId), null);
		cursor.moveToFirst();
		word = cursor.getString(0);
		cursor.close();
		if (word.length() > 7) {
			textviewWord.setTextSize(60);
		} else if (word.length() > 4) {
			textviewWord.setTextSize(80);
		} else {
			textviewWord.setTextSize(120);
		}
		textviewWord.setText(word);
		gameClock.start();
		
		sensorManager.registerListener(accListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private class GameCountDown extends CountDownTimer {
		
		TextView textviewCountDownClock;
		TextView textviewWord;
		
		public GameCountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			textviewCountDownClock = (TextView) findViewById(R.id.textviewCountDownClock_id);
			textviewWord = (TextView) findViewById(R.id.textviewWord_id);
		}
		
		@Override
		public void onFinish() {
			GameFinish(textviewWord);
		}
		
		public void onTick(long millisUntilFinished) {
			int secondsUntilFinished = (int) millisUntilFinished / 1000;
			if (secondsUntilFinished > 10) {
				textviewCountDownClock.setText(Integer.toString(secondsUntilFinished));
			} else {
				textviewCountDownClock.setTextColor(getResources().getColor(R.drawable.red));
				textviewCountDownClock.setTextSize(70);
				textviewCountDownClock.setText(Integer.toString(secondsUntilFinished));
			}
		}
	}
	
	private int UserAction(SensorEvent e, int count, final TextView textviewWord, final Map<Integer, Integer> idTable) {
		if (count == 0) {
			if (e.values[2] < -11) {
				count++;
				return count;
			} else if (e.values[2] > 11) {
				count--;
				return count;
			}
			return count;
		} else if (count > 0) {
			//The word is correct
			if (e.values[2] > -3) {
				textviewWord.setTextSize(120);
				textviewWord.setBackgroundColor(getResources().getColor(R.drawable.green));
				textviewWord.setText(getResources().getString(R.string.correct));
				score++;
				count = 0;
				new Handler().postDelayed(new Runnable(){    
				    public void run() {    
				    	NewWord(idTable, textviewWord);
				    }    
				 }, 300);
			}
			return count;
		} else {
			//Pass the word
			if (e.values[2] < 3) {
				textviewWord.setTextSize(120);
				textviewWord.setBackgroundColor(getResources().getColor(R.drawable.red));
				textviewWord.setText(getResources().getString(R.string.pass));
				pass++;
				count = 0;
				new Handler().postDelayed(new Runnable(){    
				    public void run() {    
				    	NewWord(idTable, textviewWord);
				    }    
				 }, 300);   
			}
			return count;
		}
	}
	
	private void NewWord(Map<Integer, Integer> idTable, TextView textviewWord) {
		Cursor cursor;
		String randomSelectSQL = "SELECT Value FROM %s WHERE id = %d";
		int randomId = (int) (Math.random()*wordNumber+1);
		while (idTable.get(randomId) != null) {
			randomId = (int) (Math.random()*wordNumber+1);
		}
		idTable.put(randomId, randomId);
		cursor = wordDatabase.rawQuery(String.format(randomSelectSQL, wordtype, randomId), null);
		cursor.moveToFirst();
		String word = cursor.getString(0);
		cursor.close();
		if (word.length() > 7) {
			textviewWord.setTextSize(60);
		} else if (word.length() > 5) {
			textviewWord.setTextSize(80);
		} else {
			textviewWord.setTextSize(110);
		}
		textviewWord.setBackgroundColor(getResources().getColor(R.drawable.lightblue));
		textviewWord.setText(word);
	}
	
	private void GameFinish(final TextView textviewWord) {
		Button buttonBack = (Button) findViewById(R.id.buttonBack_id);
		TextView textviewCountDownClock = (TextView) findViewById(R.id.textviewCountDownClock_id);
		textviewCountDownClock.setVisibility(TextView.INVISIBLE);
		intentMain = new Intent(GameActivity.this, MainActivity.class);
		textviewWord.setTextSize(110);
		textviewWord.setText(getResources().getString(R.string.gameend));
		new Handler().postDelayed(new Runnable(){    
		    public void run() {
		    	textviewWord.setTextSize(50);
		    	if ((score < 10 && pass < 10) || (score >= 10 && pass >= 10)) {
		    		textviewWord.setText(getResources().getString(R.string.gameresult_title) + "\n\n正确:  " + score + "\n跳过:  " + pass);
		    	} else if (score < 10 && pass >= 10) {
		    		textviewWord.setText(getResources().getString(R.string.gameresult_title) + "\n\n正确:    " + score + "\n跳过:  " + pass);
		    	} else {
		    		textviewWord.setText(getResources().getString(R.string.gameresult_title) + "\n\n正确:  " + score + "\n跳过:    " + pass);
		    	}
		    	
		    }    
		 }, 1000);
		sensorManager.unregisterListener(accListener);
		buttonBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				score = 0;
				pass = 0;
				startActivity(intentMain);
		    	finish();
			}
		});
	}
	
}
