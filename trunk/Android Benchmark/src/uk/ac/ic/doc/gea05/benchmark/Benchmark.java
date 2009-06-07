package uk.ac.ic.doc.gea05.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Benchmark extends Activity implements OnClickListener {
	
	private static final String TAG = "Benchmark";
	private static final String DIRECTORY = "/logs/"+TAG+"/";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setupViews();
	}
	
	private TextView tv;
	private Button javaButton;
	private Button jniButton;

	private void setupViews(){
		tv = (TextView) findViewById(R.id.textview);
		javaButton = (Button) findViewById(R.id.button_java);
		jniButton = (Button) findViewById(R.id.button_jni);
		javaButton.setOnClickListener(this);
		jniButton.setOnClickListener(this);
	}
	
	private static final int maxPixels = 100000;
	private static final int step = 100;

	private int executed = 0;
	
	public class BenchmarkTask extends AsyncTask<Integer, Void, String> {

		private Map<Integer,Long> times;
				
		@Override
		protected String doInBackground(Integer... ids) {
			times = new HashMap<Integer,Long>();
			
			Random rand = new Random();
			int[] unsorted;
			int i, j, median;
			long start = 0;
			long finish = 0;
			long duration;
			for (i = step; i <= maxPixels; i += step) {
				unsorted = new int[i];

				for (j = 0; j < i; j++) {
					unsorted[j] = rand.nextInt();
				}

				switch (ids[0]) {
				case R.id.button_java:
					start = System.currentTimeMillis();
					QuickSort.quicksort(unsorted);
					finish = System.currentTimeMillis();
					break;
				case R.id.button_jni:
					start = System.currentTimeMillis();
					QuickSort.nQuicksort(unsorted);
					finish = System.currentTimeMillis();
					break;
				}
				median = unsorted[(unsorted.length - 1) / 2];
				duration = (finish - start);
				Log.i(TAG, String.format("Elements: %d Time: %d Median: %d", i,
						(finish - start), median));
				times.put(i, duration);
			}
			if(ids[0]==R.id.button_java)
				writeToLog("Java");
			else
				writeToLog("JNI");
			publishResults(times);
			return String.format("Finished : %d", ++executed);
		}
		@Override
		public void onPostExecute(String msg){
			changeText(msg);
		}
	}

	private void changeText(String msg){
		if(tv!=null){
			tv.setText(msg);
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_java:
			new BenchmarkTask().execute(R.id.button_java);
			break;
		case R.id.button_jni:
			new BenchmarkTask().execute(R.id.button_jni);
			break;
		default:
			Toast.makeText(this,
					String.format("Unknown view clicked %d", v.getId()),
					Toast.LENGTH_SHORT).show();
		}

	}
	
	// Logging
	// For the logs
	private BufferedWriter out;
	
	// csv
    private void publishResults(Map<Integer, Long> times) {
    	StringBuilder builder = new StringBuilder();
		builder.append("elements, time,\n");    	
    	for(Integer inputSize : times.keySet()){
    		builder.append(inputSize.toString() +","+ times.get(inputSize).toString()+",\n");    		
    	}
    	writeToLog(builder.toString());
	}

	private void writeToLog(String msg){    
    	try {
    		if(out==null){
    		    Log.i(TAG, "Creating new file");
    			File root = Environment.getExternalStorageDirectory();
    	        File benchmarkLog = new File(root.toString()+DIRECTORY, "Benchmark_"+DateFormat.format("M-d-yy-mm",new Date()) +".log");
    	        benchmarkLog.createNewFile();    	        
    	        FileWriter writer = new FileWriter(benchmarkLog);
    	        out = new BufferedWriter(writer);    	    	    	
    		}
    		out.write(msg+"\n");
    	} catch (IOException e) {
    	    Log.e(TAG, "Could not write file " + e.getMessage());
    	}		
	}
}