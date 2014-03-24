package com.zjy.headsup;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	View layoutMain, layoutHelp, layoutSelection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = getLayoutInflater();
		layoutMain = inflater.inflate(R.layout.activity_main, null);
		layoutHelp = inflater.inflate(R.layout.help, null);
		layoutSelection = inflater.inflate(R.layout.selection, null);
		setContentView(layoutMain);
		buttonInitialization();
	}
	
	protected void onStop() {
		super.onStop();
		Log.i("tag", "Main stopped");
	}
	
	protected void onDestroy() {
		super.onDestroy();
		Log.i("tag", "Main destroyed");
	}
	
	private void buttonInitialization() {
		final Button buttonStart = (Button) findViewById(R.id.buttonStart_id);
		final Button buttonHelp = (Button) findViewById(R.id.buttonHelp_id);
		final Button buttonQuit = (Button) findViewById(R.id.buttonQuit_id);
		
		buttonStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gameSelection();
			}
		});
		
		buttonHelp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				viewHelp();
			}
		});
		
		buttonQuit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int pid = android.os.Process.myPid();
				android.os.Process.killProcess(pid);
			}
		});
	}
	
	private void viewHelp() {
		setContentView(layoutHelp);
		final Button buttonHelpBack = (Button) findViewById(R.id.buttonHelpBack_id);
		buttonHelpBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setContentView(layoutMain);
			}
		});
	}
	
	private void gameSelection() {
		setContentView(layoutSelection);
		final Button buttonSelectionBack = (Button) findViewById(R.id.buttonSelectionBack_id);
		final Button buttonMovie = (Button) findViewById(R.id.buttonMovie_id);
		final Button buttonLocation = (Button) findViewById(R.id.buttonLocation_id);
		final Button buttonActor = (Button) findViewById(R.id.buttonActor_id);
		final Button buttonActress = (Button) findViewById(R.id.buttonActress_id);
		final Button buttonFood = (Button) findViewById(R.id.buttonFood_id);
		final Button buttonBrand = (Button) findViewById(R.id.buttonBrand_id);
		final Button buttonDaily = (Button) findViewById(R.id.buttonDaily_id);
		
		buttonMovie.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("movie");
			}
		});
		
		buttonLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("location");
			}
		});
		
		buttonActor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("actor");
			}
		});
		
		buttonActress.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("actress");
			}
		});

		buttonBrand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("brand");
			}
		});

		buttonFood.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("food");
			}
		});
		
		buttonDaily.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame("daily");
			}
		});
		
		buttonSelectionBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setContentView(layoutMain);
			}
		});
	}
	
	private void startGame(String wordtype) {
		Intent intentGame = new Intent(MainActivity.this, GameActivity.class);
		Bundle bundleGame = new Bundle();
		bundleGame.putString("wordtype", wordtype);
		intentGame.putExtras(bundleGame);
		startActivity(intentGame);
		finish();
	}

}
