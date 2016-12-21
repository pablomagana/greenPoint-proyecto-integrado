package com.app.greenpoint.charts;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.app.greenpoint.R;
import com.app.greenpoint.model.Mensual;
import com.app.greenpoint.model.Reciclaje;

public class ColumnChart extends WebView {

    private static final String TAG = "Mensual";
    private Mensual data;
    private String fileUri = "file:///android_res/raw/columnchart.html";


    public ColumnChart(Context context) {
        super(context);
    }

    public ColumnChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColumnChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void create(Mensual data) {
        this.data = data;

        this.addJavascriptInterface(new ColumnInterface(), TAG);
        this.getSettings().setJavaScriptEnabled(true);
        this.loadUrl(fileUri);
    }

    private final class ColumnInterface {

        @JavascriptInterface
        public String getNomMes() {
            String[] meses = getContext().getResources().getStringArray(R.array.meses);
            int mes = Integer.parseInt(data.getNumMes());
            return meses[mes-1];
        }

        @JavascriptInterface
        public String getNomTipoContenedor(int tipo) {
            String[] contenedores = getContext().getResources().getStringArray(R.array.tipo_contenedores);
            return contenedores[tipo];
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
