package co.knry.noticiasegresadoscatolica;


import java.io.IOException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gcm.GCMRegistrar;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RegistroActivity extends Activity{
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "1001212257312";

    Context context;
	private String IdGCM;
	private Button btn_registro_traer;

	static final String TAG = "GCM Demo";
	
	GoogleCloudMessaging gcm;
	String regid;
	
	TextView mDisplay;
    
	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);
        
        mDisplay = (TextView) findViewById(R.id.display);

        context = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            else{
            	Log.i("CODIGO", regid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
     
        btn_registro_traer = (Button) findViewById(R.id.btn_registro_traer);
        btn_registro_traer.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mostrarID();
			}
		});
        
	/*	try {
			GCMRegistrar.checkDevice(RegistroActivity.this);
	        GCMRegistrar.checkManifest(RegistroActivity.this);
	        GCMRegistrar.register(RegistroActivity.this, SENDER_ID);
	        IdGCM = GCMRegistrar.getRegistrationId(RegistroActivity.this);
	        
			}
			catch(Exception exe){
				Toast.makeText(this, ":-(", Toast.LENGTH_SHORT).show();
	    	}
		
		*/

    }



	protected void mostrarID() {
		try {
			IdGCM = GCMRegistrar.getRegistrationId(RegistroActivity.this);
			Toast.makeText(this, ":-$ "+IdGCM, Toast.LENGTH_SHORT).show();
			
			Log.d(":-)", IdGCM);
		}
		catch(Exception exe){
			IdGCM="";
			Toast.makeText(this, ":-P", Toast.LENGTH_SHORT).show();
    	}
		
		
	}

	
	private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
	
	@SuppressLint("NewApi") 
	private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
	
	private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(RegistroActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
	
	private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
	
	private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	Log.d("CODIGO", msg);
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }
	
	private void sendRegistrationIdToBackend() {
	      // Your implementation here.
	    }
	
	private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
