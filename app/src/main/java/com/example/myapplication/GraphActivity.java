package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private BarChart barChart;
    private DatabaseReference milkRecordRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        barChart = findViewById(R.id.barChart);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        milkRecordRef = FirebaseDatabase.getInstance().getReference()
                .child("farmers")
                .child(userId)
                .child("milkRecords");

        loadMilkRecords();
    }

    private void loadMilkRecords() {
        milkRecordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                int index = 0;

                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    MilkRecord record = recordSnapshot.getValue(MilkRecord.class);
                    if (record != null) {
                        float liters = Float.parseFloat(record.liters);
                        entries.add(new BarEntry(index, liters));
                        dates.add(record.date);
                        index++;
                    }
                }

                BarDataSet dataSet = new BarDataSet(entries, "Milk Production (Liters)");
                dataSet.setColor(getResources().getColor(R.color.purple_200));

                BarData barData = new BarData(dataSet);
                barChart.setData(barData);

                XAxis xAxis = barChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);

                barChart.getDescription().setEnabled(false);
                barChart.animateY(1000);
                barChart.invalidate(); // refresh
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
            }
        });
    }
}

