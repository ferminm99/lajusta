package com.example.lajusta;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class VerVerdurasDeParcela extends AppCompatActivity {

    private String idParcela;
    private String idQuinta;
    private HashMap datos = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_verduras_de_parcela);

        Intent myIntent = getIntent();
        this.datos = (HashMap) myIntent.getSerializableExtra("datos");
        this.idParcela = myIntent.getStringExtra("idParcela");
        this.idQuinta = myIntent.getStringExtra("idQuinta");
        String[] parametros = {this.idParcela,this.idQuinta};

        /*SQLiteDatabase db = MainActivity.conn.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM quintas_parcelas WHERE idParcela = ? AND idQuinta= ?", parametros);
        cursor.moveToFirst();
        EditText surcos = (EditText) findViewById(R.id.editTextNumber3);
        EditText superficie = (EditText) findViewById(R.id.editTextNumber5);
        surcos.setText(cursor.getString(4));
        superficie.setText(cursor.getString(5));
        surcos.setEnabled(false);
        superficie.setEnabled(false);
        cursor.moveToPrevious();
        while (cursor.moveToNext()) {
            String[] parametros2 = {cursor.getString(3)};
            Cursor verdura = db.rawQuery("SELECT * FROM verduras WHERE id =?", parametros2);
            verdura.moveToFirst();
            TableLayout tableLayout=(TableLayout)findViewById(R.id.tableLayout);
            View tableRow = LayoutInflater.from(this).inflate(R.layout.table_item_verdura_parcela,null,false);
            TextView name  = (TextView) tableRow.findViewById(R.id.name);
            name.setText(verdura.getString(1)+" "+verdura.getString(2)+"/"+verdura.getString(3));
            tableLayout.addView(tableRow);
        }
        cursor.close();
        */
    }

    public void volverVerQuinta(View view) {
        Intent intent = new Intent(this, VerVisita.class);
        intent.putExtra("datos",this.datos);
        intent.putExtra("id",this.idQuinta);
        startActivity(intent);
    }
}