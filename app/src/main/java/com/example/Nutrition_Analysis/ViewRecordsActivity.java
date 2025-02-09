package com.example.Nutrition_Analysis;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test_app.R;

import java.util.List;

public class ViewRecordsActivity extends AppCompatActivity {

    private RecyclerView recordsRecyclerView;
    private RecordAdapter adapter;
    private DatabaseHelper databaseHelper;
    private Button clearRecordsButton;
    private List<NutritionRecord> recordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_records);

        recordsRecyclerView = findViewById(R.id.recordsRecyclerView);
        recordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        clearRecordsButton = findViewById(R.id.clearRecordsButton);
        databaseHelper = new DatabaseHelper(this);

        // Load and display the records initially
        loadRecords();

        // Clear all records when the button is clicked
        clearRecordsButton.setOnClickListener(v -> clearAllRecords());
    }

    private void loadRecords() {
        recordList = databaseHelper.getAllRecords();
        adapter = new RecordAdapter(recordList);
        recordsRecyclerView.setAdapter(adapter);
    }

    private void clearAllRecords() {
        if (recordList.isEmpty()) {
            Toast.makeText(this, "No records to clear.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.clearAllRecords();
        recordList.clear();
        adapter.notifyDataSetChanged();  // Refresh the RecyclerView
        Toast.makeText(this, "All records cleared successfully.", Toast.LENGTH_SHORT).show();
    }
}
