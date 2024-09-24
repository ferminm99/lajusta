package com.example.lajusta;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class AgregarDatosParcela extends AppCompatActivity {

    private String idParcela;
    private Spinner CampoVerduras;
    private TextView CampoSurcos;
    private TextView CampoSuperficie;
    private HashMap datos;
    private ArrayList<Integer> idVerduras = new ArrayList<Integer>();
    private String activity;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_datos_parcela);

        this.CampoSuperficie = (EditText) findViewById(R.id.editTextNumber5);
        this.CampoSurcos = (EditText) findViewById(R.id.editTextNumber3);
        Intent myIntent = getIntent();
        this.idParcela = myIntent.getStringExtra("idParcela");
        this.activity = myIntent.getStringExtra("activity");
        if (this.activity.equals("ver")) {
            this.id = myIntent.getStringExtra("id");
        }
        this.datos = (HashMap) myIntent.getSerializableExtra("datos");
        TextView titulo = (TextView) findViewById(R.id.textView30);
        titulo.setText(this.idParcela);
        /*
        SQLiteDatabase db = MainActivity.conn.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM verduras", null);

        this.CampoVerduras = (Spinner) findViewById(R.id.spinner3);
        ArrayList<String> arrayVerduras = new ArrayList<String>();
        while (cursor.moveToNext()) {
            arrayVerduras.add(cursor.getString(0)+"- "+cursor.getString(1)+" "+cursor.getString(2)+"/"+cursor.getString(3));
        }
        db.close();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayVerduras);
        this.CampoVerduras.setAdapter(adapter);
        */
    }
    /*
    public void agregarVerdura(View view) {
        String texto = this.CampoVerduras.getSelectedItem().toString();
        String[] partes = texto.split("-");
        this.idVerduras.add(parseInt(partes[0]));

        TableLayout tableLayout=(TableLayout)findViewById(R.id.tableLayout);
        View tableRow = LayoutInflater.from(this).inflate(R.layout.table_item_verdura_parcela,null,false);
        TextView name  = (TextView) tableRow.findViewById(R.id.name);
        name.setText(partes[1]);
        tableLayout.addView(tableRow);
    }

    public void guardar(View view) {
        Intent intent = null;
        if (this.activity.equals("ver")) {
            intent = new Intent(this, VerQuinta.class);
            intent.putExtra("id",this.id);
        } else {
            intent = new Intent(this, CrearQuinta.class);
        }
        this.idVerduras.add(parseInt(this.CampoSurcos.getText().toString()));
        this.idVerduras.add(parseInt(this.CampoSuperficie.getText().toString()));
        this.datos.put(this.idParcela, this.idVerduras);
        intent.putExtra("datos",this.datos);
        startActivity(intent);
    }

         */
}