package co.knry.noticiasegresadoscatolica;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class PruebaActivity extends Activity{
	
	public static final String MENSAJE = "mensaje";
	public static final String URL = "url";
	private TextView textView1;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bienvenida);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString(MENSAJE);
            String url = extras.getString(URL);
            textView1=(TextView)findViewById(R.id.textView1);
            textView1.setText(value);
			
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.setWebViewClient(new WebViewClient()       
            {
                @Override
               public boolean shouldOverrideUrlLoading(WebView view, String url) 
               {
                   view.loadUrl(url);
                   System.out.println("hello");
                   return true;
               }
           });
            myWebView.loadUrl(url);
        }
	}

}
