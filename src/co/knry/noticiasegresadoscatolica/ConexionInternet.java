package co.knry.noticiasegresadoscatolica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class ConexionInternet extends AsyncTask<String, String, String>{

	private static final int GET = 0;
	private static final int POST = 1;
	private Activity act;
	private String string_url;
	private int tipo;
	private Message respuesta;
	private Handler puente;
	private HttpParams httpparams;
	private String parametro;
	private Header[] headers;
	private String autorizacion;
	private String codificacion="UTF-8";
	
	private ProgressBar progressBar;
	private boolean showProgressBar;
	
	public ConexionInternet(Activity act) {
		this.act=act;
	}
	
	@Override
    protected void onPreExecute() {
		Log.i(Propiedades.TAG, "onPreExecute");
		if (respuesta==null){
			respuesta=new Message();
		}
		if (puente == null){
			puente = new Handler();
		}
		if (progressBar==null){
			if(showProgressBar){
				progressBar = new ProgressBar(act);
				WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				int width = display.getWidth();
				if (act.getWindow().getDecorView().findViewById(android.R.id.content) instanceof FrameLayout){
					android.widget.FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(width/3, width/3);
					flp.gravity=Gravity.CENTER;
					progressBar.setLayoutParams(flp);
					FrameLayout fr = (FrameLayout) act.getWindow().getDecorView().findViewById(android.R.id.content);
					
					Drawable d = new ColorDrawable(Color.LTGRAY);
					
					fr.setBackgroundDrawable(d);
					fr.addView(progressBar);
				}
			}
		}
		else {
			progressBar.setVisibility(View.VISIBLE);
		}
		
    }
	
	@Override
	protected void onProgressUpdate(String ... values) {
        Log.i(Propiedades.TAG, "onProgressUpdate");
    }
    
	@Override
    protected void onPostExecute(String result) {
        Log.d(Propiedades.TAG, "onPostExecute ");
        
         if (progressBar!=null){
        	 progressBar.setVisibility(View.GONE);	 
        }
         
         FrameLayout fr = (FrameLayout) act.getWindow().getDecorView().findViewById(android.R.id.content);
         Drawable d = new ColorDrawable(Color.TRANSPARENT);
         fr.setBackgroundDrawable(d);
         puente.sendMessage(respuesta);
    }

	@Override
	protected String doInBackground(String ... params) {
		Log.i(Propiedades.TAG, "doInBackground"+act.toString()+" "+string_url);		
		respuesta.what=0;
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpUriRequest httprequest;
			
			switch (tipo) {
			case GET: 
				httprequest=new HttpGet(string_url);
				HttpResponse response = httpClient.execute(httprequest);
				
	            InputStream inputStream = response.getEntity().getContent();
	            
	            if (response.getStatusLine().getStatusCode()!=200){
	            	respuesta.what=3;
	    			respuesta.obj=response.getStatusLine().getReasonPhrase();
	    			break;
	            }
	            
	            //InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "iso-8859-1");
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, codificacion);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            
	            StringBuilder stringBuilder = new StringBuilder();

	            String bufferedStrChunk = null;

	            int i=0;
	            
	            while((bufferedStrChunk = bufferedReader.readLine()) != null){
	            	Log.d("LINEA "+i, bufferedStrChunk);
					i++;
	               stringBuilder.append(bufferedStrChunk);
	            }
	            
	            respuesta.obj=stringBuilder.toString();
	            stringBuilder=null;
				break;
			case POST:
				URL url = new URL(string_url);
				OutputStream os = null;
				HttpURLConnection conexion=(HttpURLConnection) url.openConnection();
				conexion.setRequestMethod("POST");
				conexion.setDoInput(true);
				conexion.setInstanceFollowRedirects(false);
				//conexion.setDoOutput(false);
				
				conexion.addRequestProperty("Autorization", autorizacion);
				conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				
				os = conexion.getOutputStream();
				os.write(parametro.getBytes());
				os.flush();
				os.close();
				
				InputStreamReader isr = new InputStreamReader(conexion.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				String linea;
				StringBuilder sb = new StringBuilder();
				
				i=0;
				
				while ((linea = br.readLine()) != null){
					i++;
					sb.append(linea);	
				}
				respuesta.obj=sb.toString();
				
				break;

			default:
				httprequest=new HttpGet(string_url);
				break;
			}
			
			
		} catch (MalformedURLException e) {
			respuesta.obj=e.toString();
			respuesta.what=1;
			e.printStackTrace();
		} catch (IOException e) {
			respuesta.what=2;
			respuesta.obj=e.toString();
			e.printStackTrace();
		}
		
		return null;
	}
	
	

	public void setString_url(String string_url) {
		this.string_url = string_url;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public void setPuente(Handler puente) {
		this.puente = puente;
	}
	
	public HttpParams getHttpparams() {
		return httpparams;
	}

	public void setHttpparams(HttpParams httpparams) {
		this.httpparams = httpparams;
	}

	public String getParametro() {
		return parametro;
	}

	public void setParametro(String parametro) {
		this.parametro = parametro;
	}
	
	public Header[] getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	public String getAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(String autorizacion) {
		this.autorizacion = autorizacion;
	}
	
	public String getCodificacion() {
		return codificacion;
	}

	public void setCodificacion(String codificacion) {
		this.codificacion = codificacion;
	}

	public boolean isShowProgressBar() {
		return showProgressBar;
	}

	public void setShowProgressDialog(boolean showProgressBar) {
		this.showProgressBar = showProgressBar;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

}