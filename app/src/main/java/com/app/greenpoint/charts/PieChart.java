package com.app.greenpoint.charts;


import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.app.greenpoint.R;
import com.app.greenpoint.model.Reciclaje;
import com.app.greenpoint.model.Semanal;

public class PieChart extends WebView {

    private static final String TAG = "Semanal";
    private Semanal data;
    private String fileUri = "file:///android_res/raw/piechart.html";

    public PieChart(Context context) {
        super(context);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void create(Semanal data) {
        this.data = data;

        this.addJavascriptInterface(new PieInterface(), TAG);
        this.getSettings().setJavaScriptEnabled(true);
        this.loadUrl(fileUri);
    }

    private final class PieInterface {

        @JavascriptInterface
        public String getNomTipoContenedor(int tipo) {
            String[] contenedores = getContext().getResources().getStringArray(R.array.tipo_contenedores);
            return contenedores[tipo]+" (Kg)";
        }

        @JavascriptInterface
        public double getCantidadReciclaje(int tipo) {
            Reciclaje r = data.getReciclajeTipo(tipo);
            if (r != null)
                return r.getCantidad();
            return 0.0;
        }
    }

}
