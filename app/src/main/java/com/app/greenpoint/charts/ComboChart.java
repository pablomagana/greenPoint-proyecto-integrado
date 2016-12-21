package com.app.greenpoint.charts;


import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.app.greenpoint.R;
import com.app.greenpoint.model.Anual;
import com.app.greenpoint.model.Reciclaje;

public class ComboChart extends WebView {

    private static final String TAG = "Anual";
    private Anual data;
    private String fileUri = "file:///android_res/raw/combochart.html";

    public ComboChart(Context context) {
        super(context);
    }

    public ComboChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComboChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Anual getData() {
        return data;
    }

    public void setData(Anual data) {
        this.data = data;
    }

    public void create(Anual data) {
        this.data = data;

        this.addJavascriptInterface(new ComboInterface(), TAG);
        this.getSettings().setJavaScriptEnabled(true);
        this.loadUrl(fileUri);
    }

    private final class ComboInterface {

        @JavascriptInterface
        public String getNomMes(int numMes) {
            String[] meses = getContext().getResources().getStringArray(R.array.meses);
            return meses[numMes];
        }

        @JavascriptInterface
        public String getNomTipoContenedor(int tipo) {
            String[] contenedores = getContext().getResources().getStringArray(R.array.tipo_contenedores);
            return contenedores[tipo];
        }

        @JavascriptInterface
        public double getCantidadReciclaje(int tipo, int mes) {
            Reciclaje r = data.getReciclajeTipo(tipo, mes);
            if (r != null)
                return r.getCantidad();
            return 0.0;
        }

        @JavascriptInterface
        public double cantidadMediaMes(int mes) {
            return data.getMediaMes(mes);
        }
    }
}
